package net.bfsr.component.shield;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.client.particle.ParticleSpawner;
import net.bfsr.client.particle.RenderLayer;
import net.bfsr.client.renderer.instanced.BufferType;
import net.bfsr.client.renderer.instanced.SpriteRenderer;
import net.bfsr.client.renderer.texture.Texture;
import net.bfsr.client.sound.SoundRegistry;
import net.bfsr.client.sound.SoundSourceEffect;
import net.bfsr.collision.filter.ShipFilter;
import net.bfsr.core.Core;
import net.bfsr.entity.ship.Ship;
import net.bfsr.network.packet.server.PacketShieldRebuild;
import net.bfsr.network.packet.server.PacketShieldRebuildingTime;
import net.bfsr.network.packet.server.PacketShieldRemove;
import net.bfsr.physics.PhysicsUtils;
import net.bfsr.server.MainServer;
import net.bfsr.util.TimeUtils;
import net.bfsr.world.WorldServer;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Convex;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.Polygon;
import org.dyn4j.geometry.Vector2;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.List;

public class Shield {
    @Setter
    private Texture texture;
    @Getter
    private final Vector4f color = new Vector4f();
    @Getter
    private float shield, maxShield;
    private float shieldRegen;
    private Vector2f radius;
    private Vector2f diameter;
    private float timeToRebuild, rebuildingTime;
    @Getter
    private float size;
    private final Ship ship;
    private boolean alive;
    private BodyFixture shieldFixture;

    protected Shield(Ship ship) {
        this.ship = ship;
        this.size = 1.0f;
    }

    protected void createBody(float x, float y) {
        List<BodyFixture> fixtures = ship.getBody().getFixtures();
        if (shieldFixture != null) {
            ship.getBody().removeFixture(shieldFixture);
            shieldFixture = null;
        }

        radius = new Vector2f();
        for (int i = 0; i < fixtures.size(); i++) {
            BodyFixture bodyFixture = fixtures.get(i);
            Convex convex = bodyFixture.getShape();
            if (convex instanceof Polygon polygon) {
                for (Vector2 vertex : polygon.getVertices()) {
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
        diameter = new Vector2f(radius.x * 2.0f + offset, radius.y * 2.0f + offset);

        Polygon ellipse = Geometry.createPolygonalEllipse(16, diameter.x, diameter.y);
        shieldFixture = new BodyFixture(ellipse);
        shieldFixture.setUserData(this);
        shieldFixture.setDensity(PhysicsUtils.SHIELD_FIXTURE_DENSITY);
        shieldFixture.setFriction(0.0f);
        shieldFixture.setRestitution(0.1f);
        shieldFixture.setFilter(new ShipFilter(ship));
        ship.getBody().addFixture(shieldFixture);
        ship.recalculateMass();
        diameter.x += 0.1f;
        diameter.y += 0.1f;
        alive = true;
    }

    public void update() {
        if (!ship.getWorld().isRemote() && alive && shield <= 0) {
            removeShield();
        }

        if (shield < maxShield && shieldAlive()) {
            shield += shieldRegen * TimeUtils.UPDATE_DELTA_TIME;

            if (ship.getWorld().isRemote() && size < 1.0f) {
                size += 3.6f * TimeUtils.UPDATE_DELTA_TIME;
                if (size > 1.0f) size = 1.0f;
            }

            if (shield > maxShield) {
                shield = maxShield;
            }
        }

        if (!ship.getWorld().isRemote() && rebuildingTime < timeToRebuild) {
            rebuildingTime += 60.0f * TimeUtils.UPDATE_DELTA_TIME;

            if (rebuildingTime >= timeToRebuild) {
                rebuildShield();
            }
        }
    }

    public boolean shieldAlive() {
        return rebuildingTime >= timeToRebuild;
    }

    public void rebuildShield() {
        shield = maxShield / 5.0f;
        rebuildingTime = timeToRebuild;

        Vector2f position = ship.getPosition();
        if (ship.getWorld().isRemote()) {
            Vector3f shipEffectColor = ship.getEffectsColor();
            ParticleSpawner.spawnLight(position.x, position.y, ship.getScale().x * 2.0f, shipEffectColor.x, shipEffectColor.y, shipEffectColor.z, 1.0f, 0.04f * 60.0f, false,
                    RenderLayer.DEFAULT_ADDITIVE);
            if (ship.getWorld().getRand().nextInt(2) == 0) {
                Core.get().getSoundManager().play(new SoundSourceEffect(SoundRegistry.shieldUp0, position.x, position.y));
            } else {
                Core.get().getSoundManager().play(new SoundSourceEffect(SoundRegistry.shieldUp1, position.x, position.y));
            }
        } else {
            MainServer.getInstance().getNetworkSystem().sendPacketToAllNearby(new PacketShieldRebuild(ship.getId()), ship.getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
        }

        createBody(position.x, position.y);
    }

    public boolean damage(float shieldDamage) {
        if (shield > 0) {
            shield -= shieldDamage;
            return true;
        } else {
            if (!ship.getWorld().isRemote()) {
                setRebuildingTime(0);
                MainServer.getInstance().getNetworkSystem().sendPacketToAllNearby(new PacketShieldRebuildingTime(ship.getId(), 0), ship.getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
            }
        }

        return false;
    }

    public void setRebuildingTime(int time) {
        rebuildingTime = time;
    }

    public void removeShield() {
        ship.getBody().removeFixture(shieldFixture);
        shieldFixture = null;
        ship.recalculateMass();

        Vector2f shipPosition = ship.getPosition();
        if (ship.getWorld().isRemote()) {
            Vector3f shipEffectColor = ship.getEffectsColor();
            Vector2f position = ship.getPosition();
            ParticleSpawner.spawnLight(position.x, position.y, ship.getScale().x * 2.0f, 5.0f * 6.0f, shipEffectColor.x, shipEffectColor.y, shipEffectColor.z, 1.0f, 0.04f * 60.0f, false,
                    RenderLayer.DEFAULT_ADDITIVE);
            ParticleSpawner.spawnDisableShield(position.x, position.y, ship.getScale().x * 4.0f, -24.0f, color.x, color.y, color.z, color.w);
            Core.get().getSoundManager().play(new SoundSourceEffect(SoundRegistry.shieldDown, shipPosition.x, shipPosition.y));
        } else {
            MainServer.getInstance().getNetworkSystem().sendPacketToAllNearby(new PacketShieldRemove(ship.getId()), shipPosition.x, shipPosition.y, WorldServer.PACKET_SPAWN_DISTANCE);
        }

        rebuildingTime = 0;
        size = 0.0f;
        shield = 0;
        alive = false;
    }

    public void render() {
        if (shieldAlive()) {
            float sizeY = diameter.y * size;
            float sizeX = diameter.x * size;
            SpriteRenderer.INSTANCE.addToRenderPipeLineSinCos(ship.getLastPosition().x, ship.getLastPosition().y, ship.getPosition().x, ship.getPosition().y,
                    ship.getLastSin(), ship.getLastCos(), ship.getSin(), ship.getCos(), sizeX, sizeY, color.x, color.y, color.z, color.w, texture, BufferType.ENTITIES_ADDITIVE);
        }
    }

    public void setShield(float shield) {
        this.shield = shield;
    }

    void setMaxShield(float maxShield) {
        this.maxShield = maxShield;
    }

    void setTimeToRebuild(float timeToRebuild) {
        rebuildingTime = timeToRebuild;
        this.timeToRebuild = timeToRebuild;
    }

    void setShieldRegen(float shieldRegen) {
        this.shieldRegen = shieldRegen;
    }

    public void setColor(float r, float g, float b, float a) {
        this.color.set(r, g, b, a);
    }
}
