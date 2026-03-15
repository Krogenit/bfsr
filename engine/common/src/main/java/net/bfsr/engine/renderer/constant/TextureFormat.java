package net.bfsr.engine.renderer.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum TextureFormat {
    RED(0x1903);

    private final int gl;
}
