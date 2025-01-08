package net.bfsr.engine.renderer.font;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.bfsr.engine.Engine;
import net.bfsr.engine.renderer.font.glyph.GlyphsBuilder;
import net.bfsr.engine.renderer.font.legacy.LegacyGlyphsBuilder;

@Getter
@RequiredArgsConstructor
public enum Font {
    NOTOSANS_REGULAR_LEGACY(new LegacyGlyphsBuilder("NotoSans-Regular.ttf", true)),
    XOLONIUM_LEGACY(new LegacyGlyphsBuilder("Xolonium-Regular.ttf", true)),
    CONSOLA_LEGACY(new LegacyGlyphsBuilder("consola.ttf", true)),
    Segoe_UI_LEGACY(new LegacyGlyphsBuilder("Segoe UI.ttf", true)),

    NOTOSANS_REGULAR(Engine.renderer.createSTBTrueTypeGlyphsBuilder("NotoSans-Regular.ttf")),
    XOLONIUM(Engine.renderer.createSTBTrueTypeGlyphsBuilder("Xolonium-Regular.ttf")),
    CONSOLA(Engine.renderer.createSTBTrueTypeGlyphsBuilder("consola.ttf")),
    Segoe_UI(Engine.renderer.createSTBTrueTypeGlyphsBuilder("Segoe UI.ttf")),

    NOTOSANS_REGULAR_FT(Engine.renderer.createTrueTypeGlyphsBuilder("NotoSans-Regular.ttf")),
    XOLONIUM_FT(Engine.renderer.createTrueTypeGlyphsBuilder("Xolonium-Regular.ttf")),
    CONSOLA_FT(Engine.renderer.createTrueTypeGlyphsBuilder("consola.ttf")),
    Segoe_UI_FT(Engine.renderer.createTrueTypeGlyphsBuilder("Segoe UI.ttf"));

    private final GlyphsBuilder glyphsBuilder;
}