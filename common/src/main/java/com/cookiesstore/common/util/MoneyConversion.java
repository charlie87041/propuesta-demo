package com.cookiesstore.common.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class MoneyConversion {

    private MoneyConversion() {
    }

    public static long toMinor(BigDecimal amount, int fractionDigits) {
        int resolvedFractionDigits = Math.max(fractionDigits, 0);
        try {
            return amount.movePointRight(resolvedFractionDigits)
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("Amount exceeds supported money range", ex);
        }
    }

    public static BigDecimal toMajor(long amountMinor, int fractionDigits) {
        return BigDecimal.valueOf(amountMinor, Math.max(fractionDigits, 0));
    }
}
