package net.bfsr.client.font_new;

@FunctionalInterface
public interface IStringXOffsetSupplier {
    float get(String s, StringCache stringCache);
}