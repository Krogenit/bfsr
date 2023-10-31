package net.bfsr.entity.ship.module.armor;

import net.bfsr.config.component.armor.ArmorPlateData;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.ModuleType;
import net.bfsr.entity.ship.module.ModuleWithCells;

public class Armor extends ModuleWithCells<ArmorPlate> {
    public Armor(ArmorPlateData armorPlateData, Ship ship) {
        super(ship, ArmorPlate.class, () -> new ArmorPlate(armorPlateData));
    }

    @Override
    public void update() {
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[0].length; j++) {
                ArmorPlate armorPlate = cells[i][j];
                if (armorPlate != null) {
                    armorPlate.update();
                } else {
                    System.out.println();
                }
            }
        }
    }

    public float reduceDamageByArmor(float damageToArmor, float damageToHull, float contactX, float contactY, Ship ship) {
        ArmorPlate plate = getCell(contactX, contactY, ship);
        if (plate != null) {
            return plate.reduceDamage(damageToArmor, damageToHull);
        } else return damageToHull;
    }

    @Override
    public ModuleType getType() {
        return ModuleType.ARMOR;
    }
}