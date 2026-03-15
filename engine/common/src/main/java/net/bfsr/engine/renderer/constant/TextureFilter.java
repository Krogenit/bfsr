package net.bfsr.engine.renderer.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum TextureFilter {
    NEAREST(0x2600), LINEAR(0x2601);

    private final int gl;
}
