package net.bfsr.config.component.armor;

import lombok.Getter;
import net.bfsr.config.Configurable;
import net.bfsr.config.component.hull.HullConfig;

@Configurable
@Getter
public class ArmorPlateConfig extends HullConfig {
    private final float protection;

    public ArmorPlateConfig(float maxValue, float regenAmountInSeconds, float protection) {
        super(maxValue, regenAmountInSeconds);
        this.protection = protection;
    }
}