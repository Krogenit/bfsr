package net.bfsr.client.renderer;

import gnu.trove.map.TMap;
import gnu.trove.map.hash.THashMap;
import net.bfsr.client.renderer.entity.BulletRender;
import net.bfsr.client.renderer.entity.RigidBodyRender;
import net.bfsr.client.renderer.entity.ShipRender;
import net.bfsr.client.renderer.entity.ShipWreckRenderer;
import net.bfsr.client.renderer.entity.WreckRender;
import net.bfsr.entity.RigidBody;
import net.bfsr.entity.bullet.Bullet;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.wreck.ShipWreck;
import net.bfsr.entity.wreck.Wreck;

import java.util.function.Function;

class RenderRegistry {
    private final TMap<Class<? extends RigidBody>, Function<RigidBody, RigidBodyRender>> renderRegistry = new THashMap<>();

    RenderRegistry() {
        renderRegistry.put(RigidBody.class, RigidBodyRender::new);
        renderRegistry.put(Ship.class, rigidBody -> new ShipRender((Ship) rigidBody));
        renderRegistry.put(ShipWreck.class, rigidBody -> new ShipWreckRenderer((ShipWreck) rigidBody));
        renderRegistry.put(Wreck.class, rigidBody -> new WreckRender((Wreck) rigidBody));
        renderRegistry.put(Bullet.class, rigidBody -> new BulletRender((Bullet) rigidBody));
    }

    Render createRender(RigidBody rigidBody) {
        return renderRegistry.get(rigidBody.getClass()).apply(rigidBody);
    }
}