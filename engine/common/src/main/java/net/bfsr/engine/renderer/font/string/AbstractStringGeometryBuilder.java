package net.bfsr.engine.renderer.font.string;

import net.bfsr.engine.renderer.font.StringOffsetType;
import net.bfsr.engine.renderer.font.glyph.GlyphsBuilder;

public abstract class AbstractStringGeometryBuilder {
    public abstract void createString(AbstractGLString glString, GlyphsBuilder glyphsBuilder, String string, int x, int y,
                                      int fontSize, float r, float g, float b, float a, int maxWidth, int indent);
    public abstract void createString(AbstractGLString glString, GlyphsBuilder glyphsBuilder, String string, int x, int y,
                                      int fontSize, float r, float g, float b, float a, StringOffsetType offsetType,
                                      boolean shadow, int shadowOffsetX, int shadowOffsetY);
    public abstract void createString(AbstractGLString glString, GlyphsBuilder glyphsBuilder, String string, int x, int y, int fontSize,
                                      float r, float g, float b, float a, int maxWidth, StringOffsetType offsetType, int indent,
                                      boolean shadow, int shadowOffsetX, int shadowOffsetY);
}