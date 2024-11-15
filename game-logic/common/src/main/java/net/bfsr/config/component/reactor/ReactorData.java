package net.bfsr.config.component.reactor;

import lombok.Getter;
import net.bfsr.config.component.DamageableModuleData;
import net.bfsr.engine.Engine;

@Getter
public class ReactorData extends DamageableModuleData {
    private final float maxEnergyCapacity;
    private final float regenAmount;

    ReactorData(ReactorConfig reactorConfig, String fileName, int id, int registryId) {
        super(reactorConfig, fileName, id, registryId);
        this.maxEnergyCapacity = reactorConfig.getMaxEnergyCapacity();
        this.regenAmount = Engine.convertToDeltaTime(reactorConfig.getRegenAmountInSeconds());
    }
}