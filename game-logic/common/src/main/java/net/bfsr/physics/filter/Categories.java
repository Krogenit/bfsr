package net.bfsr.physics.filter;

public final class Categories {
    private static long category = 1;

    public static final long SHIP_CATEGORY = getNextCategory();

    public static final long BULLET_CATEGORY = getNextCategory();
    public static final long BEAM_CATEGORY = getNextCategory();

    public static final long WRECK_CATEGORY = getNextCategory();

    private static long getNextCategory() {
        long currentCategory = category;
        category <<= 1;
        return currentCategory;
    }

    public static long all() {
        return SHIP_CATEGORY | BULLET_CATEGORY | BEAM_CATEGORY | WRECK_CATEGORY;
    }
}