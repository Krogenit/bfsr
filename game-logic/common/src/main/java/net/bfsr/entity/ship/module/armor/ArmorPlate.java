package net.bfsr.entity.ship.module.armor;

import lombok.Getter;
import net.bfsr.config.component.armor.ArmorPlateData;
import net.bfsr.entity.ship.module.hull.HullCell;

@Getter
public class ArmorPlate extends HullCell {
    private final float hullProtection;

    ArmorPlate(int column, int row, ArmorPlateData armorPlateData) {
        super(column, row);
        this.hullProtection = armorPlateData.getHullProtection();
    }
}