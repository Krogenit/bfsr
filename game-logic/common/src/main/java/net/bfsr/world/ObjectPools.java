package net.bfsr.world;

import lombok.Getter;
import net.bfsr.engine.util.ObjectPool;
import net.bfsr.entity.wreck.Wreck;

@Getter
public class ObjectPools {
    private final ObjectPool<Wreck> wrecksPool = new ObjectPool<>(Wreck::new);
}