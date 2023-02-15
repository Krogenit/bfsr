package net.bfsr.entity.wreck;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.collision.filter.WreckFilter;
import net.bfsr.entity.ship.ShipCommon;
import net.bfsr.physics.PhysicsUtils;
import net.bfsr.util.TimeUtils;
import org.dyn4j.dynamics.BodyFixture;

@NoArgsConstructor
@Getter
public abstract class ShipWreckCommon extends WreckCommon {
    protected float lifeTime;
    protected float maxLifeTime;

    public ShipWreckCommon init(int id, int wreckIndex, ShipCommon ship, float x, float y, float velocityX, float velocityY, float rotation, float angularVelocity, float scaleX, float scaleY,
                                float r, float g, float b, float a, float lifeTime) {
        RegisteredShipWreck wreck = WreckRegistry.INSTANCE.getWrecks(ship.getType())[wreckIndex];
        init(ship.getWorld(), id, x, y, velocityX, velocityY, rotation, angularVelocity, scaleX, scaleY, r, g, b, a, 0.0f, wreckIndex, true, true, true,
                ship.getHull().getMaxHull() / 4.0f, ship.getId(), WreckType.SHIP, wreck);
        this.lifeTime = maxLifeTime = lifeTime;
        return this;
    }

    @Override
    protected void setLinearAndAngularDamping() {
        body.setLinearDamping(0.2f);
        body.setAngularDamping(0.025f);
    }

    @Override
    protected void createFixtures() {
        if (body.getFixtures().size() > 0) body.removeFixture(0);
        BodyFixture bodyFixture = new BodyFixture(registeredShipWreck.getPolygon());
        bodyFixture.setDensity(PhysicsUtils.DEFAULT_FIXTURE_DENSITY);
        bodyFixture.setFilter(new WreckFilter(this));
        body.addFixture(bodyFixture);
    }

    @Override
    protected void updateLifeTime() {
        lifeTime -= 60.0f * TimeUtils.UPDATE_DELTA_TIME;
    }
}
