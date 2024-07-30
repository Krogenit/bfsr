package net.bfsr.engine.renderer.font;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.bfsr.engine.Engine;
import net.bfsr.engine.renderer.font.glyph.GlyphsBuilder;
import net.bfsr.engine.renderer.font.legacy.LegacyGlyphsBuilder;

@Getter
@RequiredArgsConstructor
public enum Font {
    SANS_SERIF_LEGACY(new LegacyGlyphsBuilder()),
    NOTOSANS_REGULAR_LEGACY(new LegacyGlyphsBuilder("NotoSans-Regular.ttf", true)),
    XOLONIUM_LEGACY(new LegacyGlyphsBuilder("Xolonium-Regular.ttf", true)),
    CONSOLA_LEGACY(new LegacyGlyphsBuilder("consola.ttf", true)),
    Segoe_UI_LEGACY(new LegacyGlyphsBuilder("Segoe UI.ttf", true)),

    NOTOSANS_REGULAR(Engine.renderer.stringRenderer.createSTBTrueTypeGlyphsBuilder("NotoSans-Regular.ttf")),
    XOLONIUM(Engine.renderer.stringRenderer.createSTBTrueTypeGlyphsBuilder("Xolonium-Regular.ttf")),
    CONSOLA(Engine.renderer.stringRenderer.createSTBTrueTypeGlyphsBuilder("consola.ttf")),
    Segoe_UI(Engine.renderer.stringRenderer.createSTBTrueTypeGlyphsBuilder("Segoe UI.ttf"));

    private final GlyphsBuilder glyphsBuilder;
}