package net.bfsr.config.component.armor;

import lombok.Getter;
import net.bfsr.config.component.hull.HullData;

@Getter
public class ArmorPlateData extends HullData {
    private final float hullProtection;

    ArmorPlateData(ArmorPlateConfig config, String fileName, int id) {
        super(config, fileName, id);
        this.hullProtection = config.getProtection();
    }
}