package net.bfsr.engine.renderer.font;

@FunctionalInterface
public interface IStringXOffsetSupplier {
    int get(String string, StringCache stringCache);
}