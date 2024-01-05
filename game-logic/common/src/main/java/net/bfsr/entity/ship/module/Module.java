package net.bfsr.entity.ship.module;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.bfsr.entity.GameObject;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
public abstract class Module extends GameObject {
    protected int id;

    Module(float sizeX, float sizeY) {
        super(sizeX, sizeY);
    }

    Module(float x, float y, float sizeX, float sizeY) {
        super(x, y, sizeX, sizeY);
    }

    public abstract ModuleType getType();

    public void addToList(List<Module> modules) {
        modules.add(this);
    }
}