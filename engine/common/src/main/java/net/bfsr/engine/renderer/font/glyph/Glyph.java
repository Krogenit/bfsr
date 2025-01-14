package net.bfsr.engine.renderer.font.glyph;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class Glyph {
    private float x1, y1, x2, y2;
    private float u1, v1, u2, v2;
    private long textureHandle;
    private int advance;
    private char codepoint;
    private boolean empty;
}
