package net.bfsr.config.component.armor;

import lombok.Getter;
import net.bfsr.config.component.hull.HullData;

@Getter
public class ArmorPlateData extends HullData {
    private final float hullProtection;

    ArmorPlateData(ArmorPlateConfig config, String name, int dataIndex) {
        super(config, name, dataIndex);
        this.hullProtection = config.getProtection();
    }
}