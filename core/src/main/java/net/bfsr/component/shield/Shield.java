package net.bfsr.component.shield;

import net.bfsr.client.particle.EnumParticlePositionType;
import net.bfsr.client.particle.ParticleSpawner;
import net.bfsr.client.render.OpenGLHelper;
import net.bfsr.client.shader.BaseShader;
import net.bfsr.client.sound.SoundRegistry;
import net.bfsr.client.sound.SoundSourceEffect;
import net.bfsr.collision.filter.ShipFilter;
import net.bfsr.core.Core;
import net.bfsr.entity.CollisionObject;
import net.bfsr.entity.ship.Ship;
import net.bfsr.network.packet.server.PacketShieldRebuild;
import net.bfsr.network.packet.server.PacketShieldRebuildingTime;
import net.bfsr.network.packet.server.PacketShieldRemove;
import net.bfsr.server.MainServer;
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

public class Shield extends CollisionObject {
    private float shield, maxShield;
    private float shieldRegen;
    private Vector2f radius;
    private Vector2f diameter;
    private float timeToRebuild, rebuildingTime;
    private float size;
    private final Ship ship;
    private boolean alive;

    protected Shield(Ship ship) {
        world = ship.getWorld();
        this.ship = ship;
        size = 1.0f;
    }

    @Override
    protected void createBody(Vector2f pos) {
        super.createBody(pos);

        List<BodyFixture> shipFixtures = ship.getBody().getFixtures();
        for (int i = 0; i < shipFixtures.size(); i++) {
            BodyFixture fixture = shipFixtures.get(i);
            Object userData = fixture.getUserData();
            if (userData instanceof Shield) {
                ship.getBody().removeFixture(fixture);
                i--;
            }
        }

        radius = new Vector2f();
        for (BodyFixture bodyFixture : ship.getBody().getFixtures()) {
            Convex convex = bodyFixture.getShape();
            if (convex instanceof Polygon polygon) {
                for (Vector2 vertex : polygon.getVertices()) {
                    float x = (float) Math.abs(vertex.x);
                    if (x > radius.x) {
                        radius.x = x;
                    }
                    float y = (float) Math.abs(vertex.y);
                    if (y > radius.y) {
                        radius.y = y;
                    }
                }
            }

        }

        float offset = 14.0f;
        diameter = new Vector2f(radius.x * 2.0f + offset, radius.y * 2.0f + offset);

        Polygon ellipse = Geometry.createPolygonalEllipse(16, diameter.x, diameter.y);
        BodyFixture bodyFixture = new BodyFixture(ellipse);
        bodyFixture.setUserData(this);
        bodyFixture.setDensity(0.0001f);
        bodyFixture.setFriction(0.0f);
        bodyFixture.setRestitution(0.1f);
        bodyFixture.setFilter(new ShipFilter(ship));
        ship.getBody().addFixture(bodyFixture);
        ship.recalculateMass();
        diameter.x += 1.0f;
        diameter.y += 1.0f;
        alive = true;
    }

    @Override
    public void update() {
        if (!world.isRemote() && alive && shield <= 0) {
            removeShield();
        }

        if (shield < maxShield && shieldAlive()) {
            shield += shieldRegen * 0.01666666753590107f;

            if (world.isRemote() && size < 1.0f) {
                size += 0.06f;
                if (size > 1.0f) size = 1.0f;
            }

            if (shield > maxShield) {
                shield = maxShield;
            }
        }

        if (!world.isRemote() && rebuildingTime < timeToRebuild) {
            rebuildingTime += 1;

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

        if (world.isRemote()) {
            Vector3f shipEffectColor = ship.getEffectsColor();
            Vector4f color = new Vector4f(shipEffectColor.x, shipEffectColor.y, shipEffectColor.z, 1.0f);
            ParticleSpawner.spawnLight(getPosition(), ship.getScale().x * 2.0f, color, 0.04f * 60.0f, false, EnumParticlePositionType.Default);
            if (ship.getWorld().getRand().nextInt(2) == 0) {
                Core.getCore().getSoundManager().play(new SoundSourceEffect(SoundRegistry.shieldUp0, getPosition()));
            } else {
                Core.getCore().getSoundManager().play(new SoundSourceEffect(SoundRegistry.shieldUp1, getPosition()));
            }
        } else {
            MainServer.getInstance().getNetworkSystem().sendPacketToAllNearby(new PacketShieldRebuild(ship.getId()), ship.getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
        }

        createBody(ship.getPosition());
    }

    public boolean damage(float shieldDamage) {
        if (shield > 0) {
            shield -= shieldDamage;
            return true;
        } else {
            if (!world.isRemote()) {
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
        List<BodyFixture> shipFixtures = ship.getBody().getFixtures();
        for (int i = 0; i < shipFixtures.size(); i++) {
            BodyFixture fixture = shipFixtures.get(i);
            Object userData = fixture.getUserData();
            if (userData instanceof Shield) {
                ship.getBody().removeFixture(fixture);
                i--;
            }
        }

        ship.recalculateMass();

        if (world.isRemote()) {
            Vector3f shipEffectColor = ship.getEffectsColor();
            Vector4f color = new Vector4f(shipEffectColor.x, shipEffectColor.y, shipEffectColor.z, 1.0f);
            ParticleSpawner.spawnLight(getPosition(), ship.getScale().x * 2.0f, 5.0f * 60.0f, color, 0.04f * 60.0f, false, EnumParticlePositionType.Default);
            ParticleSpawner.spawnDisableShield(getPosition(), ship.getScale().x * 4.0f, -240.0f, new Vector4f(color));
            Core.getCore().getSoundManager().play(new SoundSourceEffect(SoundRegistry.shieldDown, ship.getPosition()));
        } else {
            MainServer.getInstance().getNetworkSystem().sendPacketToAllNearby(new PacketShieldRemove(ship.getId()), ship.getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
        }

        rebuildingTime = 0;
        size = 0.0f;
        shield = 0;
        alive = false;
    }

    @Override
    public void render(BaseShader shader) {
        if (shieldAlive()) {
            OpenGLHelper.alphaGreater(0.01f);
            super.render(shader);
        }
    }

    @Override
    public float getRotation() {
        return ship.getRotation();
    }

    @Override
    public Vector2f getPosition() {
        return ship.getPosition();
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

    public Ship getShip() {
        return ship;
    }

    @Override
    public Vector2f getScale() {
        return new Vector2f(diameter.x * size, diameter.y * size);
    }

    public float getShield() {
        return shield;
    }

    public float getMaxShield() {
        return maxShield;
    }

    public float getSize() {
        return size;
    }
}
