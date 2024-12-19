package net.bfsr.engine.renderer.font.string;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.engine.renderer.font.glyph.GlyphData;

import java.util.ArrayList;
import java.util.List;

@Getter
public class StringGeometry {
    @Setter
    private int width;
    private final List<GlyphData> glyphsData = new ArrayList<>();

    public void addGlyphData(GlyphData glyphData) {
        glyphsData.add(glyphData);
    }

    public int getGlyphsCount() {
        return glyphsData.size();
    }

    public void clear() {
        glyphsData.clear();
    }
}