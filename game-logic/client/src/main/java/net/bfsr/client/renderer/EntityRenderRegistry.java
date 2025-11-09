package net.bfsr.client.renderer;

import gnu.trove.map.TMap;
import gnu.trove.map.hash.THashMap;
import net.bfsr.client.Client;
import net.bfsr.client.renderer.entity.BulletRender;
import net.bfsr.client.renderer.entity.RigidBodyRender;
import net.bfsr.client.renderer.entity.ShipRender;
import net.bfsr.client.renderer.entity.ShipWreckRenderer;
import net.bfsr.client.renderer.entity.StationRender;
import net.bfsr.client.renderer.entity.WreckRender;
import net.bfsr.config.entity.ship.ShipRegistry;
import net.bfsr.engine.config.ConfigConverterManager;
import net.bfsr.engine.config.entity.GameObjectConfigData;
import net.bfsr.engine.renderer.entity.Render;
import net.bfsr.engine.world.entity.RigidBody;
import net.bfsr.entity.Station;
import net.bfsr.entity.bullet.Bullet;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.wreck.ShipWreck;
import net.bfsr.entity.wreck.Wreck;

import java.util.function.BiFunction;

class EntityRenderRegistry {
    private final TMap<Class<? extends RigidBody>, BiFunction<RigidBody, Float, RigidBodyRender>> renderRegistry = new THashMap<>();
    private final Layers layers;

    EntityRenderRegistry(Client client) {
        this.layers = client.getLayers();
        ConfigConverterManager configConverterManager = client.getConfigConverterManager();
        ShipRegistry shipRegistry = configConverterManager.getConverter(ShipRegistry.class);

        put(RigidBody.class, (rigidBody, z) -> new RigidBodyRender(rigidBody, z, ((GameObjectConfigData) configConverterManager
                .getConverter(rigidBody.getRegistryId()).get(rigidBody.getDataId())).getTexture()));
        put(Ship.class, ShipRender::new);
        put(ShipWreck.class, (rigidBody, z) -> new ShipWreckRenderer(rigidBody, z, shipRegistry.get(rigidBody.getDataId()).getTexture()));
        put(Wreck.class, WreckRender::new);
        put(Bullet.class, BulletRender::new);
        put(Station.class, StationRender::new);
    }

    private <T> void put(Class<T> rigidBodyClass, BiFunction<T, Float, RigidBodyRender> function) {
        renderRegistry.put((Class<? extends RigidBody>) rigidBodyClass, (BiFunction<RigidBody, Float, RigidBodyRender>) function);
    }

    Render createRender(RigidBody rigidBody) {
        RigidBodyRender render = renderRegistry.get(rigidBody.getClass()).apply(rigidBody, layers.getLayerFor(rigidBody));
        render.init();
        return render;
    }
}