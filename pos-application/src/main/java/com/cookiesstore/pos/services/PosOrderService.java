package com.cookiesstore.pos.services;

import com.cookiesstore.common.dto.orders.OrderAddressForm;
import com.cookiesstore.common.dto.orders.OrderItemForm;
import com.cookiesstore.common.entities.AdminSourcePosOrder;
import com.cookiesstore.common.entities.Customer;
import com.cookiesstore.common.entities.CustomerAddress;
import com.cookiesstore.common.entities.Order;
import com.cookiesstore.common.entities.OrderAddressType;
import com.cookiesstore.common.entities.OrderItem;
import com.cookiesstore.common.entities.OrderStatus;
import com.cookiesstore.common.entities.ProductSource;
import com.cookiesstore.common.repositories.CustomerAddressRepository;
import com.cookiesstore.common.repositories.CustomerRepository;
import com.cookiesstore.common.repositories.ProductSourceRepository;
import com.cookiesstore.common.services.orders.OrderService;
import com.cookiesstore.pos.dto.CreateOrderForm;
import com.cookiesstore.pos.dto.CreateOrderLineForm;
import com.cookiesstore.pos.repository.PosOrderHistoryRepository;
import com.cookiesstore.pos.repository.PosOrderLinkRepository;
import com.cookiesstore.pos.repository.PosSessionRepository;
import com.cookiesstore.pos.repository.PosSourceRepository;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PosOrderService {

    private final OrderService orderService;
    private final CustomerRepository customerRepository;
    private final CustomerAddressRepository customerAddressRepository;
    private final PosSourceRepository sourceRepository;
    private final PosSessionRepository posSessionRepository;
    private final PosOrderLinkRepository posOrderLinkRepository;
    private final PosOrderHistoryRepository posOrderHistoryRepository;
    private final ProductSourceRepository productSourceRepository;

    public PosOrderService(
        OrderService orderService,
        CustomerRepository customerRepository,
        CustomerAddressRepository customerAddressRepository,
        PosSourceRepository sourceRepository,
        PosSessionRepository posSessionRepository,
        PosOrderLinkRepository posOrderLinkRepository,
        PosOrderHistoryRepository posOrderHistoryRepository,
        ProductSourceRepository productSourceRepository
    ) {
        this.orderService = orderService;
        this.customerRepository = customerRepository;
        this.customerAddressRepository = customerAddressRepository;
        this.sourceRepository = sourceRepository;
        this.posSessionRepository = posSessionRepository;
        this.posOrderLinkRepository = posOrderLinkRepository;
        this.posOrderHistoryRepository = posOrderHistoryRepository;
        this.productSourceRepository = productSourceRepository;
    }

    public Order createAndHoldOrder(Long sourceId, CreateOrderForm form) 
    {
        var order = createOrder(sourceId, form);
        orderService.updateOrderStatus(order, OrderStatus.HOLD);
        return order;
    }

    public Order createOrder(Long sourceId, CreateOrderForm form) {
        if (form.getResumeOrderId() != null) {
            return transitionResumedOrderToPending(sourceId, form.getResumeOrderId());
        }

        if (form.getCustomerId() == null) {
            throw new PosOrderDomainException("pos.order.customer.required");
        }

        Customer customer = customerRepository.findById(form.getCustomerId())
            .orElseThrow(() -> new PosOrderDomainException("pos.order.customer.required"));

        CustomerAddress customerAddress = customerAddressRepository
            .findByCustomerIdAndLatestTrueOrderByIdDesc(customer.getId())
            .stream()
            .findFirst()
            .orElse(null);

        List<OrderAddressForm> addresses = List.of(
            toOrderAddressForm(OrderAddressType.SHIPPING, customer, customerAddress),
            toOrderAddressForm(OrderAddressType.BILLING, customer, customerAddress)
        );

        List<OrderItemForm> products = form.getLines().stream()
            .map(line -> toOrderItemForm(line, sourceId))
            .toList();

        var commonForm = new com.cookiesstore.common.dto.orders.CreateOrderForm(
            null,
            OrderStatus.PENDING.name(),
            false,
            customer.getId(),
            customerAddress != null ? customerAddress.getId() : 0L,
            customer.getEmail(),
            extractFirstName(customer.getName()),
            extractLastName(customer.getName()),
            null,
            products.size(),
            form.getCurrencyCode(),
            products,
            addresses
        );

        Order createdOrder = orderService.putOrder(commonForm);
        var source = sourceRepository.findById(sourceId)
            .orElseThrow(() -> new PosOrderDomainException("pos.order.source.required"));

        AdminSourcePosOrder posOrderLink = new AdminSourcePosOrder();
        posOrderLink.setSource(source);
        posOrderLink.setOrder(createdOrder);
        posSessionRepository.findBySourceIdAndSessionDate(sourceId, LocalDate.now())
            .filter(session -> session.getClosedByAdminUserId() == null)
            .ifPresent(posOrderLink::setPosSession);
        posOrderLinkRepository.save(posOrderLink);

        return createdOrder;
    }

    public Order payHeldOrder(Long sourceId, Long orderId) {
        Order order = requireOrderInSource(sourceId, orderId);
        if (order.getStatus() != OrderStatus.HOLD) {
            throw new PosOrderDomainException("pos.order.hold.invalidStatus");
        }
        orderService.updateOrderStatus(order, OrderStatus.PENDING);
        return order;
    }

    public void cancelHeldOrder(Long sourceId, Long orderId) {
        Order order = requireOrderInSource(sourceId, orderId);
        if (order.getStatus() != OrderStatus.HOLD) {
            throw new PosOrderDomainException("pos.order.hold.invalidStatus");
        }
        orderService.updateOrderStatus(order, OrderStatus.CANCELED);
    }

    public ResumeDecision evaluateResume(Long sourceId, Long orderId) {
        Order order = requireOrderInSource(sourceId, orderId);
        if (order.getStatus() != OrderStatus.HOLD) {
            throw new PosOrderDomainException("pos.order.hold.invalidStatus");
        }

        List<OrderItem> items = posOrderHistoryRepository.findItemsByOrderIdAndSourceId(orderId, sourceId);
        if (items.isEmpty()) {
            throw new PosOrderDomainException("pos.order.resume.empty");
        }

        var stockByProductId = stockByProduct(sourceId, items);
        List<ResumeLine> allLines = items.stream()
            .map(item -> toResumeLine(item, stockByProductId.getOrDefault(item.getProduct().getId(), 0)))
            .toList();

        List<ResumeLine> outOfStock = allLines.stream()
            .filter(line -> line.availableQuantity() < line.quantity())
            .toList();
        if (outOfStock.isEmpty()) {
            return new ResumeDecision(orderId, order.getCustomer() != null ? order.getCustomer().getId() : null, allLines, List.of(), true);
        }

        List<ResumeLine> availableOnly = allLines.stream()
            .filter(line -> line.availableQuantity() > 0)
            .map(line -> new ResumeLine(
                line.productId(),
                line.productName(),
                Math.min(line.quantity(), line.availableQuantity()),
                line.unitPriceMinor(),
                line.availableQuantity()
            ))
            .toList();

        return new ResumeDecision(orderId, order.getCustomer() != null ? order.getCustomer().getId() : null, availableOnly, outOfStock, false);
    }

    public ResumeDraft resumeWithAvailableItems(Long sourceId, Long orderId) {
        ResumeDecision decision = evaluateResume(sourceId, orderId);
        if (decision.linesToResume().isEmpty()) {
            throw new PosOrderDomainException("pos.order.resume.noStock");
        }
        return new ResumeDraft(decision.orderId(), decision.customerId(), decision.linesToResume());
    }

    public ResumeDraft resumeAllItems(Long sourceId, Long orderId) {
        ResumeDecision decision = evaluateResume(sourceId, orderId);
        if (!decision.canResumeAll()) {
            throw new PosOrderDomainException("pos.order.resume.stockConflict");
        }
        return new ResumeDraft(decision.orderId(), decision.customerId(), decision.linesToResume());
    }

    private Order transitionResumedOrderToPending(Long sourceId, Long orderId) {
        Order order = requireOrderInSource(sourceId, orderId);
        if (order.getStatus() == OrderStatus.HOLD) {
            orderService.updateOrderStatus(order, OrderStatus.PENDING);
            return order;
        }
        return order;
    }

    private Map<Long, Integer> stockByProduct(Long sourceId, List<OrderItem> items) {
        List<Long> productIds = items.stream()
            .map(OrderItem::getProduct)
            .filter(product -> product != null && product.getId() != null)
            .map(product -> product.getId())
            .distinct()
            .toList();
        List<ProductSource> productSources = productSourceRepository.findBySourceIdAndProductIdIn(sourceId, productIds);
        Map<Long, Integer> byProduct = new HashMap<>();
        for (ProductSource productSource : productSources) {
            byProduct.put(productSource.getProduct().getId(), productSource.getStockQuantity());
        }
        return byProduct;
    }

    private ResumeLine toResumeLine(OrderItem item, int availableQty) {
        if (item.getProduct() == null || item.getProduct().getId() == null) {
            throw new PosOrderDomainException("pos.order.resume.invalidProduct");
        }
        return new ResumeLine(
            item.getProduct().getId(),
            item.getProductName(),
            item.getQuantityOrdered() == null ? 0 : item.getQuantityOrdered(),
            item.getUnitPriceMinor() == null ? 0L : item.getUnitPriceMinor(),
            Math.max(availableQty, 0)
        );
    }

    private Order requireOrderInSource(Long sourceId, Long orderId) {
        return posOrderLinkRepository.findBySourceIdAndOrderId(sourceId, orderId)
            .map(AdminSourcePosOrder::getOrder)
            .orElseThrow(() -> new PosOrderDomainException("pos.order.notFound"));
    }

    public record ResumeDraft(
        Long orderId,
        Long customerId,
        List<ResumeLine> lines
    ) {
    }

    public record ResumeLine(
        Long productId,
        String productName,
        int quantity,
        long unitPriceMinor,
        int availableQuantity
    ) {
    }

    public record ResumeDecision(
        Long orderId,
        Long customerId,
        List<ResumeLine> linesToResume,
        List<ResumeLine> outOfStockLines,
        boolean canResumeAll
    ) {
    }

    private OrderItemForm toOrderItemForm(CreateOrderLineForm line, Long sourceId) {
        return new OrderItemForm(
            null,
            line.getProductId(),
            sourceId,
            line.getQuantity(),
            null
        );
    }

    private OrderAddressForm toOrderAddressForm(
        OrderAddressType type,
        Customer customer,
        CustomerAddress sourceAddress
    ) {
        if (sourceAddress != null) {
            return new OrderAddressForm(
                type,
                sourceAddress.getFirstName(),
                sourceAddress.getLastName(),
                sourceAddress.getGender(),
                sourceAddress.getCompanyName(),
                sourceAddress.getAddress1(),
                sourceAddress.getAddress2(),
                sourceAddress.getCity(),
                sourceAddress.getState(),
                sourceAddress.getCountry(),
                sourceAddress.getPostcode(),
                sourceAddress.getEmail() != null ? sourceAddress.getEmail() : customer.getEmail(),
                sourceAddress.getPhone() != null ? sourceAddress.getPhone() : customer.getPhone(),
                sourceAddress.getVatId()
            );
        }

        return new OrderAddressForm(
            type,
            extractFirstName(customer.getName()),
            extractLastName(customer.getName()),
            null,
            null,
            "N/A",
            null,
            "N/A",
            null,
            "N/A",
            null,
            customer.getEmail(),
            customer.getPhone(),
            null
        );
    }

    private String extractFirstName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "Customer";
        }
        String trimmed = fullName.trim();
        int firstSpace = trimmed.indexOf(' ');
        return firstSpace > 0 ? trimmed.substring(0, firstSpace) : trimmed;
    }

    private String extractLastName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "Unknown";
        }
        String trimmed = fullName.trim();
        int firstSpace = trimmed.indexOf(' ');
        return firstSpace > 0 ? trimmed.substring(firstSpace + 1).trim() : "Unknown";
    }
}
