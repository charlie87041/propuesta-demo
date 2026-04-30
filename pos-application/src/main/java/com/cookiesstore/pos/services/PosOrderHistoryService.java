package com.cookiesstore.pos.services;

import com.cookiesstore.common.entities.AdminSourcePosOrder;
import com.cookiesstore.common.entities.Order;
import com.cookiesstore.common.entities.OrderItem;
import com.cookiesstore.common.entities.OrderStatus;
import com.cookiesstore.pos.dto.PosOrderDetailItemView;
import com.cookiesstore.pos.dto.PosOrderDetailsView;
import com.cookiesstore.pos.dto.PosOrderHistoryRowView;
import com.cookiesstore.pos.repository.PosOrderHistoryRepository;


import java.text.NumberFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class PosOrderHistoryService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");

    private final PosOrderHistoryRepository repository;

    public PosOrderHistoryService(PosOrderHistoryRepository repository) {
        this.repository = repository;
    }

    public PosOrderHistoryPage getHistory(Long sourceId, int page, int size, String search, String statusRaw, Locale locale) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        OrderStatus status = parseStatus(statusRaw);
        String trimmedSearch = StringUtils.hasText(search) ? search.trim() : null;

        var result = repository.findHistoryPage(
            sourceId,
            status,
            trimmedSearch,
            PageRequest.of(safePage, safeSize)
        );

        List<PosOrderHistoryRowView> rows = result.getContent()
            .stream()
            .map(order -> toRow(order, locale))
            .toList();
        String currencyCode = resolveCurrencyCode(result.getContent());

        long totalOrders = repository.countAllBySource(sourceId);
        long totalSalesMinor = repository.sumGrandTotalBySource(sourceId);
        long totalItemsSold = repository.sumTotalQtyBySource(sourceId);
        Double avgOrderMinor = repository.avgGrandTotalBySource(sourceId);

        return new PosOrderHistoryPage(
            rows,
            result.getNumber(),
            result.getSize(),
            result.getTotalElements(),
            result.getTotalPages(),
            totalOrders,
            formatMoney(totalSalesMinor, locale),
            formatMoney(avgOrderMinor == null ? 0d : avgOrderMinor, locale),
            currencyCode,
            totalItemsSold
        );
    }

    @Transactional(readOnly = true)
    public AdminSourcePosOrder findOrderPosSource(Long orderId) {
        return repository.findOrderPosSource(orderId).get();
    }

     

    public PosOrderDetailsView getOrderDetails(Long sourceId, Long orderId, Locale locale) {
        Order order = repository.findByIdAndSourceId(orderId, sourceId)
            .orElseThrow(() -> new PosOrderDomainException("pos.order.notFound"));

        List<OrderItem> items = repository.findItemsByOrderIdAndSourceId(orderId, sourceId);

        List<PosOrderDetailItemView> itemViews = items.stream()
            .map(item -> new PosOrderDetailItemView(
                item.getProductName(),
                item.getProductTypeCode() == null ? "N/A" : item.getProductTypeCode(),
                item.getQuantityOrdered() == null ? 0 : item.getQuantityOrdered(),
                formatMoney(item.getUnitPriceMinor() == null ? 0L : item.getUnitPriceMinor(), locale),
                formatMoney(item.getLineTotalMinor() == null ? 0L : item.getLineTotalMinor(), locale)
            ))
            .toList();

        String customerName = (order.getCustomerFirstName() == null ? "" : order.getCustomerFirstName()).trim();
        String customerLastName = (order.getCustomerLastName() == null ? "" : order.getCustomerLastName()).trim();
        String fullName = (customerName + " " + customerLastName).trim();
        if (!StringUtils.hasText(fullName)) {
            fullName = order.isGuest() ? "Guest" : "N/A";
        }

        return new PosOrderDetailsView(
            order.getId(),
            order.getIncrementId(),
            fullName,
            order.getStatus() == null ? "PENDING" : order.getStatus().name(),
            TIME_FORMATTER.format(order.getCreatedAt().atZone(ZoneId.systemDefault())),
            formatMoney(order.getGrandTotalMinor() == null ? 0L : order.getGrandTotalMinor(), locale),
            order.getOrderCurrency() != null && StringUtils.hasText(order.getOrderCurrency().getCode())
                ? order.getOrderCurrency().getCode()
                : "",
            itemViews.size(),
            itemViews
        );
    }

    private PosOrderHistoryRowView toRow(Order order, Locale locale) {
        String customerName = (order.getCustomerFirstName() == null ? "" : order.getCustomerFirstName()).trim();
        String customerLastName = (order.getCustomerLastName() == null ? "" : order.getCustomerLastName()).trim();
        String fullName = (customerName + " " + customerLastName).trim();
        if (!StringUtils.hasText(fullName)) {
            fullName = order.isGuest() ? "Guest" : "N/A";
        }

        int qty = order.getTotalQtyOrdered() == null ? 0 : order.getTotalQtyOrdered();
        String itemsLabel = qty + (qty == 1 ? " item" : " items");
        String statusLabel = order.getStatus() != null ? order.getStatus().name() : "PENDING";
        String currencyCode = order.getOrderCurrency() != null && order.getOrderCurrency().getCode() != null
            ? order.getOrderCurrency().getCode()
            : "";
        String totalAmount = formatMoney(order.getGrandTotalMinor() == null ? 0L : order.getGrandTotalMinor(), locale)
            + (currencyCode.isBlank() ? "" : " " + currencyCode);

        return new PosOrderHistoryRowView(
            order.getId(),
            order.getIncrementId(),
            TIME_FORMATTER.format(order.getCreatedAt().atZone(ZoneId.systemDefault())),
            fullName,
            itemsLabel,
            totalAmount,
            statusLabel,
            statusTone(statusLabel)
        );
    }

    private String statusTone(String statusLabel) {
        return switch (statusLabel) {
            case "COMPLETED", "CLOSED" -> "success";
            case "HOLD" -> "warning";
            case "PROCESSING" -> "info";
            case "CANCELED" -> "danger";
            default -> "neutral";
        };
    }

    private OrderStatus parseStatus(String statusRaw) {
        if (!StringUtils.hasText(statusRaw) || "ALL".equalsIgnoreCase(statusRaw)) {
            return null;
        }
        try {
            return OrderStatus.valueOf(statusRaw.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private String formatMoney(long minor, Locale locale) {
        NumberFormat format = NumberFormat.getCurrencyInstance(locale == null ? Locale.US : locale);
        return format.format(minor / 100d);
    }

    private String formatMoney(double minor, Locale locale) {
        NumberFormat format = NumberFormat.getCurrencyInstance(locale == null ? Locale.US : locale);
        return format.format(minor / 100d);
    }

    private String resolveCurrencyCode(List<Order> orders) {
        return orders.stream()
            .map(Order::getOrderCurrency)
            .filter(currency -> currency != null && StringUtils.hasText(currency.getCode()))
            .map(currency -> currency.getCode())
            .findFirst()
            .orElse("");
    }

    public record PosOrderHistoryPage(
        List<PosOrderHistoryRowView> rows,
        int page,
        int size,
        long totalElements,
        int totalPages,
        long totalOrders,
        String totalSales,
        String averageOrder,
        String currencyCode,
        long itemsSold
    ) {
    }
}
