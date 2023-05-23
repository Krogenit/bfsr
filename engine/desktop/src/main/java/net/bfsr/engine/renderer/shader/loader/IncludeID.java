package net.bfsr.engine.renderer.shader.loader;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class IncludeID {
    private int value;

    public boolean isValid() {
        return value != 0;
    }
}