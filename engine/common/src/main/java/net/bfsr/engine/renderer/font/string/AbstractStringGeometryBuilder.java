package net.bfsr.engine.renderer.font.string;

import net.bfsr.engine.renderer.font.StringCache;
import net.bfsr.engine.renderer.font.StringOffsetType;

public abstract class AbstractStringGeometryBuilder {
    public abstract void createString(AbstractGLString glString, StringCache stringCache, String string, int x, int y,
                                      int fontSize, float r, float g, float b, float a, int maxWidth, int indent);
    public abstract void createString(AbstractGLString glString, StringCache stringCache, String string, int x, int y,
                                      int fontSize, float r, float g, float b, float a, StringOffsetType stringOffsetType,
                                      boolean shadow, int shadowOffsetX, int shadowOffsetY);
}