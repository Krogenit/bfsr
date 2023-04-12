package net.bfsr.client.renderer.font;

@FunctionalInterface
public interface IStringXOffsetSupplier {
    int get(String string, StringCache stringCache);
}