package net.bfsr.server.entity.wreck;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.entity.wreck.RegisteredShipWreck;
import net.bfsr.entity.wreck.WreckRegistry;
import net.bfsr.entity.wreck.WreckType;
import net.bfsr.physics.PhysicsUtils;
import net.bfsr.server.collision.filter.WreckFilter;
import net.bfsr.server.entity.ship.Ship;
import net.bfsr.util.TimeUtils;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Vector2;

@NoArgsConstructor
@Getter
public class ShipWreck extends Wreck {
    public ShipWreck init(int id, int wreckIndex, Ship ship, float x, float y, float velocityX, float velocityY, float rotation, float angularVelocity,
                          float scaleX, float scaleY, float lifeTime) {
        RegisteredShipWreck wreck = WreckRegistry.INSTANCE.getWrecks(ship.getType())[wreckIndex];
        init(ship.getWorld(), id, x, y, velocityX, velocityY, rotation, angularVelocity, scaleX, scaleY, lifeTime, 0.0f, wreckIndex, true, true, true,
                ship.getHull().getMaxHull() / 4.0f, ship.getId(), WreckType.SHIP, wreck);
        return this;
    }

    @Override
    protected void setLinearAndAngularDamping() {
        body.setLinearDamping(0.2f);
        body.setAngularDamping(0.025f);
    }

    @Override
    protected void createFixtures() {
        BodyFixture bodyFixture = new BodyFixture(registeredShipWreck.getPolygon());
        bodyFixture.setDensity(PhysicsUtils.DEFAULT_FIXTURE_DENSITY);
        bodyFixture.setFilter(new WreckFilter(this));
        body.addFixture(bodyFixture);
    }

    @Override
    protected void updateLifeTime() {
        lifeTime += 60.0f * TimeUtils.UPDATE_DELTA_TIME;
        if (lifeTime >= maxLifeTime) {
            destroy();
        }
    }

    @Override
    protected void destroy() {
        super.destroy();
        if (lifeTime < maxLifeTime) {
            Vector2 worldPos = body.getLocalCenter().add(position.x, position.y);
            WreckSpawner.spawnDamageDebris(world, random.nextInt(3), (float) worldPos.x, (float) worldPos.y, velocity.x, velocity.y, 1.0f);
            WreckSpawner.spawnDamageWrecks(world, random.nextInt(2), (float) worldPos.x, (float) worldPos.y, velocity.x, velocity.y);
        }
    }
}
