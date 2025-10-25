package net.bfsr.config.component.armor;

import lombok.Getter;
import net.bfsr.config.component.hull.HullConfig;
import net.bfsr.engine.config.Configurable;

@Configurable
@Getter
public class ArmorPlateConfig extends HullConfig {
    private final float protection;

    public ArmorPlateConfig(float maxValue, float regenAmountInSeconds, float protection) {
        super(maxValue, regenAmountInSeconds);
        this.protection = protection;
    }
}