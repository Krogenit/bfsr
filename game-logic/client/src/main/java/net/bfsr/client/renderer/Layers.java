package net.bfsr.client.renderer;

import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.bfsr.damage.DamageableRigidBody;
import net.bfsr.engine.renderer.DepthBufferRenderLayers;
import net.bfsr.engine.world.entity.RigidBody;
import net.bfsr.entity.Station;
import net.bfsr.entity.bullet.Bullet;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.wreck.ShipWreck;
import net.bfsr.entity.wreck.Wreck;

import java.util.function.Function;

public class Layers {
    private static final int BACKGROUND_LAYER = 0;
    private static final float BACKGROUND_Z = DepthBufferRenderLayers.getZ(BACKGROUND_LAYER);

    private static final int STATION_LAYER = 1;

    private static final int SHIPS_LAYER_SIZE = 2;
    private static final int SHIPS_LAYER = 2;

    private static final float Z_NOT_FOUND_VALUE = -1.0f;

    private final Object2ObjectMap<Class<? extends RigidBody>, Function<Integer, Float>> layerSupplierMap = new Object2ObjectOpenHashMap<>();
    private final Int2FloatMap usedZByIdMap = new Int2FloatOpenHashMap();

    private int entityLayer = SHIPS_LAYER;

    public Layers() {
        usedZByIdMap.defaultReturnValue(Z_NOT_FOUND_VALUE);
        registerLayerSuppliers();
    }

    private void registerLayerSuppliers() {
        layerSupplierMap.put(Station.class, this::getZForStation);

        layerSupplierMap.put(RigidBody.class, this::getZForEntity);
        layerSupplierMap.put(DamageableRigidBody.class, this::getZForEntity);
        layerSupplierMap.put(Ship.class, this::getZForEntity);
        layerSupplierMap.put(Bullet.class, this::getZForEntity);
        layerSupplierMap.put(Wreck.class, this::getZForEntity);
        layerSupplierMap.put(ShipWreck.class, this::getZForEntity);
    }

    private float getZForStation(int id) {
        return DepthBufferRenderLayers.getZ(STATION_LAYER);
    }

    private float getZForEntity(int id) {
        int layer = entityLayer;
        float z = DepthBufferRenderLayers.getZ(layer);
        usedZByIdMap.put(id, z);
        entityLayer += SHIPS_LAYER_SIZE;
        return z;
    }

    public float getLayerFor(RigidBody rigidBody) {
        return layerSupplierMap.get(rigidBody.getClass()).apply(rigidBody.getId());
    }

    public float getUsedZ(int id) {
        float v = usedZByIdMap.get(id);

        if (v == Z_NOT_FOUND_VALUE) {
            return getZForEntity(id);
        }

        return v;
    }

    public float getBackgroundLayer() {
        return BACKGROUND_Z;
    }
}
