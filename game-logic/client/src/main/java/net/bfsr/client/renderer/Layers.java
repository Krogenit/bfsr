package net.bfsr.client.renderer;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.bfsr.damage.DamageableRigidBody;
import net.bfsr.engine.renderer.DepthBufferRenderLayers;
import net.bfsr.engine.world.entity.RigidBody;
import net.bfsr.entity.Station;
import net.bfsr.entity.ship.Ship;

import java.util.function.Supplier;

public class Layers {
    private static final int BACKGROUND_LAYER = 0;
    private static final float BACKGROUND_Z = DepthBufferRenderLayers.getZ(BACKGROUND_LAYER);

    private static final int STATION_LAYER_SIZE = 3;
    private static final int STATION_LAYER = 2;

    private static final int SHIPS_LAYER_SIZE = 3;
    private static final int SHIPS_LAYER = 100;

    private final Object2ObjectMap<Class<? extends RigidBody>, Supplier<Float>> layerSupplierMap = new Object2ObjectOpenHashMap<>();

    private int entityLayer = SHIPS_LAYER;

    public Layers() {
        registerLayerSuppliers();
    }

    private void registerLayerSuppliers() {
        layerSupplierMap.put(RigidBody.class, this::getZForEntity);
        layerSupplierMap.put(DamageableRigidBody.class, this::getZForEntity);
        layerSupplierMap.put(Ship.class, this::getZForEntity);
        layerSupplierMap.put(Station.class, this::getZForStation);
    }

    private float getZForStation() {
        return DepthBufferRenderLayers.getZ(STATION_LAYER);
    }

    private float getZForEntity() {
        int layer = entityLayer;
        float z = DepthBufferRenderLayers.getZ(layer);
        entityLayer += SHIPS_LAYER_SIZE;
        return z;
    }

    public float getLayerFor(RigidBody rigidBody) {
        return layerSupplierMap.get(rigidBody.getClass()).get();
    }

    public float getBackgroundLayer() {
        return BACKGROUND_Z;
    }
}
