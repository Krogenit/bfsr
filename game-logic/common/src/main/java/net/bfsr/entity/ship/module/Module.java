package net.bfsr.entity.ship.module;

import lombok.NoArgsConstructor;
import net.bfsr.entity.GameObject;

@NoArgsConstructor
public abstract class Module extends GameObject {
    private float hp;

    protected Module(float sizeX, float sizeY) {
        super(sizeX, sizeY);
    }

    protected Module(float x, float y, float sizeX, float sizeY) {
        super(x, y, sizeX, sizeY);
    }

    public abstract ModuleType getType();
}