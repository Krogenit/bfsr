package net.bfsr.util;

import java.text.DecimalFormat;

public final class DecimalUtils {
    private static final DecimalFormat FORMATTER = new DecimalFormat("0.00");

    public static String formatWithToDigits(float value) {
        return FORMATTER.format(value);
    }
}
