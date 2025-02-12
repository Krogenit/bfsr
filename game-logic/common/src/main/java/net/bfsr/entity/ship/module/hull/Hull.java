package net.bfsr.entity.ship.module.hull;

import lombok.Getter;
import net.bfsr.config.component.hull.HullData;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.ModuleType;
import net.bfsr.entity.ship.module.ModuleWithCells;

@Getter
public class Hull extends ModuleWithCells<HullCell> {
    private final float maxValue;
    protected final float repairSpeed;

    public Hull(HullData hullData, Ship ship) {
        super(hullData, ship, HullCell.class, HullCell::new);
        this.maxValue = hullData.getMaxHullValue();
        this.repairSpeed = hullData.getRegenAmount();

        for (int i = 0; i < cells.length; i++) {
            for (int i1 = 0; i1 < cells[0].length; i1++) {
                HullCell cell = cells[i][i1];
                cell.value = cell.maxValue = maxValue / (cells.length * cells[0].length);
            }
        }
    }

    @Override
    public void update() {
        for (int i = 0; i < cells.length; i++) {
            for (int i1 = 0; i1 < cells[0].length; i1++) {
                HullCell cell = cells[i][i1];
                cell.update();
            }
        }
    }

    public void repair(float repairAmount) {
        HullCell cell = getMostDamagedCell();
        if (cell != null) {
            if (cell.value > 0 && cell.value < cell.maxValue) {
                cell.value += repairSpeed + repairAmount;

                if (cell.value > cell.maxValue) {
                    cell.value = cell.maxValue;
                }
            }
        }
    }

    public HullCell damage(float amount, float contactX, float contactY, Ship ship) {
        HullCell cell = getCell(contactX, contactY, ship);
        cell.damage(amount);
        return cell;
    }

    private HullCell getMostDamagedCell() {
        float minValue = Float.MAX_VALUE;
        HullCell cell = null;
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[0].length; j++) {
                HullCell hullCell = cells[i][j];
                if (hullCell != null && hullCell.getRepairTimer() == 0 && hullCell.getValue() < minValue) {
                    minValue = hullCell.getValue();
                    cell = hullCell;
                }
            }
        }

        return cell;
    }

    @Override
    public ModuleType getType() {
        return ModuleType.HULL;
    }
}