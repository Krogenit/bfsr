package net.bfsr.entity.ship.module.hull;

import lombok.Getter;
import net.bfsr.config.component.hull.HullData;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.ModuleType;
import net.bfsr.entity.ship.module.ModuleWithCells;

public class Hull extends ModuleWithCells<HullCell> {
    @Getter
    private final float maxValue;

    public Hull(HullData hullData, Ship ship) {
        super(ship, HullCell.class, () -> new HullCell(hullData));
        this.maxValue = width * height * hullData.getMaxHullValue();
    }

    public void repair(float repairAmount) {
        HullCell cell = getMostDamagedCell();
        if (cell != null) {
            if (cell.value < cell.maxValue) {
                cell.value += cell.repairSpeed + repairAmount;

                if (cell.value > cell.maxValue) {
                    cell.value = cell.maxValue;
                }
            }
        }
    }

    public void damage(float amount, float contactX, float contactY, Ship ship) {
        HullCell cell = getCell(contactX, contactY, ship);
        if (cell != null) {
            cell.value -= amount;
        } else {
            System.out.println();
        }
    }

    public boolean badlyDamaged() {
        return getValue() / maxValue <= 0.1f;
    }

    private HullCell getMostDamagedCell() {
        float minValue = maxValue;
        HullCell cell = null;
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[0].length; j++) {
                HullCell hullCell = cells[i][j];
                if (hullCell != null && hullCell.getValue() < minValue) {
                    minValue = hullCell.getValue();
                    cell = hullCell;
                }
            }
        }

        return cell;
    }

    public float getValue() {
        float value = 0;
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[0].length; j++) {
                HullCell hullCell = cells[i][j];
                if (hullCell != null) {
                    value += hullCell.getValue();
                }
            }
        }

        return value;
    }

    @Override
    public ModuleType getType() {
        return ModuleType.HULL;
    }
}