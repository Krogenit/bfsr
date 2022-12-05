package net.bfsr.client.font_new;

public enum FontType {
    Default(new StringCache()),
    Xolonium(new StringCache("Xolonium-Regular.ttf", true));

    private final StringCache stringCache;

    FontType(StringCache stringCache) {
        this.stringCache = stringCache;
    }

    public StringCache getStringCache() {
        return stringCache;
    }
}
