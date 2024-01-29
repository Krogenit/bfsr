package net.bfsr.entity.ship.module.armor;

import net.bfsr.config.component.armor.ArmorPlateData;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.ModuleType;
import net.bfsr.entity.ship.module.ModuleWithCells;
import net.bfsr.entity.ship.module.hull.HullCell;

public class Armor extends ModuleWithCells<ArmorPlate> {
    private final float repairSpeed;

    public Armor(ArmorPlateData armorPlateData, Ship ship) {
        super(ship, ArmorPlate.class, () -> new ArmorPlate(armorPlateData));
        repairSpeed = armorPlateData.getRegenAmount();

        for (int i = 0; i < cells.length; i++) {
            for (int i1 = 0; i1 < cells[0].length; i1++) {
                HullCell cell = cells[i][i1];
                cell.setValue(armorPlateData.getMaxHullValue());
                cell.setMaxValue(armorPlateData.getMaxHullValue());
            }
        }
    }

    @Override
    public void update() {
        ArmorPlate cell = getMostDamagedCell();
        if (cell != null) {
            if (cell.getValue() < cell.getMaxValue()) {
                cell.setValue(cell.getValue() + repairSpeed);

                if (cell.getValue() > cell.getMaxValue()) {
                    cell.setValue(cell.getMaxValue());
                }
            }
        }

        for (int i = 0; i < cells.length; i++) {
            for (int i1 = 0; i1 < cells[0].length; i1++) {
                cells[i][i1].update();
            }
        }
    }

    private ArmorPlate getMostDamagedCell() {
        float minValue = Float.MAX_VALUE;
        ArmorPlate armorPlate = null;
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[0].length; j++) {
                ArmorPlate cell = cells[i][j];
                if (cell != null && cell.getRepairTimer() == 0 && cell.getValue() < minValue) {
                    minValue = cell.getValue();
                    armorPlate = cell;
                }
            }
        }

        return armorPlate;
    }

    @Override
    public ModuleType getType() {
        return ModuleType.ARMOR;
    }
}