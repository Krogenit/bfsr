package net.bfsr.engine.renderer.font.glyph;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.RequiredArgsConstructor;
import net.bfsr.engine.Engine;
import net.bfsr.engine.renderer.font.FontPacker;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

@RequiredArgsConstructor
public abstract class DynamicFont<FONT_PACKER extends FontPacker<?>> extends Font {
    protected final String fontFile;

    protected final int bitmapWidth = 256;
    protected final int bitmapHeight = 256;

    private final Int2ObjectMap<FONT_PACKER> fontsPackerBySize = new Int2ObjectOpenHashMap<>();

    @Nullable
    protected <T extends DynamicFont<FONT_PACKER>> T findFontSupportedChar(char charCode, Predicate<Font> predicate) {
        List<Font> fonts = Engine.getFontManager().getFonts();
        for (int i = 0; i < fonts.size(); i++) {
            Font font = fonts.get(i);
            if (font instanceof DynamicFont<?> dynamicFont && predicate.test(dynamicFont)) {
                if (dynamicFont.isCharCodeSupported(charCode)) {
                    return (T) font;
                }
            }
        }

        return null;
    }

    protected FONT_PACKER getFontPackerBySize(int fontSize) {
        FONT_PACKER stbTrueTypeFontPacker = fontsPackerBySize.get(fontSize);
        if (stbTrueTypeFontPacker == null) {
            stbTrueTypeFontPacker = createNewFontPacker(fontSize);
            stbTrueTypeFontPacker.init();
            fontsPackerBySize.put(fontSize, stbTrueTypeFontPacker);
        }

        return stbTrueTypeFontPacker;
    }

    protected abstract FONT_PACKER createNewFontPacker(int fontSize);
    public abstract boolean isCharCodeSupported(char charCode);

    @Override
    public void clear() {
        fontsPackerBySize.forEach((integer, fontPacker) -> fontPacker.clear());
        fontsPackerBySize.clear();
    }
}
