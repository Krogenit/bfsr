package net.bfsr.client.particle;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.client.renderer.texture.TextureLoader;
import net.bfsr.client.renderer.texture.TextureRegister;
import net.bfsr.client.sound.SoundRegistry;
import net.bfsr.client.sound.SoundSourceEffect;
import net.bfsr.collision.filter.WreckFilter;
import net.bfsr.core.Core;
import net.bfsr.entity.ship.Ship;
import net.bfsr.network.packet.server.PacketRemoveObject;
import net.bfsr.physics.PhysicsUtils;
import net.bfsr.server.MainServer;
import net.bfsr.util.TimeUtils;
import net.bfsr.world.WorldServer;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Polygon;
import org.dyn4j.geometry.Vector2;
import org.joml.Vector2f;

@NoArgsConstructor
public class ShipWreck extends Wreck {
    private TextureRegister textureWreck;
    @Getter
    private float lifeTime, maxLifeTime;

    public ShipWreck init(int textureOffset, Ship ship, float x, float y, float velocityX, float velocityY, float rotation, float angularVelocity, float scaleX, float scaleY,
                          float r, float g, float b, float a, float alphaVelocity, int id, float lifeTime) {
        init(ship.getWorld(), id, x, y, velocityX, velocityY, rotation, angularVelocity, scaleX, scaleY, r, g, b, a, alphaVelocity,
                TextureLoader.getTexture(ship.getWreckTexture(textureOffset)), textureOffset, true, true, true, ship.getHull().getMaxHull() / 4.0f, ship.getId(),
                ship.getWorld().getRand(), 1.0f, 1.0f, 1.0f, 0.0f, TextureLoader.getTexture(ship.getWreckFireTexture(textureOffset)),
                TextureLoader.getTexture(ship.getWreckLightTexture(textureOffset)), 200.0f + ship.getWorld().getRand().nextInt(200));
        this.textureWreck = ship.getWreckTexture(textureOffset);
        this.lifeTime = maxLifeTime = lifeTime;
        return this;
    }

    public ShipWreck init(int id, int textureOffset, Ship ship, float x, float y, float velocityX, float velocityY, float rotation, float angularVelocity, float scaleX, float scaleY,
                          float r, float g, float b, float a, float alphaVelocity) {
        init(ship.getWorld(), id, x, y, velocityX, velocityY, rotation, angularVelocity, scaleX, scaleY, r, g, b, a, alphaVelocity, null, textureOffset, true, true, true,
                ship.getHull().getMaxHull() / 4.0f, ship.getId(), ship.getWorld().getRand(), 0.0f, 0.0f, 0.0f, 0.0f, null, null, 0.0f);
        this.textureWreck = ship.getWreckTexture(textureOffset);
        return this;
    }

    @Override
    protected void setLinearAndAngularDamping() {
        body.setLinearDamping(0.2f);
        body.setAngularDamping(0.025f);
    }

    @Override
    protected void createFixtures() {
        Vector2[] vertices;
        if (textureWreck == TextureRegister.particleWreckEngiSmall0Wreck0) {
            vertices = new Vector2[6];
            vertices[0] = new Vector2(-16.80f, 20.20f);
            vertices[1] = new Vector2(-32.80f, 4.60f);
            vertices[2] = new Vector2(-32.00f, -11.40f);
            vertices[3] = new Vector2(-15.60f, -20.20f);
            vertices[4] = new Vector2(-0.40f, -19.00f);
            vertices[5] = new Vector2(-4.00f, 16.60f);
        } else if (textureWreck == TextureRegister.particleWreckEngiSmall0Wreck1) {
            vertices = new Vector2[5];
            vertices[0] = new Vector2(14.00f, 13.40f);
            vertices[1] = new Vector2(-6.80f, -5.00f);
            vertices[2] = new Vector2(7.60f, -21.00f);
            vertices[3] = new Vector2(33.60f, -7.00f);
            vertices[4] = new Vector2(34.40f, 2.60f);
        } else if (textureWreck == TextureRegister.particleWreckHumanSmall0Wreck0) {
            vertices = new Vector2[5];
            vertices[0] = new Vector2(-11.75f, 21.80f);
            vertices[1] = new Vector2(-25.35f, 6.60f);
            vertices[2] = new Vector2(-20.55f, -17.00f);
            vertices[3] = new Vector2(-4.55f, -19.80f);
            vertices[4] = new Vector2(1.45f, 11.40f);
        } else if (textureWreck == TextureRegister.particleWreckHumanSmall0Wreck1) {
            vertices = new Vector2[4];
            vertices[0] = new Vector2(7.05f, 30.20f);
            vertices[1] = new Vector2(-8.95f, 27.00f);
            vertices[2] = new Vector2(0.65f, -21.00f);
            vertices[3] = new Vector2(25.85f, -3.00f);
        } else if (textureWreck == TextureRegister.particleWreckSaimonSmall0Wreck0) {
            vertices = new Vector2[4];
            vertices[0] = new Vector2(-44.94f, 15.66f);
            vertices[1] = new Vector2(-27.98f, -10.42f);
            vertices[2] = new Vector2(11.13f, -30.42f);
            vertices[3] = new Vector2(-7.56f, 6.96f);
        } else if (textureWreck == TextureRegister.particleWreckSaimonSmall0Wreck1) {
            vertices = new Vector2[3];
            vertices[0] = new Vector2(2.88f, 20.00f);
            vertices[1] = new Vector2(-5.82f, -9.56f);
            vertices[2] = new Vector2(45.47f, -0.86f);
        } else {
            vertices = new Vector2[4];
            vertices[0] = new Vector2(7.21f, 9.16f);
            vertices[1] = new Vector2(-9.19f, 4.76f);
            vertices[2] = new Vector2(-7.99f, -10.44f);
            vertices[3] = new Vector2(8.81f, -7.24f);
        }

        for (int i = 0; i < vertices.length; i++) {
            Vector2 vertex = vertices[i];
            vertex.divide(10.0f);
        }

        BodyFixture bodyFixture = new BodyFixture(new Polygon(vertices));
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
