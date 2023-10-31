package net.bfsr.config.component.armor;

import lombok.Getter;
import net.bfsr.config.component.hull.HullData;

@Getter
public class ArmorPlateData extends HullData {
    private final float hullProtection;

    public ArmorPlateData(ArmorPlateConfig config, int dataIndex) {
        super(config, dataIndex);
        this.hullProtection = config.getProtection();
    }
}