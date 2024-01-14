package net.bfsr.engine.renderer.font;

@FunctionalInterface
public interface StringXOffsetSupplier {
    int get(String string, StringCache stringCache);
}