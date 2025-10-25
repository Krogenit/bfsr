package net.bfsr.engine.renderer.font;

import net.bfsr.engine.logic.ClientGameLogic;
import net.bfsr.engine.renderer.font.glyph.Font;

import java.util.List;

public interface AbstractFontManager {
    String DEFAULT_FONT_NAME = "Xolonium-Regular";

    void registerFont(ClientGameLogic client, String fontName, String fontFile);
    Font getFont(String fontName);
    List<Font> getFonts();
    void clear();
}
