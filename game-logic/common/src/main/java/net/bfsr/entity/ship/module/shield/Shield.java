package net.bfsr.entity.ship.module.shield;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.config.component.shield.ShieldData;
import net.bfsr.entity.RigidBody;
import net.bfsr.entity.ship.module.DamageableModule;
import net.bfsr.entity.ship.module.ModuleType;
import net.bfsr.module.CommonShieldLogic;
import net.bfsr.physics.PhysicsUtils;
import net.bfsr.physics.filter.ShipFilter;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Convex;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.Polygon;
import org.joml.Vector2f;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;

public class Shield extends DamageableModule {
    private final float shieldRegen;
    @Getter
    private final Vector2f diameter = new Vector2f();
    @Getter
    private final int timeToRebuild;
    @Setter
    @Getter
    private int rebuildingTime;
    @Getter
    private boolean alive;
    @Getter
    private final ShieldData shieldData;
    private BodyFixture shieldFixture;
    @Getter
    @Setter
    private float shieldHp;
    @Getter
    private final float shieldMaxHp;
    private final Convex shieldConvex;
    private final CommonShieldLogic logic;

    public Shield(ShieldData shieldData, Convex shieldConvex, CommonShieldLogic logic) {
        super(5.0f, 1.0f, 1.0f);
        this.shieldHp = shieldMaxHp = shieldData.getMaxShield();
        this.shieldRegen = shieldData.getRegenAmount();
        this.timeToRebuild = (int) shieldData.getRebuildTimeInTicks();
        this.rebuildingTime = timeToRebuild;
        this.shieldData = shieldData;
        this.shieldConvex = shieldConvex;
        this.logic = logic;
    }

    @Override
    public void createFixture(RigidBody rigidBody) {
        fixture = new BodyFixture(shieldConvex);
        fixture.setUserData(this);
        fixture.setFilter(new ShipFilter(rigidBody));
        fixture.setDensity(PhysicsUtils.DEFAULT_FIXTURE_DENSITY);
        rigidBody.getBody().addFixture(fixture);

        createShieldFixture();
    }

    private void createShieldFixture() {
        Body body = ship.getBody();
        if (shieldFixture != null) {
            body.removeFixture(shieldFixture);
        }

        org.locationtech.jts.geom.Polygon polygon = ship.getPolygon();
        CoordinateSequence coordinateSequence = polygon.getExteriorRing().getCoordinateSequence();
        int pointCount = coordinateSequence.size() - 1;
        Coordinate point = coordinateSequence.getCoordinate(0);
        float minX = (float) point.x;
        float maxX = (float) point.x;
        float minY = (float) point.y;
        float maxY = (float) point.y;
        for (int i = 1; i < pointCount; i++) {
            point = coordinateSequence.getCoordinate(i);
            float value = (float) point.x;
            if (value > maxX) {
                maxX = value;
            } else if (value < minX) {
                minX = value;
            }

            value = (float) point.y;
            if (value > maxY) {
                maxY = value;
            } else if (value < minY) {
                minY = value;
            }
        }

        float offset = 1.4f;
        diameter.set(maxX - minX + offset, maxY - minY + offset);

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

        logic.update(this);
    }

    public void rebuilding() {
        rebuildingTime += 1;
    }

    public void regenHp() {
        if (shieldHp < shieldMaxHp) {
            shieldHp += shieldRegen;
            if (shieldHp > shieldMaxHp) shieldHp = shieldMaxHp;
        }
    }

    @Override
    public void addFixtureToBody(Body body) {
        super.addFixtureToBody(body);
        if (shieldFixture != null) {
            body.addFixture(shieldFixture);
        }
    }

    public void rebuildShield() {
        shieldHp = shieldMaxHp / 5.0f;
        rebuildingTime = timeToRebuild;
        createShieldFixture();
    }

    @Override
    protected void destroy() {
        super.destroy();
        removeShield();
        ship.getFixturesToRemove().add(fixture);
    }

    public void resetRebuildingTime() {
        rebuildingTime = 0;
    }

    public void removeShield() {
        ship.getFixturesToRemove().add(shieldFixture);
        shieldFixture = null;
        rebuildingTime = 0;
        size.set(0.0f);
        shieldHp = 0;
        alive = false;
    }

    @Override
    public ModuleType getType() {
        return ModuleType.SHIELD;
    }
}