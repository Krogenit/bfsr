package net.bfsr.entity.ship.module;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.config.ConfigData;
import net.bfsr.engine.entity.GameObject;
import net.bfsr.entity.RigidBody;

import java.util.List;

@Setter
@Getter
public abstract class Module extends GameObject {
    protected final ConfigData data;
    protected int id;

    protected Module(ConfigData data) {
        this(data, 0.0f, 0.0f);
    }

    Module(ConfigData data, float sizeX, float sizeY) {
        this(data, 0.0f, 0.0f, sizeX, sizeY);
    }

    Module(ConfigData data, float x, float y, float sizeX, float sizeY) {
        super(x, y, sizeX, sizeY);
        this.data = data;
    }

    public abstract ModuleType getType();

    public void addToList(List<Module> modules) {
        modules.add(this);
    }

    public void postPhysicsUpdate(RigidBody rigidBody) {}

    @Override
    public void postPhysicsUpdate() {
        throw new RuntimeException("Use postPhysicsUpdate with RigidBody param instead");
    }

    public int getDataId() {
        return data.getId();
    }
}