package net.bfsr.component.armor;

import lombok.NoArgsConstructor;
import net.bfsr.config.component.armor.ArmorPlateData;
import net.bfsr.math.Direction;

@NoArgsConstructor
public class Armor {
    private final ArmorPlate[] armorPlates = new ArmorPlate[4];

    public Armor(ArmorPlateData armorPlateData) {
        for (int i = 0; i < armorPlates.length; i++) {
            armorPlates[i] = new ArmorPlate(armorPlateData);
        }
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