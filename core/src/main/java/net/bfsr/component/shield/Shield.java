package net.bfsr.component.shield;

import net.bfsr.client.particle.ParticleSpawner;
import net.bfsr.client.particle.RenderLayer;
import net.bfsr.client.render.InstancedRenderer;
import net.bfsr.client.shader.BaseShader;
import net.bfsr.client.sound.SoundRegistry;
import net.bfsr.client.sound.SoundSourceEffect;
import net.bfsr.collision.filter.ShipFilter;
import net.bfsr.core.Core;
import net.bfsr.entity.CollisionObject;
import net.bfsr.entity.ship.Ship;
import net.bfsr.math.ModelMatrixUtils;
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
    private BodyFixture shieldFixture;

    protected Shield(Ship ship) {
        super(ship.getWorld());
        this.ship = ship;
        this.size = 1.0f;
    }

    @Override
    protected void createBody(float x, float y) {
        super.createBody(x, y);

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

    @Override
    public void update() {
        if (!world.isRemote() && alive && shield <= 0) {
            removeShield();
        }

        if (shield < maxShield && shieldAlive()) {
            shield += shieldRegen * TimeUtils.UPDATE_DELTA_TIME;

            if (world.isRemote() && size < 1.0f) {
                size += 3.6f * TimeUtils.UPDATE_DELTA_TIME;
                if (size > 1.0f) size = 1.0f;
            }

            if (shield > maxShield) {
                shield = maxShield;
            }
        }

        if (!world.isRemote() && rebuildingTime < timeToRebuild) {
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

        if (world.isRemote()) {
            Vector3f shipEffectColor = ship.getEffectsColor();
            Vector2f position = getPosition();
            ParticleSpawner.spawnLight(position.x, position.y, ship.getScale().x * 2.0f, shipEffectColor.x, shipEffectColor.y, shipEffectColor.z, 1.0f, 0.04f * 60.0f, false,
                    RenderLayer.DEFAULT_ADDITIVE);
            if (ship.getWorld().getRand().nextInt(2) == 0) {
                Core.getCore().getSoundManager().play(new SoundSourceEffect(SoundRegistry.shieldUp0, position.x, position.y));
            } else {
                Core.getCore().getSoundManager().play(new SoundSourceEffect(SoundRegistry.shieldUp1, position.x, position.y));
            }
        } else {
            MainServer.getInstance().getNetworkSystem().sendPacketToAllNearby(new PacketShieldRebuild(ship.getId()), ship.getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
        }

        Vector2f shipPosition = ship.getPosition();
        createBody(shipPosition.x, shipPosition.y);
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
        ship.getBody().removeFixture(shieldFixture);
        shieldFixture = null;
        ship.recalculateMass();

        Vector2f shipPosition = ship.getPosition();
        if (world.isRemote()) {
            Vector3f shipEffectColor = ship.getEffectsColor();
            Vector2f position = getPosition();
            ParticleSpawner.spawnLight(position.x, position.y, ship.getScale().x * 2.0f, 5.0f * 6.0f, shipEffectColor.x, shipEffectColor.y, shipEffectColor.z, 1.0f, 0.04f * 60.0f, false,
                    RenderLayer.DEFAULT_ADDITIVE);
            ParticleSpawner.spawnDisableShield(position.x, position.y, ship.getScale().x * 4.0f, -24.0f, color.x, color.y, color.z, color.w);
            Core.getCore().getSoundManager().play(new SoundSourceEffect(SoundRegistry.shieldDown, shipPosition.x, shipPosition.y));
        } else {
            MainServer.getInstance().getNetworkSystem().sendPacketToAllNearby(new PacketShieldRemove(ship.getId()), shipPosition.x, shipPosition.y, WorldServer.PACKET_SPAWN_DISTANCE);
        }

        rebuildingTime = 0;
        size = 0.0f;
        shield = 0;
        alive = false;
    }

    @Override
    public void render(BaseShader shader, float interpolation) {
        if (shieldAlive()) {
            InstancedRenderer.INSTANCE.addToRenderPipeLine(ModelMatrixUtils.getDefaultModelMatrix(ship.getLastPosition().x, ship.getLastPosition().y, ship.getPosition().x, ship.getPosition().y,
                    ship.getRotation(), diameter.x * size, diameter.y * size, interpolation), color.x, color.y, color.z, color.w, texture);
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
