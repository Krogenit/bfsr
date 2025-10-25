package net.bfsr.engine.physics;

import lombok.AllArgsConstructor;
import net.bfsr.engine.world.entity.RigidBody;
import org.jbox2d.callbacks.RayCastCallback;
import org.jbox2d.common.Vector2;
import org.jbox2d.dynamics.World;

@AllArgsConstructor
public class DefaultRayCastManager implements CommonRayCastManager {
    private final World world;

    @Override
    public void rayCast(RigidBody rigidBody, RayCastCallback callback, Vector2 point1, Vector2 point2) {
        world.raycast(callback, point1, point2);
    }
}
