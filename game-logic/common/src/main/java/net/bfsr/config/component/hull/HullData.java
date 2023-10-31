package net.bfsr.config.component.hull;

import lombok.Getter;
import net.bfsr.config.ConfigData;
import net.bfsr.engine.util.TimeUtils;

@Getter
public class HullData extends ConfigData {
    private final float maxHullValue;
    private final float regenAmount;

    public HullData(HullConfig hullConfig, int dataIndex) {
        super(hullConfig.getName(), dataIndex);
        this.maxHullValue = hullConfig.getMaxValue();
        this.regenAmount = hullConfig.getRegenAmountInSeconds() * TimeUtils.UPDATE_DELTA_TIME;
    }
}