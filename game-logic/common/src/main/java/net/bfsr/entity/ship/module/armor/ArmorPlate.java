package net.bfsr.entity.ship.module.armor;

import net.bfsr.config.component.armor.ArmorPlateData;
import net.bfsr.entity.ship.module.hull.HullCell;

public class ArmorPlate extends HullCell {
    private final float armorHullProtection;

    public ArmorPlate(ArmorPlateData armorPlateData) {
        super(armorPlateData);
        this.armorHullProtection = armorPlateData.getHullProtection();
    }

    public float reduceDamage(float damageToArmor, float damageToHull) {
        if (value > 0) {
            value -= damageToArmor;
            return damageToHull / armorHullProtection;
        } else {
            return damageToHull;
        }
    }
}