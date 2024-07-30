package net.bfsr.engine.renderer.font.glyph;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class GlyphsData {
    private final List<Glyph> glyphs;
    private final int width;
}
