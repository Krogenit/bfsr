package net.bfsr.engine.renderer.font.glyph;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GlyphKey {
    private Font font;
    private int fontSize;
    private char codepoint;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GlyphKey glyphKey = (GlyphKey) o;
        return fontSize == glyphKey.fontSize && codepoint == glyphKey.codepoint &&
                Objects.equals(font, glyphKey.font);
    }

    @Override
    public int hashCode() {
        return Objects.hash(font, fontSize, codepoint);
    }
}
