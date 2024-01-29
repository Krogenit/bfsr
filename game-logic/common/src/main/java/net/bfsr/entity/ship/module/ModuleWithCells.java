package net.bfsr.entity.ship.module;

import lombok.Getter;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.entity.ship.Ship;
import org.dyn4j.geometry.AABB;

import java.lang.reflect.Array;
import java.util.function.Supplier;

public abstract class ModuleWithCells<T> extends Module {
    @Getter
    protected final T[][] cells;
    protected final float width, height;

    protected ModuleWithCells(Ship ship, Class<T> componentType, Supplier<T> supplier) {
        AABB aabb = new AABB(0);
        MathUtils.computeAABB(aabb, ship.getBody());
        this.width = (float) aabb.getWidth();
        this.height = (float) aabb.getHeight();
        int width = (int) Math.ceil(this.width / 2);
        int height = (int) Math.ceil(this.height / 2);
        this.cells = (T[][]) Array.newInstance(componentType, width, height);

        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[0].length; j++) {
                cells[i][j] = supplier.get();
            }
        }
    }

    public T getCell(float contactX, float contactY, Ship ship) {
        float sin = (float) -ship.getBody().getTransform().getSint();
        float cos = (float) ship.getBody().getTransform().getCost();

        float halfWidth = width * 0.5f;
        float halfHeight = height * 0.5f;

        float localPosX = contactX - (float) ship.getBody().getTransform().getTranslationX();
        float localPosY = contactY - (float) ship.getBody().getTransform().getTranslationY();
        float rotatedX = cos * localPosX - sin * localPosY;
        float rotatedY = sin * localPosX + cos * localPosY;
        int localX = Math.max(Math.min((int) ((rotatedX + halfWidth) * (halfWidth / cells.length)), cells.length - 1), 0);
        int localY = Math.max(Math.min((int) ((rotatedY + halfHeight) * (halfHeight / cells[0].length)), cells[0].length - 1), 0);

        return cells[localX][localY];
    }
}