package net.bfsr.client.entity.wreck;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.client.collision.filter.WreckFilter;
import net.bfsr.client.core.Core;
import net.bfsr.client.entity.ship.Ship;
import net.bfsr.client.particle.ParticleSpawner;
import net.bfsr.entity.wreck.RegisteredShipWreck;
import net.bfsr.entity.wreck.WreckRegistry;
import net.bfsr.entity.wreck.WreckType;
import net.bfsr.physics.PhysicsUtils;
import net.bfsr.util.TimeUtils;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Vector2;

@NoArgsConstructor
@Getter
public class ShipWreck extends Wreck {
    protected float maxLifeTime;

    public ShipWreck init(int id, int wreckIndex, Ship ship, float x, float y, float velocityX, float velocityY, float rotation, float angularVelocity, float scaleX, float scaleY,
                          float r, float g, float b, float a, float lifeTime) {
        RegisteredShipWreck wreck = WreckRegistry.INSTANCE.getWrecks(ship.getType())[wreckIndex];
        init(ship.getWorld(), id, x, y, velocityX, velocityY, rotation, angularVelocity, scaleX, scaleY, r, g, b, a, 0.0f, wreckIndex, true, true, true,
                ship.getHull().getMaxHull() / 4.0f, ship.getId(), WreckType.SHIP, wreck);
        this.maxLifeTime = lifeTime;
        return this;
    }

    @Override
    protected void setLinearAndAngularDamping() {
        body.setLinearDamping(0.2f);
        body.setAngularDamping(0.025f);
    }

    @Override
    protected void createFixture() {
        if (body.getFixtures().size() > 0) body.removeFixture(0);
        BodyFixture bodyFixture = new BodyFixture(registeredShipWreck.getPolygon());
        bodyFixture.setDensity(PhysicsUtils.DEFAULT_FIXTURE_DENSITY);
        bodyFixture.setFilter(new WreckFilter(this));
        body.addFixture(bodyFixture);
    }

    @Override
    protected void updateLifeTime() {
        lifeTime += 60.0f * TimeUtils.UPDATE_DELTA_TIME;
    }

    @Override
    protected void updateFire() {
        if (fire) {
            float fireSpeed = 0.120f * TimeUtils.UPDATE_DELTA_TIME;

            if (fireFadingOut) {
                if (colorFire.w > 0.7f) {
                    colorFire.w -= fireSpeed;
                    if (colorFire.w < 0.0f) {
                        colorFire.w = 0.0f;
                    }
                } else {
                    fireFadingOut = false;
                }
            } else {
                if (colorFire.w < 1.0f) {
                    colorFire.w += fireSpeed;
                } else {
                    fireFadingOut = true;
                }
            }
        }

        if (lifeTime >= maxLifeTime / 2.0f) {
            if (colorFire.w > 0.0f) {
                float fireSpeed = 0.120F * TimeUtils.UPDATE_DELTA_TIME;

                fire = false;
                colorFire.w -= fireSpeed;
                if (colorFire.w < 0.0f) {
                    colorFire.w = 0.0f;
                }
            }
        }
    }

    @Override
    public void setDead() {
        this.isDead = true;
        if (color.w > 0.01f) {
            Vector2 worldPos = body.getLocalCenter().add(position.x, position.y);
            ParticleSpawner.spawnSmallExplosion((float) worldPos.x, (float) worldPos.y, getScale().x);
        }
    }

    @Override
    public void onRemoved() {
        ParticleSpawner.PARTICLE_SHIP_WREAK_POOL.returnBack(this);
        Core.get().getWorld().removePhysicObject(this);
    }
}