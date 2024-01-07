package net.bfsr.config.component.hull;

import lombok.Getter;
import net.bfsr.config.ConfigData;
import net.bfsr.engine.Engine;

@Getter
public class HullData extends ConfigData {
    private final float maxHullValue;
    private final float regenAmount;

    public HullData(HullConfig hullConfig, String fileName, int id) {
        super(fileName, id);
        this.maxHullValue = hullConfig.getMaxValue();
        this.regenAmount = Engine.convertToDeltaTime(hullConfig.getRegenAmountInSeconds());
    }
}