package net.bfsr.engine.renderer.font;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import net.bfsr.engine.logic.ClientGameLogic;
import net.bfsr.engine.renderer.font.glyph.Font;
import net.bfsr.engine.renderer.font.stb.STBTrueTypeFont;

import java.util.ArrayList;
import java.util.List;

public class FontManager implements AbstractFontManager {
    private final Object2ObjectMap<String, Font> fontsMap = new Object2ObjectOpenHashMap<>();
    @Getter
    private final List<Font> fonts = new ArrayList<>();

    @Override
    public void registerFont(ClientGameLogic client, String fontName, String fontFile) {
        if (fontsMap.containsKey(fontName)) {
            throw new IllegalArgumentException("Font with name " + fontName + " already registered");
        }

        STBTrueTypeFont glyphsBuilder = new STBTrueTypeFont(fontFile);
        fontsMap.put(fontName, glyphsBuilder);
        fonts.add(glyphsBuilder);
    }

    @Override
    public Font getFont(String fontName) {
        return fontsMap.get(fontName);
    }

    @Override
    public void clear() {
        for (int i = 0; i < fonts.size(); i++) {
            fonts.get(i).clear();
        }

        fontsMap.clear();
        fonts.clear();
    }
}
