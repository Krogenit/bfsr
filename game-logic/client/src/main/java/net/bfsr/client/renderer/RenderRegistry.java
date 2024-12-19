package net.bfsr.client.renderer;

import gnu.trove.map.TMap;
import gnu.trove.map.hash.THashMap;
import net.bfsr.client.Client;
import net.bfsr.client.renderer.entity.BulletRender;
import net.bfsr.client.renderer.entity.RigidBodyRender;
import net.bfsr.client.renderer.entity.ShipRender;
import net.bfsr.client.renderer.entity.ShipWreckRenderer;
import net.bfsr.client.renderer.entity.WreckRender;
import net.bfsr.config.ConfigConverterManager;
import net.bfsr.config.GameObjectConfigData;
import net.bfsr.config.entity.ship.ShipRegistry;
import net.bfsr.entity.RigidBody;
import net.bfsr.entity.bullet.Bullet;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.wreck.ShipWreck;
import net.bfsr.entity.wreck.Wreck;

import java.util.function.Function;

class RenderRegistry {
    private final TMap<Class<? extends RigidBody>, Function<RigidBody, RigidBodyRender>> renderRegistry = new THashMap<>();

    private ConfigConverterManager configConverterManager;
    private ShipRegistry shipRegistry;

    RenderRegistry() {
        put(RigidBody.class, rigidBody -> new RigidBodyRender(rigidBody, ((GameObjectConfigData) configConverterManager
                .getConverter(rigidBody.getRegistryId()).get(rigidBody.getDataId())).getTexture()));
        put(Ship.class, ShipRender::new);
        put(ShipWreck.class, rigidBody -> new ShipWreckRenderer(rigidBody, shipRegistry.get(rigidBody.getDataId()).getTexture()));
        put(Wreck.class, WreckRender::new);
        put(Bullet.class, BulletRender::new);
    }

    public void init() {
        configConverterManager = Client.get().getConfigConverterManager();
        shipRegistry = configConverterManager.getConverter(ShipRegistry.class);
    }

    private <T> void put(Class<T> rigidBodyClass, Function<T, RigidBodyRender> function) {
        renderRegistry.put((Class<? extends RigidBody>) rigidBodyClass, (Function<RigidBody, RigidBodyRender>) function);
    }

    Render createRender(RigidBody rigidBody) {
        RigidBodyRender render = renderRegistry.get(rigidBody.getClass()).apply(rigidBody);
        render.init();
        return render;
    }
}