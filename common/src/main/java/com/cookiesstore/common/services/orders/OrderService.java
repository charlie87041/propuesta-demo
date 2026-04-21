package com.cookiesstore.common.services.orders;

import com.cookiesstore.common.dto.orders.CreateOrderForm;
import com.cookiesstore.common.dto.orders.OrderAddressForm;
import com.cookiesstore.common.dto.orders.OrderItemForm;
import com.cookiesstore.common.entities.Currency;
import com.cookiesstore.common.entities.Customer;
import com.cookiesstore.common.entities.Order;
import com.cookiesstore.common.entities.OrderAddress;
import com.cookiesstore.common.entities.OrderAddressType;
import com.cookiesstore.common.entities.OrderItem;
import com.cookiesstore.common.entities.OrderStatus;
import com.cookiesstore.common.entities.OrderStatusHistory;
import com.cookiesstore.common.entities.Price;
import com.cookiesstore.common.entities.Product;
import com.cookiesstore.common.entities.ProductSource;
import com.cookiesstore.common.entities.Source;
import com.cookiesstore.common.events.OrderUpdated;
import com.cookiesstore.common.repositories.CurrencyRepository;
import com.cookiesstore.common.repositories.CustomerRepository;
import com.cookiesstore.common.repositories.OrderAddressRepository;
import com.cookiesstore.common.repositories.OrderItemRepository;
import com.cookiesstore.common.repositories.OrderRepository;
import com.cookiesstore.common.repositories.OrderStatusHistoryRepository;
import com.cookiesstore.common.repositories.ProductRepository;
import com.cookiesstore.common.repositories.SourceRepository;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.util.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
@Transactional
public class OrderService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final CurrencyRepository currencyRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderAddressRepository orderAddressRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final SourceRepository sourceRepository;

    public OrderService(
        ProductRepository productRepository,
        OrderRepository orderRepository,
        CustomerRepository customerRepository,
        CurrencyRepository currencyRepository,
        OrderItemRepository orderItemRepository,
        OrderAddressRepository orderAddressRepository,
        OrderStatusHistoryRepository orderStatusHistoryRepository,
        ApplicationEventPublisher applicationEventPublisher,
        SourceRepository sourceRepository
    ) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.currencyRepository = currencyRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderAddressRepository = orderAddressRepository;
        this.orderStatusHistoryRepository = orderStatusHistoryRepository;
        this.applicationEventPublisher = applicationEventPublisher;
        this.sourceRepository = sourceRepository;
    }

    public Order putOrder(CreateOrderForm form) {
        if (form.id() == null) {
            if (CollectionUtils.isEmpty(form.products())) {
                throw new OrderItemsRequiredException();
            }
            OrderContext context = OrderContext.buildCreateContext(form, this);
            return createOrder(context, form);
        }
        if (CollectionUtils.isEmpty(form.products())) {
            Order order = orderRepository.findById(form.id())
                .orElseThrow(() -> new OrderNotFoundException(form.id()));
            orderRepository.delete(order);
            return null;
        }
        OrderContext context = OrderContext.buildUpdateContext(form, this);
        return updateOrderProducts(context, form);
    }

    private Order updateOrderProducts(OrderContext context, CreateOrderForm form) {
        Order order = context.order;
        var customer = context.customer;
        List<OrderItem> incomingItems = context.incomingItems;
        Set<Long> incomingIds = context.incomingIds;

        orderItemRepository.findByOrderId(order.getId())
            .stream()
            .filter(existing -> existing.getId() != null && !incomingIds.contains(existing.getId()))
            .forEach(orderItemRepository::delete);

        incomingItems.forEach(item -> item.setOrder(order));
        orderItemRepository.saveAll(incomingItems);

        applyOrderTotals(order, incomingItems);
        order.setCustomer(customer);
        orderRepository.save(order);
        applyAddressesIfProvided(order, form.addresses());
        this.applicationEventPublisher.publishEvent(
            new OrderUpdated(order.getId(), order.getStatus(), buildOrderItemSnapshots(incomingItems))
        );

        return orderRepository.findById(order.getId())
            .orElseThrow(() -> new OrderNotFoundException(order.getId()));
    }

    private Order createOrder(OrderContext context, CreateOrderForm form) {
        var customer = context.customer;
        List<OrderItem> orderItems = context.incomingItems;
        Currency currency = context.currency;
        
        Order order = new Order();
        order.setIncrementId("T-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8));
        order.setStatus(OrderStatus.PENDING);
        order.setGuest(form.isGuest());
        order.setCustomer(customer);
        order.setCustomerEmail(form.customerEmail());
        order.setCustomerFirstName(form.customerFirstName());
        order.setCustomerLastName(form.customerLastName());
        order.setCouponCode(form.couponCode());
        order.setOrderCurrency(currency);
        applyOrderTotals(order, orderItems);
        order.setDiscountTotalMinor(0L);
        order.setTaxTotalMinor(0L);
        order.setShippingTotalMinor(0L);
        orderRepository.save(order);

        orderItems.forEach(item -> item.setOrder(order));
        orderItemRepository.saveAll(orderItems);

	    applyAddressesIfProvided(order, form.addresses());
        updateNewOrderStatus(order);

        this.applicationEventPublisher.publishEvent(
            new OrderUpdated(order.getId(), order.getStatus(), buildOrderItemSnapshots(orderItems))
        );

        return orderRepository.findById(order.getId())
            .orElseThrow(() -> new OrderNotFoundException(order.getId()));
    }

    private void updateNewOrderStatus(Order order)
    {
        OrderStatusHistory createdStatus = new OrderStatusHistory();
        createdStatus.setOrder(order);
        createdStatus.setFromStatus(null);
        createdStatus.setToStatus(OrderStatus.PENDING);
        orderStatusHistoryRepository.save(createdStatus);
    }

    private OrderAddress upsertOrderAddress(
        OrderAddressForm form,
        Order order,
        Map<OrderAddressType, OrderAddress> existingByType
    ) {
        OrderAddressType type = form.addressType();
        OrderAddress address = existingByType.get(type);
        if (address == null) {
            address = new OrderAddress();
        }

        address.setOrder(order);
        address.setAddressType(type);
        address.setFirstName(form.firstName().trim());
        address.setLastName(form.lastName().trim());
        address.setGender(trimToNull(form.gender()));
        address.setCompanyName(trimToNull(form.companyName()));
        address.setAddress1(form.address1().trim());
        address.setAddress2(trimToNull(form.address2()));
        address.setCity(form.city().trim());
        address.setState(trimToNull(form.state()));
        address.setCountry(trimToNull(form.country()));
        address.setPostcode(trimToNull(form.postcode()));
        address.setEmail(trimToNull(form.email()));
        address.setPhone(trimToNull(form.phone()));
        address.setVatId(trimToNull(form.vatId()));

        OrderAddress saved = orderAddressRepository.save(address);
        existingByType.put(type, saved);
        return saved;
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private void applyOrderTotals(Order order, List<OrderItem> items) {
        int totalItemCount = items.size();
        int totalQtyOrdered = items.stream().mapToInt(OrderItem::getQuantityOrdered).sum();
        long subTotalMinor = items.stream().mapToLong(OrderItem::getLineSubTotalMinor).sum();
        long discountMinor = items.stream().mapToLong(OrderItem::getLineDiscountMinor).sum();
        long taxMinor = items.stream().mapToLong(OrderItem::getLineTaxMinor).sum();
        long grandTotalMinor = subTotalMinor - discountMinor + taxMinor + order.getShippingTotalMinor();

        order.setTotalItemCount(totalItemCount);
        order.setTotalQtyOrdered(totalQtyOrdered);
        order.setSubTotalMinor(subTotalMinor);
        order.setDiscountTotalMinor(discountMinor);
        order.setTaxTotalMinor(taxMinor);
        order.setGrandTotalMinor(grandTotalMinor);
    }

    private OrderItem productItemFromForm(OrderItemForm form, Order order) {
        var orderId = order != null ? order.getId() : null;
        OrderItem item = resolveOrderItem(form, orderId);
        Product product = productRepository.findById(form.productId())
            .orElseThrow(() -> new OrderProductNotFoundException(form.productId()));

        Price currentPrice = resolveCurrentPrice(product, form.sourceId());
        String currencyCode = currentPrice.getCurrency();
        Currency currency = currencyRepository.findById(currencyCode)
            .orElseThrow(() -> new OrderCurrencyNotFoundException(currencyCode));

        long unitPriceMinor = currentPrice.getAmountMinor();
        Integer requestedQuantity = form.totalQtyOrdered();
        if (requestedQuantity == null || requestedQuantity <= 0) {
            throw new OrderItemQuantityInvalidException(form.productId());
        }
        int quantity = requestedQuantity;
        long lineSubTotalMinor = unitPriceMinor * quantity;

        Source source = sourceRepository.findById(form.sourceId()).orElseThrow();

        item.setProduct(product);
        item.setSku(product.getSku());
        item.setProductName(product.getName());
        item.setProductTypeCode(product.getProductTypeCode());
        item.setQuantityOrdered(quantity);
        item.setQuantityShipped(0);
        item.setQuantityInvoiced(0);
        item.setQuantityCanceled(0);
        item.setQuantityRefunded(0);
        item.setUnitPriceMinor(unitPriceMinor);
        item.setLineSubTotalMinor(lineSubTotalMinor);
        item.setLineDiscountMinor(0L);
        item.setLineTaxMinor(0L);
        item.setLineTotalMinor(lineSubTotalMinor);
        item.setCurrency(currency);
        item.setSource(source);
        return item;
    }

    private OrderItem resolveOrderItem(OrderItemForm form, Long orderId) {
        if (form.orderItemId() == null) {
            return new OrderItem();
        }

        OrderItem item = orderItemRepository.findById(form.orderItemId())
            .orElseThrow(() -> new OrderItemNotFoundException(form.orderItemId()));
        if (orderId != null && item.getOrder() != null && !Objects.equals(item.getOrder().getId(), orderId)) {
            throw new OrderItemOwnershipException(orderId, form.orderItemId());
        }
        return item;
    }

    private Price resolveCurrentPrice(Product product, Long sourceId) {
        Price productPrice = product.getCurrentPrice();
        ProductSource source = product.productSources.stream()
            .filter(ps -> Objects.equals(ps.getSource().getId(), sourceId))
            .findFirst()
            .orElseThrow(() -> new OrderSourceNotFoundException(product.getId(), sourceId));
        Price sourcePrice = source.getPrice();
        Price currentPrice = Objects.requireNonNullElse(sourcePrice, productPrice);
        if (currentPrice == null) {
            throw new OrderPriceNotFoundException(product.getId(), sourceId);
        }
        return currentPrice;
    }

    private void applyAddressesIfProvided(Order order, List<OrderAddressForm> addresses) {
        if (addresses == null || addresses.isEmpty()) {
            return;
        }
        validateAddressesCount(addresses);
        Map<OrderAddressType, OrderAddress> existingByType = orderAddressRepository.findByOrderId(order.getId())
            .stream()
            .collect(Collectors.toMap(OrderAddress::getAddressType, address -> address, (left, right) -> left, () -> new EnumMap<>(OrderAddressType.class)));
        addresses.forEach(addressForm -> upsertOrderAddress(addressForm, order, existingByType));
    }

    private void validateAddressesCount(List<OrderAddressForm> addresses) {
        int addressCount = addresses == null ? 0 : addresses.size();
        if (addressCount != 2) {
            throw new OrderAddressesInvalidException(addressCount);
        }
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



    private static final class OrderContext {
        private final Order order;
        private final Customer customer;
        private final List<OrderItem> incomingItems;
        private final Set<Long> incomingIds;
        private final Currency currency;

        private OrderContext(
            Order order,
            Customer customer,
            List<OrderItem> incomingItems,
            Set<Long> incomingIds,
            Currency currency
        ) {
            this.order = order;
            this.customer = customer;
            this.incomingItems = incomingItems;
            this.incomingIds = incomingIds;
            this.currency = currency;
        }

        private static OrderContext buildCreateContext(CreateOrderForm form, OrderService parent) {
            Customer customer = parent.customerRepository.findById(form.customerId())
                .orElseThrow(() -> new OrderCustomerNotFoundException(form.customerId()));
            List<OrderItem> incomingItems = form.products()
                .stream()
                .map(orderProduct -> parent.productItemFromForm(orderProduct, null))
                .toList();
            Set<Long> incomingIds = incomingItems.stream()
                .map(OrderItem::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
            Currency currency = parent.currencyRepository.findById(form.orderCurrencyCode())
                .orElseThrow(() -> new OrderCurrencyNotFoundException(form.orderCurrencyCode()));
            parent.validateAddressesCount(form.addresses());
            return new OrderContext(null, customer, incomingItems, incomingIds, currency);
        }

        private static OrderContext buildUpdateContext(CreateOrderForm form, OrderService parent) {
            Order order = parent.orderRepository.findById(form.id())
                .orElseThrow(() -> new OrderNotFoundException(form.id()));
            Customer customer = parent.customerRepository.findById(form.customerId())
                .orElseThrow(() -> new OrderCustomerNotFoundException(form.customerId()));
            List<OrderItem> incomingItems = form.products()
                .stream()
                .map(orderProduct -> parent.productItemFromForm(orderProduct, order))
                .toList();
            Set<Long> incomingIds = incomingItems.stream()
                .map(OrderItem::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
            return new OrderContext(order, customer, incomingItems, incomingIds, null);
        }
    }
}
