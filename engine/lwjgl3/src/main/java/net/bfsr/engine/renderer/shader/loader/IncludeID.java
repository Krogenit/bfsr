package net.bfsr.engine.renderer.shader.loader;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
class IncludeID {
    private int value;

    boolean isValid() {
        return value != 0;
    }
}