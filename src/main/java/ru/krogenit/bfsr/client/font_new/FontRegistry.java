package ru.krogenit.bfsr.client.font_new;

public enum FontRegistry {

    Default(new StringCache()),

    Xolonium(new StringCache("Xolonium-Regular.ttf", 48, true));

    private final StringCache stringCache;

    FontRegistry(StringCache stringCache) {
        this.stringCache = stringCache;
    }

    public StringCache getStringCache() {
        return stringCache;
    }
}
