package net.bfsr.engine.world;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import net.bfsr.engine.util.ObjectPool;

@Getter
public class ObjectPools {
    private final Object2ObjectMap<Class<?>, ObjectPool<?>> objectPoolMap = new Object2ObjectOpenHashMap<>();

    public void addPool(Class<?> objectClass, ObjectPool<?> objectPool) {
        objectPoolMap.put(objectClass, objectPool);
    }

    public <T> ObjectPool<T> getPool(Class<T> objectClass) {
        return (ObjectPool<T>) objectPoolMap.get(objectClass);
    }

    public void clear() {
        objectPoolMap.clear();
    }
}