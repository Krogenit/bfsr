package net.bfsr.config.component.hull;

import lombok.Getter;
import net.bfsr.config.ConfigData;
import net.bfsr.engine.util.TimeUtils;

@Getter
public class HullData extends ConfigData {
    private final float maxHullValue;
    private final float regenAmount;

    public HullData(HullConfig hullConfig, int dataIndex) {
        super(hullConfig.name(), dataIndex);
        this.maxHullValue = hullConfig.maxHullValue();
        this.regenAmount = hullConfig.regenAmountInSeconds() * TimeUtils.UPDATE_DELTA_TIME;
    }
}