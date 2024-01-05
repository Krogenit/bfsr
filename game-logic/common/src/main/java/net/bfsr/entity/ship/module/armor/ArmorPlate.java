package net.bfsr.entity.ship.module.armor;

import net.bfsr.config.component.armor.ArmorPlateData;
import net.bfsr.entity.ship.module.hull.HullCell;

public class ArmorPlate extends HullCell {
    private final float armorHullProtection;

    ArmorPlate(ArmorPlateData armorPlateData) {
        this.armorHullProtection = armorPlateData.getHullProtection();
    }

    float reduceDamage(float damageToArmor, float damageToHull) {
        if (value > 0) {
            damage(damageToArmor);
            return damageToHull / armorHullProtection;
        } else {
            return damageToHull;
        }
    }
}