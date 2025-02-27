package net.bfsr.config.component.hull;

import lombok.Getter;
import net.bfsr.engine.Engine;
import net.bfsr.engine.config.ConfigData;

@Getter
public class HullData extends ConfigData {
    private final float maxHullValue;
    private final float regenAmount;

    public HullData(HullConfig hullConfig, String fileName, int id, int registryId) {
        super(fileName, id, registryId);
        this.maxHullValue = hullConfig.getMaxValue();
        this.regenAmount = Engine.convertToDeltaTime(hullConfig.getRegenAmountInSeconds());
    }
}