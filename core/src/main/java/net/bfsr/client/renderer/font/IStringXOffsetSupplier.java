package net.bfsr.client.renderer.font;

@FunctionalInterface
public interface IStringXOffsetSupplier {
    float get(String string, StringCache stringCache);
}