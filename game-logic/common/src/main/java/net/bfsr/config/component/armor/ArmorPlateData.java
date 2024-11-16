package net.bfsr.config.component.armor;

import lombok.Getter;
import net.bfsr.config.component.hull.HullData;

@Getter
public class ArmorPlateData extends HullData {
    private final float hullProtection;

    ArmorPlateData(ArmorPlateConfig config, String fileName, int id, int registryId) {
        super(config, fileName, id, registryId);
        this.hullProtection = config.getProtection();
    }
}