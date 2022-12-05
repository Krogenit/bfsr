package net.bfsr.component;

public class ArmorPlate {

    private float armor;
    private final float armorRegenSpeed;
    private final float armorMax;
    private final float armorHullProtection;
    private int id;

    public ArmorPlate(float armor, float armorRegenSpeed, float armorHullProtection) {
        this.armor = armor;
        this.armorRegenSpeed = armorRegenSpeed;
        this.armorMax = armor;
        this.armorHullProtection = armorHullProtection;
    }

    public void update(double delta) {
        if (armor < armorMax) {
            armor += armorRegenSpeed * delta;
        }
    }

    public float reduceDamage(float damageToArmor, float damageToHull) {
        if (armor > 0) {
            this.armor -= damageToArmor;
            return damageToHull / armorHullProtection;
        } else return damageToHull;
    }

    public float getArmor() {
        return armor;
    }

    public float getArmorMax() {
        return armorMax;
    }

    public float getArmorRegenSpeed() {
        return armorRegenSpeed;
    }

    public float getArmorHullProtection() {
        return armorHullProtection;
    }

    public void setArmor(float armor) {
        this.armor = armor;
    }

    public void setId(int i) {
        this.id = i;
    }

    public int getId() {
        return id;
    }
}
