package com.cookiesstore.admin.web.controllers;

import com.cookiesstore.common.api.ApiResponse;
import com.cookiesstore.common.entities.Customer;
import com.cookiesstore.common.entities.CustomerAddress;
import com.cookiesstore.common.entities.Order;
import com.cookiesstore.common.entities.OrderAddress;
import com.cookiesstore.common.entities.OrderAddressType;
import com.cookiesstore.common.entities.OrderItem;
import com.cookiesstore.common.entities.OrderStatus;
import com.cookiesstore.common.entities.OrderStatusHistory;
import com.cookiesstore.common.entities.Price;
import com.cookiesstore.common.entities.Product;
import com.cookiesstore.common.events.OrderUpdated;
import com.cookiesstore.common.dto.orders.CreateOrderForm;
import com.cookiesstore.common.dto.orders.OrderAddressForm;
import com.cookiesstore.common.dto.orders.OrderItemForm;
import com.cookiesstore.common.repositories.CustomerAddressRepository;
import com.cookiesstore.common.repositories.CustomerRepository;
import com.cookiesstore.common.repositories.OrderAddressRepository;
import com.cookiesstore.common.repositories.OrderItemRepository;
import com.cookiesstore.common.repositories.OrderRepository;
import com.cookiesstore.common.repositories.OrderStatusHistoryRepository;
import com.cookiesstore.common.repositories.ProductRepository;
import com.cookiesstore.common.services.orders.OrderService;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderTestController {

    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final CustomerAddressRepository customerAddressRepository;
    private final OrderAddressRepository orderAddressRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final ApplicationEventPublisher applicationEventPublisher;

    public OrderTestController(
        ProductRepository productRepository,
        CustomerRepository customerRepository,
        CustomerAddressRepository customerAddressRepository,
        OrderAddressRepository orderAddressRepository,
        OrderItemRepository orderItemRepository,
        OrderStatusHistoryRepository orderStatusHistoryRepository,
        OrderService orderService,
        ApplicationEventPublisher applicationEventPublisher,
        OrderRepository orderRepository
    ) {
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.customerAddressRepository = customerAddressRepository;
        this.orderAddressRepository = orderAddressRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderStatusHistoryRepository = orderStatusHistoryRepository;
        this.orderService = orderService;
        this.orderRepository = orderRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @PostMapping("/public/api/dev/orders/bootstrap")
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, Object>>> createTestOrder() {
        Product product = productRepository.findAll(PageRequest.of(0, 50, Sort.by("id").ascending()))
            .stream()
            .filter(p -> p.productSources != null && !p.productSources.isEmpty())
            .filter(p -> p.getCurrentPrice() != null || p.productSources.stream().anyMatch(ps -> ps.getPrice() != null))
            .findFirst()
            .orElse(null);
        if (product == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("BOOTSTRAP_ORDER_ERROR", "No suitable products found (needs product source and a price)"));
        }

        Customer customer = customerRepository.findAll(PageRequest.of(0, 1, Sort.by("id").ascending()))
            .stream()
            .findFirst()
            .orElse(null);
        if (customer == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("BOOTSTRAP_ORDER_ERROR", "No customers found"));
        }

        CustomerAddress sourceAddress = customerAddressRepository.findByCustomerIdAndLatestTrueOrderByIdDesc(customer.getId())
            .stream()
            .findFirst()
            .orElse(null);

        var selectedSource = product.productSources.stream()
            .filter(ps -> ps.getPrice() != null)
            .findFirst()
            .orElse(product.productSources.stream().findFirst().orElse(null));
        if (selectedSource == null || selectedSource.getSource() == null || selectedSource.getSource().getId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("BOOTSTRAP_ORDER_ERROR", "Product has no usable source"));
        }

        Price effectivePrice = selectedSource.getPrice() != null ? selectedSource.getPrice() : product.getCurrentPrice();
        if (effectivePrice == null || effectivePrice.getCurrency() == null || effectivePrice.getCurrency().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("BOOTSTRAP_ORDER_ERROR", "Product has no usable price/currency"));
        }

        String currencyCode = effectivePrice.getCurrency();

        Long customerAddressId = sourceAddress != null ? sourceAddress.getId() : 0L;
        List<OrderAddressForm> addresses = List.of(
            toOrderAddressForm(OrderAddressType.SHIPPING, customer, sourceAddress),
            toOrderAddressForm(OrderAddressType.BILLING, customer, sourceAddress)
        );

        CreateOrderForm form = new CreateOrderForm(
            null,
            OrderStatus.PENDING.name(),
            false,
            customer.getId(),
            customerAddressId,
            customer.getEmail(),
            extractFirstName(customer.getName()),
            extractLastName(customer.getName()),
            null,
            1,
            currencyCode,
            List.of(new OrderItemForm(null, product.getId(), selectedSource.getSource().getId(), 1, null)),
            addresses
        );

        Order order = orderService.putOrder(form);
        order.setStatus(OrderStatus.COMPLETED);
        orderRepository.save(order);
         this.applicationEventPublisher.publishEvent(
            new OrderUpdated(order.getId(), order.getStatus(), buildOrderItemSnapshots(orderItemRepository.findByOrderId(order.getId())))
        );

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("orderId", order.getId());
        data.put("incrementId", order.getIncrementId());
        data.put("customerId", customer.getId());
        data.put("productId", product.getId());
        data.put("currency", currencyCode);
        data.put("grandTotalMinor", order.getGrandTotalMinor());

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(data));
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


    private List<OrderUpdated.OrderItemSnapshot> buildOrderItemSnapshots(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        return items.stream()
            .map(item -> new OrderUpdated.OrderItemSnapshot(
                item.getProduct() == null ? null : item.getProduct().getId(),
                item.getSource() == null ? null : item.getSource().getId(),
                item.getQuantityOrdered()
            ))
            .toList();
    }
}
