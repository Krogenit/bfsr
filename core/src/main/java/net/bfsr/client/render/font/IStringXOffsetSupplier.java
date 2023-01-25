package net.bfsr.client.render.font;

@FunctionalInterface
public interface IStringXOffsetSupplier {
    float get(String string, StringCache stringCache);
}