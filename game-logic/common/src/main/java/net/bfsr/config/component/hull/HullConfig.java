package net.bfsr.config.component.hull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.bfsr.config.Configurable;

@Configurable
@AllArgsConstructor
@Getter
public class HullConfig {
    private final String name;
    private final float maxValue;
    private final float regenAmountInSeconds;
}