package net.bfsr.engine.physics.collision.filter;

import lombok.Getter;
import org.jbox2d.dynamics.Filter;

@Getter
public class CollisionProfile {
    private final Filter shipFilter;
    private final Filter bulletFilter;
    private final Filter beamFilter;

    public CollisionProfile(Filter shipFilter, Filter bulletFilter, Filter beamFilter) {
        this.shipFilter = shipFilter;
        this.bulletFilter = bulletFilter;
        this.beamFilter = beamFilter;
    }
}
