package net.bfsr.config.component.armor;

import lombok.Getter;
import net.bfsr.config.ConfigData;
import net.bfsr.util.TimeUtils;

@Getter
public class ArmorPlateData extends ConfigData {
    private final float maxArmorValue;
    private final float regenSpeed;
    private final float hullProtection;

    public ArmorPlateData(ArmorPlateConfig config, int dataIndex) {
        super(config.name(), dataIndex);
        this.maxArmorValue = config.maxArmorValue();
        this.regenSpeed = config.regenSpeedInSeconds() * TimeUtils.UPDATE_DELTA_TIME;
        this.hullProtection = config.hullProtection();
    }
}