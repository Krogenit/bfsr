package net.bfsr.engine.renderer.font;

import net.bfsr.engine.renderer.font.glyph.GlyphsBuilder;

@FunctionalInterface
public interface StringXOffsetSupplier {
    int get(String string, GlyphsBuilder glyphsBuilder, int fontSize);
}