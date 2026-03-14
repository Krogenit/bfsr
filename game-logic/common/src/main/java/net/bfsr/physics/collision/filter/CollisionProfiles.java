package net.bfsr.physics.collision.filter;

import net.bfsr.GameplayMode;
import net.bfsr.engine.physics.collision.filter.CollisionProfile;

public final class CollisionProfiles {
    private CollisionProfiles() {}

    public static CollisionProfile forGameplayMode(GameplayMode gameplayMode) {
        GameplayMode effectiveMode = gameplayMode == null ? GameplayMode.MMO : gameplayMode;
        long shipMaskBits = effectiveMode == GameplayMode.SESSION ? Categories.all() : Categories.none();
        return new CollisionProfile(new ShipFilter(shipMaskBits), new BulletFilter(), new BeamFilter());
    }
}
