package net.bfsr.entity.ship.module.armor;

import lombok.Getter;
import net.bfsr.config.component.armor.ArmorPlateData;
import net.bfsr.entity.ship.module.hull.HullCell;

@Getter
public class ArmorPlate extends HullCell {
    private final float hullProtection;

    ArmorPlate(ArmorPlateData armorPlateData) {
        this.hullProtection = armorPlateData.getHullProtection();
    }
}