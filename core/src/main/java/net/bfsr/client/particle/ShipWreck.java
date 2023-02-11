package net.bfsr.client.particle;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.client.renderer.texture.TextureLoader;
import net.bfsr.client.sound.SoundRegistry;
import net.bfsr.client.sound.SoundSourceEffect;
import net.bfsr.collision.filter.WreckFilter;
import net.bfsr.core.Core;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.wreck.RegisteredShipWreck;
import net.bfsr.entity.wreck.WreckRegistry;
import net.bfsr.network.packet.server.PacketRemoveObject;
import net.bfsr.physics.PhysicsUtils;
import net.bfsr.server.MainServer;
import net.bfsr.util.TimeUtils;
import net.bfsr.world.WorldServer;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Vector2;
import org.joml.Vector2f;

@NoArgsConstructor
public class ShipWreck extends Wreck {
    private RegisteredShipWreck registeredShipWreck;
    @Getter
    private float lifeTime, maxLifeTime;

    public ShipWreck init(int wreckIndex, Ship ship, float x, float y, float velocityX, float velocityY, float rotation, float angularVelocity, float scaleX, float scaleY,
                          float r, float g, float b, float a, float alphaVelocity, int id, float lifeTime) {
        registeredShipWreck = WreckRegistry.INSTANCE.getWrecks(ship.getType())[wreckIndex];
        init(ship.getWorld(), id, x, y, velocityX, velocityY, rotation, angularVelocity, scaleX, scaleY, r, g, b, a, alphaVelocity,
                TextureLoader.getTexture(registeredShipWreck.getTexture()), wreckIndex, true, true, true, ship.getHull().getMaxHull() / 4.0f, ship.getId(),
                ship.getWorld().getRand(), 1.0f, 1.0f, 1.0f, 0.0f, TextureLoader.getTexture(registeredShipWreck.getFireTexture()),
                TextureLoader.getTexture(registeredShipWreck.getSparkleTexture()), 200.0f + ship.getWorld().getRand().nextInt(200));
        this.lifeTime = maxLifeTime = lifeTime;
        return this;
    }

    public ShipWreck init(int id, int wreckIndex, Ship ship, float x, float y, float velocityX, float velocityY, float rotation, float angularVelocity, float scaleX, float scaleY,
                          float r, float g, float b, float a, float alphaVelocity, float lifeTime) {
        registeredShipWreck = WreckRegistry.INSTANCE.getWrecks(ship.getType())[wreckIndex];
        init(ship.getWorld(), id, x, y, velocityX, velocityY, rotation, angularVelocity, scaleX, scaleY, r, g, b, a, alphaVelocity, null, wreckIndex, true, true, true,
                ship.getHull().getMaxHull() / 4.0f, ship.getId(), ship.getWorld().getRand(), 0.0f, 0.0f, 0.0f, 0.0f, null, null, 0.0f);
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
        BodyFixture bodyFixture = new BodyFixture(registeredShipWreck.getPolygon());
        bodyFixture.setDensity(PhysicsUtils.DEFAULT_FIXTURE_DENSITY);
        bodyFixture.setFilter(new WreckFilter(this));
        body.addFixture(bodyFixture);
    }

    @Override
    protected void updateLifeTime() {
        lifeTime -= 60.0f * TimeUtils.UPDATE_DELTA_TIME;
        if (lifeTime <= 0) {
            if (!world.isRemote()) {
                MainServer.getInstance().getNetworkSystem().sendPacketToAllNearby(new PacketRemoveObject(this), getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
                setDead(true);
            }
        }
    }

    @Override
    protected void updateFire() {
        float fireSpeed = 0.120f * TimeUtils.UPDATE_DELTA_TIME;
        float fireAddSpeed = 0.0300f * TimeUtils.UPDATE_DELTA_TIME;

        if (changeFire) {
            if (colorFire.w > 0.7f) {
                colorFire.w -= fireSpeed - fireAddSpeed;
                colorFire.x -= fireSpeed;
                colorFire.y -= fireSpeed / 4.0f;
                colorFire.z -= fireSpeed / 4.0f;
                if (colorFire.w < 0.0f) {
                    colorFire.set(0.0f);
                }
            } else {
                changeFire = false;
            }
        } else {
            if (colorFire.w < 1.0f) {
                colorFire.w += fireSpeed + fireAddSpeed;
                colorFire.x += fireSpeed;
                colorFire.y += fireSpeed / 4.0f;
                colorFire.z += fireSpeed / 4.0f;
            } else {
                changeFire = true;
            }
        }

        if (lifeTime <= maxLifeTime / 2.0f) {
            if (colorFire.w > 0.0f) {
                fireSpeed = 0.120F * TimeUtils.UPDATE_DELTA_TIME;

                fire = false;
                colorFire.w -= fireSpeed;
                colorFire.x -= fireSpeed;
                colorFire.y -= fireSpeed;
                colorFire.z -= fireSpeed;
                if (colorFire.w < 0.0f) {
                    colorFire.set(0.0f);
                }
            }
        }
    }

    @Override
    protected void updateSparkleFading() {
        if (lifeTime <= maxLifeTime / 7.0f) {
            if (colorLight.w > 0.0f) {
                float lightSpeed = 12.0f * TimeUtils.UPDATE_DELTA_TIME;

                light = false;
                colorLight.w -= lightSpeed;
                colorLight.x -= lightSpeed;
                colorLight.y -= lightSpeed;
                colorLight.z -= lightSpeed;
                if (colorLight.w < 0.0f) {
                    colorLight.set(0.0f);
                }
            }
        }
    }

    @Override
    public void setDead(boolean isDead) {
        Vector2f velocity = getVelocity();
        if (world.isRemote()) {
            if (color.w > 0.01f) {
                Vector2 worldPos = body.getLocalCenter().add(position.x, position.y);
                Vector2f scale = getScale();
                ParticleSpawner.spawnLight((float) worldPos.x, (float) worldPos.y, getScale().x * 2.0f, 1.0f, 0.8f, 0.6f, 1.0f, RenderLayer.DEFAULT_ADDITIVE);
                ParticleSpawner.spawnSpark((float) worldPos.x, (float) worldPos.y, getScale().x);
                ParticleSpawner.spawnExplosion((float) worldPos.x, (float) worldPos.y, getScale().x);
                ParticleSpawner.spawnSmallGarbage(random.nextInt(10), (float) worldPos.x, (float) worldPos.y, 2.0f, 5.0f + getScale().x);
                ParticleSpawner.spawnShipOst(random.nextInt(3), (float) worldPos.x, (float) worldPos.y, velocity.x * 0.06f, velocity.y * 0.06f, 0.25f + 0.75f * random.nextFloat());

                ParticleSpawner.spawnMediumGarbage(3, (float) worldPos.x, (float) worldPos.y, velocity.x * 0.1f, velocity.y * 0.1f, scale.x / 2.0f);
                Core.get().getSoundManager().play(new SoundSourceEffect(SoundRegistry.explosion0, (float) worldPos.x, (float) worldPos.y));
            }
        } else {
            if (color.w > 0.01f) {
                Vector2 worldPos = body.getLocalCenter().add(position.x, position.y);
                ParticleSpawner.spawnDamageDebris(world, random.nextInt(3), (float) worldPos.x, (float) worldPos.y, velocity.x, velocity.y, 1.0f);
                ParticleSpawner.spawnDamageWrecks(world, random.nextInt(2), (float) worldPos.x, (float) worldPos.y, velocity.x, velocity.y);
            }
        }

        this.isDead = isDead;
    }
}
