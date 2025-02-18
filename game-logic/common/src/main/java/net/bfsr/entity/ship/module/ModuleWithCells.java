package net.bfsr.entity.ship.module;

import lombok.Getter;
import net.bfsr.config.ConfigData;
import net.bfsr.entity.ship.Ship;

import java.lang.reflect.Array;
import java.util.function.Supplier;

public abstract class ModuleWithCells<T> extends Module {
    @Getter
    protected final T[][] cells;
    protected final float width, height;

    protected ModuleWithCells(ConfigData data, Ship ship, Class<T> componentType, Supplier<T> supplier) {
        super(data);
        this.width = ship.getSizeX();
        this.height = ship.getSizeY();
        int width;
        int height;
        if (this.width > 100.0f || this.height > 100.0f) {
            width = (int) Math.ceil(this.width / 4.0f);
            height = (int) Math.ceil(this.height / 4.0f);
        } else {
            width = (int) Math.ceil(this.width / 2.0f);
            height = (int) Math.ceil(this.height / 2.0f);
        }

        this.cells = (T[][]) Array.newInstance(componentType, width, height);

        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[0].length; j++) {
                cells[i][j] = supplier.get();
            }
        }
    }

    public T getCell(float contactX, float contactY, Ship ship) {
        float sin = -ship.getSin();
        float cos = ship.getCos();

        float halfWidth = width * 0.5f;
        float halfHeight = height * 0.5f;

        float localPosX = contactX - ship.getX();
        float localPosY = contactY - ship.getY();
        float rotatedX = cos * localPosX - sin * localPosY;
        float rotatedY = sin * localPosX + cos * localPosY;
        int localX = Math.max(Math.min((int) ((rotatedX + halfWidth) * (cells.length / width)), cells.length - 1), 0);
        int localY = Math.max(Math.min((int) ((rotatedY + halfHeight) * (cells[0].length / height)), cells[0].length - 1), 0);

        return cells[localX][localY];
    }
}