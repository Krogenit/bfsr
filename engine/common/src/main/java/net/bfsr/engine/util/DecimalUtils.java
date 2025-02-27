package net.bfsr.engine.util;

import java.text.DecimalFormat;

public final class DecimalUtils {
    private static final DecimalFormat FORMATTER = new DecimalFormat("#.##");
    private static final DecimalFormat STRICT_FORMATTER = new DecimalFormat("0.00");

    public static String formatWithToDigits(float value) {
        return FORMATTER.format(value);
    }

    public static String formatWithToDigits(double value) {
        return FORMATTER.format(value);
    }

    public static String strictFormatWithToDigits(float value) {
        return STRICT_FORMATTER.format(value);
    }

    public static String strictFormatWithToDigits(double value) {
        return STRICT_FORMATTER.format(value);
    }
}