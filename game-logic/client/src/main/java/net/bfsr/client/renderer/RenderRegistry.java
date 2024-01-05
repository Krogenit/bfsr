package net.bfsr.client.renderer;

import gnu.trove.map.TMap;
import gnu.trove.map.hash.THashMap;
import net.bfsr.client.renderer.entity.*;
import net.bfsr.entity.RigidBody;
import net.bfsr.entity.bullet.Bullet;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.wreck.ShipWreck;
import net.bfsr.entity.wreck.Wreck;

import java.lang.reflect.Constructor;

@SuppressWarnings("rawtypes")
class RenderRegistry {
    private final TMap<Class<? extends RigidBody>, Constructor<? extends RigidBodyRender>> renderRegistry = new THashMap<>();

    RenderRegistry() {
        try {
            renderRegistry.put(RigidBody.class, RigidBodyRender.class.getConstructor(RigidBody.class));
            renderRegistry.put(Ship.class, ShipRender.class.getConstructor(Ship.class));
            renderRegistry.put(ShipWreck.class, ShipWreckRenderer.class.getConstructor(ShipWreck.class));
            renderRegistry.put(Wreck.class, WreckRender.class.getConstructor(Wreck.class));
            renderRegistry.put(Bullet.class, BulletRender.class.getConstructor(Bullet.class));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    Constructor<? extends RigidBodyRender> getRenderConstructor(Class<? extends RigidBody> rigidBodyClass) {
        return renderRegistry.get(rigidBodyClass);
    }
}