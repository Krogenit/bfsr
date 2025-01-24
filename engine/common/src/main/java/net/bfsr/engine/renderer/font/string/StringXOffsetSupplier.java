package net.bfsr.engine.renderer.font.string;

import net.bfsr.engine.renderer.font.glyph.Font;

@FunctionalInterface
public interface StringXOffsetSupplier {
    int get(String string, Font font, int fontSize);
}