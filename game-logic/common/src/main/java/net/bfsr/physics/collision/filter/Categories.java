package net.bfsr.physics.collision.filter;

final class Categories {
    private static long category = 1;

    static final long SHIP_CATEGORY = getNextCategory();

    static final long BULLET_CATEGORY = getNextCategory();
    static final long BEAM_CATEGORY = getNextCategory();

    private static long getNextCategory() {
        long currentCategory = category;
        category <<= 1;
        return currentCategory;
    }

    static long all() {
        return SHIP_CATEGORY | BULLET_CATEGORY | BEAM_CATEGORY;
    }
}