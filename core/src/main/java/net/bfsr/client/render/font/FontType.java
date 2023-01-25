package net.bfsr.client.render.font;

public enum FontType {
    DEFAULT(new StringCache()),
    XOLONIUM(new StringCache("Xolonium-Regular.ttf", true)),
    CONSOLA(new StringCache("consola.ttf", true));

    private final StringCache stringCache;

    FontType(StringCache stringCache) {
        this.stringCache = stringCache;
    }

    public StringCache getStringCache() {
        return stringCache;
    }
}
