package net.bfsr.engine.renderer.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum BlendFactor {
    ONE(1), SRC_ALPHA(0x302), ONE_MINUS_SRC_ALPHA(0x303);

    private final int gl;
}
