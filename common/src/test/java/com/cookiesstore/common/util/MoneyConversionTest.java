package com.cookiesstore.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class MoneyConversionTest {

    @Test
    void convertsUsdAmountToMinorAndBack() {
        BigDecimal amount = new BigDecimal("10.99");

        long amountMinor = MoneyConversion.toMinor(amount, 2);
        BigDecimal converted = MoneyConversion.toMajor(amountMinor, 2);

        assertEquals(1099L, amountMinor);
        assertEquals(new BigDecimal("10.99"), converted);
    }

    @Test
    void convertsThreeDigitCurrencyAmountToMinorAndBack() {
        BigDecimal amount = new BigDecimal("10.125");

        long amountMinor = MoneyConversion.toMinor(amount, 3);
        BigDecimal converted = MoneyConversion.toMajor(amountMinor, 3);

        assertEquals(10125L, amountMinor);
        assertEquals(new BigDecimal("10.125"), converted);
    }
}
