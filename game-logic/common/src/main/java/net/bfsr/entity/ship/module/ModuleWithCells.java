package net.bfsr.entity.ship.module;

import lombok.Getter;
import net.bfsr.config.ConfigData;
import net.bfsr.entity.ship.Ship;
import org.joml.Vector2f;

import java.lang.reflect.Array;
import java.util.function.BiFunction;

public abstract class ModuleWithCells<T extends ModuleCell> extends Module {
    @Getter
    protected final T[][] cells;
    protected final float shipSizeX, shipSizeY;
    private final float halfShipSizeX, halfShipSizeY;
    protected final Ship ship;
    private final Vector2f toLocalCoordinatesScale = new Vector2f();
    private final int maxRow;
    private final int maxColumn;

    protected ModuleWithCells(ConfigData data, Ship ship, Class<T> componentType, BiFunction<Integer, Integer, T> supplier) {
        super(data);
        this.ship = ship;
        this.shipSizeX = ship.getSizeX();
        this.shipSizeY = ship.getSizeY();
        this.halfShipSizeX = shipSizeX * 0.5f;
        this.halfShipSizeY = shipSizeY * 0.5f;
        int columns;
        int rows;
        if (shipSizeX > 100.0f || shipSizeY > 100.0f) {
            columns = (int) Math.ceil(shipSizeX / 4.0f);
            rows = (int) Math.ceil(shipSizeY / 4.0f);
        } else {
            columns = (int) Math.ceil(shipSizeX / 2.0f);
            rows = (int) Math.ceil(shipSizeY / 2.0f);
        }

        this.cells = (T[][]) Array.newInstance(componentType, columns, rows);

        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[0].length; j++) {
                cells[i][j] = supplier.apply(i, j);
            }
        }

        this.toLocalCoordinatesScale.set(cells.length / shipSizeX, cells[0].length / shipSizeY);
        this.maxColumn = columns - 1;
        this.maxRow = rows - 1;
    }

    public T getCell(float contactX, float contactY) {
        float sin = -ship.getSin();
        float cos = ship.getCos();
        float localPosX = contactX - ship.getX();
        float localPosY = contactY - ship.getY();
        float rotatedX = cos * localPosX - sin * localPosY;
        float rotatedY = sin * localPosX + cos * localPosY;
        int localX = Math.max(Math.min((int) ((rotatedX + halfShipSizeX) * toLocalCoordinatesScale.x), maxColumn), 0);
        int localY = Math.max(Math.min((int) ((rotatedY + halfShipSizeY) * toLocalCoordinatesScale.y), maxRow), 0);

        return cells[localX][localY];
    }
}