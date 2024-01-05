package net.bfsr.entity.ship.module.shield;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.config.component.shield.ShieldData;
import net.bfsr.engine.event.Event;
import net.bfsr.engine.util.SideUtils;
import net.bfsr.engine.util.TimeUtils;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.DamageableModule;
import net.bfsr.entity.ship.module.ModuleType;
import net.bfsr.event.module.shield.ShieldRebuildEvent;
import net.bfsr.event.module.shield.ShieldRemoveEvent;
import net.bfsr.event.module.shield.ShieldResetRebuildingTimeEvent;
import net.bfsr.physics.PhysicsUtils;
import net.bfsr.physics.filter.ShipFilter;
import net.engio.mbassy.bus.MBassador;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Convex;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.Polygon;
import org.dyn4j.geometry.Vector2;
import org.joml.Vector2f;

import java.util.List;

public class Shield extends DamageableModule {
    private final float shieldRegen;
    private final Vector2f radius = new Vector2f();
    @Getter
    private final Vector2f diameter = new Vector2f();
    private final int timeToRebuild;
    @Setter
    private int rebuildingTime;
    private final Body body;
    private boolean alive;
    @Getter
    private final Ship ship;
    @Getter
    private final ShieldData shieldData;
    private final MBassador<Event> eventBus;
    private BodyFixture shieldFixture;
    @Getter
    @Setter
    private float shieldHp;
    @Getter
    private float shieldMaxHp;

    public Shield(ShieldData shieldData, Ship ship) {
        super(5.0f, 1.0f, 1.0f);
        this.shieldHp = shieldMaxHp = shieldData.getMaxShield();
        this.shieldRegen = shieldData.getRegenAmount();
        this.timeToRebuild = (int) shieldData.getRebuildTimeInTicks();
        this.rebuildingTime = timeToRebuild;
        this.shieldData = shieldData;
        this.ship = ship;
        this.body = ship.getBody();
        this.eventBus = ship.getWorld().getEventBus();
    }

    @Override
    public void createFixture() {
        fixture = new BodyFixture(ship.getConfigData().getShieldPolygon());
        fixture.setUserData(this);
        fixture.setFilter(new ShipFilter(ship));
        fixture.setDensity(PhysicsUtils.DEFAULT_FIXTURE_DENSITY);
        body.addFixture(fixture);

        createShieldFixture();
    }

    private void createShieldFixture() {
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
        shieldFixture.setDensity(PhysicsUtils.ZERO_FIXTURE_DENSITY);
        shieldFixture.setFriction(0.0f);
        shieldFixture.setRestitution(0.1f);
        shieldFixture.setFilter(body.getFixture(0).getFilter());
        body.addFixture(shieldFixture);
        diameter.x += 0.1f;
        diameter.y += 0.1f;
        alive = true;
    }

    @Override
    public void update() {
        if (isDead) return;

        if (SideUtils.IS_SERVER && ship.getWorld().isServer()) {
            if (alive && shieldHp <= 0) {
                removeShield();
            }
        }

        if (shieldHp < shieldMaxHp && isShieldAlive()) {
            shieldHp += shieldRegen;

            onShieldAlive();

            if (shieldHp > shieldMaxHp) {
                shieldHp = shieldMaxHp;
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

    @Override
    public void addFixtureToBody(Body body) {
        super.addFixtureToBody(body);
        if (shieldFixture != null) {
            body.addFixture(shieldFixture);
        }
    }

    private void onShieldAlive() {
        if (size.x < 1.0f) {
            size.x += 3.6f * TimeUtils.UPDATE_DELTA_TIME;
            if (size.x > 1.0f) size.x = 1.0f;
        }
    }

    public boolean isShieldAlive() {
        return rebuildingTime >= timeToRebuild;
    }

    public void rebuildShield() {
        shieldHp = shieldMaxHp / 5.0f;
        rebuildingTime = timeToRebuild;
        createShieldFixture();
        eventBus.publish(new ShieldRebuildEvent(this));
    }

    @Override
    public boolean damage(float amount) {
        if (isDead) return false;

        if (SideUtils.IS_SERVER && ship.getWorld().isServer()) {
            if (shieldHp > 0) {
                shieldHp -= amount;

                if (shieldHp < 0) {
                    shieldHp = 0;
                }

                return true;
            }

            onNoShieldDamage();
            return false;
        } else {
            return shieldHp > 0;
        }
    }

    @Override
    protected void destroy() {
        super.destroy();
        ship.getFixturesToRemove().add(fixture);
    }

    private void onNoShieldDamage() {
        resetRebuildingTime();
    }

    private void resetRebuildingTime() {
        rebuildingTime = 0;
        eventBus.publish(new ShieldResetRebuildingTimeEvent(this));
    }

    public void removeShield() {
        body.removeFixture(shieldFixture);
        shieldFixture = null;
        rebuildingTime = 0;
        size.set(0.0f);
        shieldHp = 0;
        alive = false;
        eventBus.publish(new ShieldRemoveEvent(this));
    }

    @Override
    public ModuleType getType() {
        return ModuleType.SHIELD;
    }
}