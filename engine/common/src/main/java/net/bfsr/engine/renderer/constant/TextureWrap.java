package net.bfsr.engine.renderer.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum TextureWrap {
    REPEAT(0x2901), CLAMP_TO_EDGE(0x812F), CLAMP_TO_BORDER(0x812D);

    private final int gl;
}
