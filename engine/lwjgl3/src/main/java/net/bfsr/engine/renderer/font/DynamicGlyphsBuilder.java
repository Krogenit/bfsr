package net.bfsr.engine.renderer.font;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.RequiredArgsConstructor;
import net.bfsr.engine.renderer.font.glyph.GlyphsBuilder;

import javax.annotation.Nullable;
import java.util.function.Predicate;

@RequiredArgsConstructor
public abstract class DynamicGlyphsBuilder<FONT extends FontPacker<?>> extends GlyphsBuilder {
    protected final String fontFile;

    protected final int bitmapWidth = 256;
    protected final int bitmapHeight = 256;

    private final Int2ObjectMap<FONT> fontsBySize = new Int2ObjectOpenHashMap<>();

    @Nullable
    protected <T extends DynamicGlyphsBuilder<FONT>> T findFontSupportedChar(char charCode, Predicate<GlyphsBuilder> predicate) {
        Font[] fonts = Font.values();
        for (int i = 0; i < fonts.length; i++) {
            GlyphsBuilder glyphsBuilder = fonts[i].getGlyphsBuilder();
            if (glyphsBuilder instanceof DynamicGlyphsBuilder<?> dynamicGlyphsBuilder && predicate.test(dynamicGlyphsBuilder)) {
                if (dynamicGlyphsBuilder.isCharCodeSupported(charCode)) {
                    return (T) glyphsBuilder;
                }
            }
        }

        return null;
    }

    protected FONT getFontBySize(int fontSize) {
        FONT stbTrueTypeFont = fontsBySize.get(fontSize);
        if (stbTrueTypeFont == null) {
            stbTrueTypeFont = createNewFont(fontSize);
            stbTrueTypeFont.init();
            fontsBySize.put(fontSize, stbTrueTypeFont);
        }

        return stbTrueTypeFont;
    }

    protected abstract FONT createNewFont(int fontSize);
    public abstract boolean isCharCodeSupported(char charCode);
}
