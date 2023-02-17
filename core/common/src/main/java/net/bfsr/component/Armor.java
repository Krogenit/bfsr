package net.bfsr.component;

import net.bfsr.math.Direction;

public class Armor {
    private final ArmorPlate[] armorPlates;

    public Armor() {
        this.armorPlates = new ArmorPlate[4];
    }

    public void update() {
        for (int i = 0; i < armorPlates.length; i++) {
            ArmorPlate armorPlate = armorPlates[i];
            if (armorPlate != null) armorPlate.update();
        }
    }

    public float reduceDamageByArmor(float damageToArmor, float damageToHull, Direction dir) {
        ArmorPlate plate = getArmorPlatreByDir(dir);
        if (plate != null) {
            return plate.reduceDamage(damageToArmor, damageToHull);
        } else return damageToHull;
    }

    public void setArmorPlateByDir(Direction dir, ArmorPlate armor) {
        setArmorPlateToSlot(dir.ordinal(), armor);
    }

    public ArmorPlate getArmorPlatreByDir(Direction dir) {
        return armorPlates[dir.ordinal()];
    }

    public void setArmorPlateToSlot(int i, ArmorPlate armor) {
        armor.setId(i);
        this.armorPlates[i] = armor;
    }

    public ArmorPlate getArmorPlate(int i) {
        return armorPlates[i];
    }

    public ArmorPlate[] getArmorPlates() {
        return armorPlates;
    }
}
