package net.bfsr.config.component.reactor;

import lombok.Getter;
import net.bfsr.config.ConfigData;
import net.bfsr.util.TimeUtils;

@Getter
public class ReactorData extends ConfigData {
    private final float maxEnergyCapacity;
    private final float regenAmount;

    public ReactorData(ReactorConfig reactorConfig, int dataIndex) {
        super(reactorConfig.name(), dataIndex);
        this.maxEnergyCapacity = reactorConfig.maxEnergyCapacity();
        this.regenAmount = reactorConfig.regenAmountInSeconds() * TimeUtils.UPDATE_DELTA_TIME;
    }
}