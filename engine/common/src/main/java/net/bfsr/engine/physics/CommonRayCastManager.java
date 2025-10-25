package net.bfsr.engine.physics;

import net.bfsr.engine.world.entity.RigidBody;
import org.jbox2d.callbacks.RayCastCallback;
import org.jbox2d.common.Vector2;

@FunctionalInterface
public interface CommonRayCastManager {
    void rayCast(RigidBody rigidBody, RayCastCallback callback, Vector2 point1, Vector2 point2);
}
