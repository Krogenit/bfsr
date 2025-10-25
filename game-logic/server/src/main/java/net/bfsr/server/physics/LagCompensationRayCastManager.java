package net.bfsr.server.physics;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.bfsr.engine.network.LagCompensation;
import net.bfsr.engine.physics.CommonRayCastManager;
import net.bfsr.engine.world.World;
import net.bfsr.engine.world.entity.RigidBody;
import org.jbox2d.callbacks.RayCastCallback;
import org.jbox2d.common.Vector2;

@RequiredArgsConstructor
public class LagCompensationRayCastManager implements CommonRayCastManager {
    private final World world;
    private final LagCompensation lagCompensation;
    @Setter
    private int compensateTimeInFrames;

    @Override
    public void rayCast(RigidBody rigidBody, RayCastCallback callback, Vector2 point1, Vector2 point2) {
        lagCompensation.compensateRay(rigidBody, callback, point1, point2, compensateTimeInFrames, world, world.getGameLogic().getFrame());
    }
}
