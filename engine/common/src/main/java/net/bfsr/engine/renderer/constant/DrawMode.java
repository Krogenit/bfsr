package net.bfsr.engine.renderer.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum DrawMode {
    LINES(0x1), LINE_LOOP(0x2), TRIANGLES(0x4);

    private final int gl;
}
