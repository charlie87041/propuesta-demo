package com.cookiesstore.common.dto.orders;

import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CreateOrderForm(
    Long id,
    @NotBlank String status,
    boolean isGuest,
    @NotNull @Min(value = 1) Long customerId,
    @NotNull @Min(value = 1) Long customerAddressId,
    @NotBlank @Email String customerEmail,
    @NotBlank String customerFirstName,
    String customerLastName,
    String couponCode,
    @Min(value = 1) Integer totalItemCount,
    @NotBlank String orderCurrencyCode,
    @NotEmpty List<OrderItemForm> products,
    List<OrderAddressForm> addresses
     


){}
