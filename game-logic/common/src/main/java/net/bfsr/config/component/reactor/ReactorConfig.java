package net.bfsr.config.component.reactor;

import lombok.Getter;
import net.bfsr.config.Configurable;
import net.bfsr.config.component.DamageableModuleConfig;

@Getter
@Configurable
final class ReactorConfig extends DamageableModuleConfig {
    private float maxEnergyCapacity;
    private float regenAmountInSeconds;
}