package net.bfsr.component.armor;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.config.component.armor.ArmorPlateData;

public class ArmorPlate {
    @Getter
    @Setter
    private float armor;
    private final float armorRegenSpeed;
    @Getter
    private final float armorMax;
    private final float armorHullProtection;
    @Setter
    @Getter
    private int id;

    public ArmorPlate(ArmorPlateData armorPlateData) {
        this.armor = armorPlateData.getMaxArmorValue();
        this.armorMax = armor;
        this.armorRegenSpeed = armorPlateData.getRegenSpeed();
        this.armorHullProtection = armorPlateData.getHullProtection();
    }

    public void update() {
        if (armor < armorMax) {
            armor += armorRegenSpeed;
        }
    }

    public float reduceDamage(float damageToArmor, float damageToHull) {
        if (armor > 0) {
            this.armor -= damageToArmor;
            return damageToHull / armorHullProtection;
        } else {
            return damageToHull;
        }
    }
}