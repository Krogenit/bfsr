package net.bfsr.component;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.util.TimeUtils;

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

    public ArmorPlate(float armor, float armorRegenSpeed, float armorHullProtection) {
        this.armor = armor;
        this.armorRegenSpeed = armorRegenSpeed;
        this.armorMax = armor;
        this.armorHullProtection = armorHullProtection;
    }

    public void update() {
        if (armor < armorMax) {
            armor += armorRegenSpeed * TimeUtils.UPDATE_DELTA_TIME;
        }
    }

    public float reduceDamage(float damageToArmor, float damageToHull) {
        if (armor > 0) {
            this.armor -= damageToArmor;
            return damageToHull / armorHullProtection;
        } else return damageToHull;
    }
}
