package net.bfsr.engine.renderer.font;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.bfsr.engine.renderer.font.glyph.GlyphsBuilder;

import java.util.Objects;

@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GlyphKey {
    private GlyphsBuilder glyphsBuilder;
    private int fontSize;
    private char codepoint;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GlyphKey glyphKey = (GlyphKey) o;
        return fontSize == glyphKey.fontSize && codepoint == glyphKey.codepoint &&
                Objects.equals(glyphsBuilder, glyphKey.glyphsBuilder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(glyphsBuilder, fontSize, codepoint);
    }
}
