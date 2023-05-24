package net.bfsr.component.shield;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.config.component.shield.ShieldData;
import net.bfsr.engine.util.SideUtils;
import net.bfsr.engine.util.TimeUtils;
import net.bfsr.entity.ship.Ship;
import net.bfsr.event.EventBus;
import net.bfsr.event.module.shield.ShieldRebuildEvent;
import net.bfsr.event.module.shield.ShieldRemoveEvent;
import net.bfsr.event.module.shield.ShieldResetRebuildingTimeEvent;
import net.bfsr.physics.PhysicsUtils;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Convex;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.Polygon;
import org.dyn4j.geometry.Vector2;
import org.joml.Vector2f;

import java.util.List;

public class Shield {
    @Getter
    @Setter
    private float shield;
    @Getter
    private final float maxShield;
    private final float shieldRegen;
    private final Vector2f radius = new Vector2f();
    @Getter
    private final Vector2f diameter = new Vector2f();
    private final int timeToRebuild;
    private int rebuildingTime;
    @Getter
    private float size;
    private Body body;
    private boolean alive;
    private BodyFixture shieldFixture;
    @Getter
    private Ship ship;
    @Getter
    private final ShieldData shieldData;

    public Shield(ShieldData shieldData) {
        this.size = 1.0f;
        this.maxShield = shieldData.getMaxShield();
        this.shieldRegen = shieldData.getRegenAmount();
        this.timeToRebuild = (int) shieldData.getRebuildTimeInTicks();
        this.rebuildingTime = timeToRebuild;
        this.shield = maxShield;
        this.shieldData = shieldData;
    }

    public void init(Ship ship) {
        this.body = ship.getBody();
        this.ship = ship;
        createBody();
    }

    public void createBody() {
        List<BodyFixture> fixtures = body.getFixtures();
        if (shieldFixture != null) {
            body.removeFixture(shieldFixture);
            shieldFixture = null;
        }

        for (int i = 0; i < fixtures.size(); i++) {
            BodyFixture bodyFixture = fixtures.get(i);
            Convex convex = bodyFixture.getShape();
            if (convex instanceof Polygon polygon) {
                Vector2[] vertices = polygon.getVertices();
                for (int j = 0, verticesLength = vertices.length; j < verticesLength; j++) {
                    Vector2 vertex = vertices[j];
                    float x1 = (float) Math.abs(vertex.x);
                    if (x1 > radius.x) {
                        radius.x = x1;
                    }
                    float y1 = (float) Math.abs(vertex.y);
                    if (y1 > radius.y) {
                        radius.y = y1;
                    }
                }
            }
        }

        float offset = 1.4f;
        diameter.set(radius.x * 2.0f + offset, radius.y * 2.0f + offset);

        Polygon ellipse = Geometry.createPolygonalEllipse(12, diameter.x, diameter.y);
        shieldFixture = new BodyFixture(ellipse);
        shieldFixture.setUserData(this);
        shieldFixture.setDensity(PhysicsUtils.SHIELD_FIXTURE_DENSITY);
        shieldFixture.setFriction(0.0f);
        shieldFixture.setRestitution(0.1f);
        shieldFixture.setFilter(body.getFixture(0).getFilter());
        body.addFixture(shieldFixture);
        diameter.x += 0.1f;
        diameter.y += 0.1f;
        alive = true;
    }

    public void update() {
        if (SideUtils.IS_SERVER && ship.getWorld().isServer()) {
            if (alive && shield <= 0) {
                removeShield();
            }
        }

        if (shield < maxShield && isShieldAlive()) {
            shield += shieldRegen;

            onShieldAlive();

            if (shield > maxShield) {
                shield = maxShield;
            }
        }

        if (SideUtils.IS_SERVER && ship.getWorld().getSide().isServer()) {
            if (rebuildingTime < timeToRebuild) {
                rebuildingTime += 1;

                if (rebuildingTime >= timeToRebuild) {
                    rebuildShield();
                }
            }
        }
    }

    private void onShieldAlive() {
        if (size < 1.0f) {
            size += 3.6f * TimeUtils.UPDATE_DELTA_TIME;
            if (size > 1.0f) size = 1.0f;
        }
    }

    public boolean isShieldAlive() {
        return rebuildingTime >= timeToRebuild;
    }

    public void rebuildShield() {
        shield = maxShield / 5.0f;
        rebuildingTime = timeToRebuild;
        createBody();
        EventBus.post(ship.getWorld().getSide(), new ShieldRebuildEvent(this));
    }

    public boolean damage(float shieldDamage) {
        if (shield > 0) {
            shield -= shieldDamage;
            return true;
        }

        onNoShieldDamage();
        return false;
    }

    private void onNoShieldDamage() {
        resetRebuildingTime();
    }

    private void resetRebuildingTime() {
        rebuildingTime = 0;
        EventBus.post(ship.getWorld().getSide(), new ShieldResetRebuildingTimeEvent(this));
    }

    public void setRebuildingTime(int time) {
        rebuildingTime = time;
    }

    public void removeShield() {
        body.removeFixture(shieldFixture);
        shieldFixture = null;
        rebuildingTime = 0;
        size = 0.0f;
        shield = 0;
        alive = false;
        EventBus.post(ship.getWorld().getSide(), new ShieldRemoveEvent(this));
    }
}