package net.bfsr.entity;

import net.bfsr.config.entity.station.StationData;
import net.bfsr.engine.world.entity.RigidBody;
import net.bfsr.network.packet.common.entity.spawn.StationSpawnData;
import net.bfsr.physics.collision.filter.Filters;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Filter;
import org.jbox2d.dynamics.Fixture;

import java.util.List;

public class Station extends RigidBody {
    private int collisionTimer;
    private int sparksTimer;
    private final int timeToDestroy, maxSparksTimer;
    private Runnable updateRunnable = this::updateAlive;

    public Station(StationData stationData) {
        super(stationData.getSizeX(), stationData.getSizeY());
        this.configData = stationData;
        this.timeToDestroy = stationData.getDestroyTimeInFrames();
        this.maxSparksTimer = timeToDestroy / 3;
    }

    @Override
    protected void initBody() {
        super.initBody();

        List<Shape> convexes = configData.getShapeList();
        for (int i = 0; i < convexes.size(); i++) {
            addHullFixture(setupFixture(new Fixture(convexes.get(i))));
        }

        body.setUserData(this);
        body.setType(BodyType.STATIC);
    }

    @Override
    public void update() {
        super.update();
        updateRunnable.run();
    }

    private void updateAlive() {
        if (collisionTimer > 0) {
            collisionTimer -= 1;
        }
    }

    @Override
    protected void updateLifeTime() {}

    private void updateDestroying() {
        sparksTimer -= 1;
        if (sparksTimer <= 0) {
//            eventBus.publish(new StationDestroyingExplosionEvent(this));
            sparksTimer = maxSparksTimer;
        }

        lifeTime++;
    }

    @Override
    public StationSpawnData createSpawnData() {
        return new StationSpawnData();
    }

    public void setDestroying() {
        if (maxLifeTime == DEFAULT_MAX_LIFE_TIME_IN_FRAMES) {
            maxLifeTime = timeToDestroy;
//            eventBus.publish(new ShipDestroyingEvent(this));
            updateRunnable = this::updateDestroying;
        }
    }

    public boolean isDestroying() {
        return maxLifeTime != DEFAULT_MAX_LIFE_TIME_IN_FRAMES;
    }

    @Override
    public Filter getCollisionFilter(Fixture fixture) {
        return Filters.SHIP_FILTER;
    }

    @Override
    public int getEntityType() {
        return EntityTypes.STATION.ordinal();
    }
}
