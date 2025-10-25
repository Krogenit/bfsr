package net.bfsr.config.component.reactor;

import lombok.Getter;
import net.bfsr.config.component.DamageableModuleConfig;
import net.bfsr.engine.config.Configurable;

@Getter
@Configurable
final class ReactorConfig extends DamageableModuleConfig {
    private float maxEnergyCapacity;
    private float regenAmountInSeconds;
}