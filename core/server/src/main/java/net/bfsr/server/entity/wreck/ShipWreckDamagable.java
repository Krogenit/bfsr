package net.bfsr.server.entity.wreck;

import clipper2.core.Path64;
import clipper2.core.PathsD;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.bfsr.physics.PhysicsUtils;
import net.bfsr.server.collision.filter.ShipWreckFilter;
import net.bfsr.server.core.Server;
import net.bfsr.server.damage.Damagable;
import net.bfsr.server.damage.DamageMask;
import net.bfsr.server.damage.DamageUtils;
import net.bfsr.server.entity.CollisionObject;
import net.bfsr.server.entity.bullet.Bullet;
import net.bfsr.server.network.packet.server.entity.wreck.PacketShipWreck;
import net.bfsr.server.world.WorldServer;
import org.dyn4j.dynamics.BodyFixture;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class ShipWreckDamagable extends CollisionObject implements Damagable {
    @Getter
    private final DamageMask mask;
    @Getter
    private final PathsD contours;
    @Getter
    private final List<BodyFixture> fixturesToAdd = new ArrayList<>();
    private int lifeTime;
    private final int maxLifeTime = 1200;
    @Getter
    private final int textureIndex;

    public ShipWreckDamagable(float x, float y, float sin, float cos, float scaleX, float scaleY, int textureIndex, DamageMask mask, PathsD contours) {
        super(Server.getInstance().getWorld(), Server.getInstance().getWorld().getNextId(), x, y, sin, cos, scaleX, scaleY);
        this.textureIndex = textureIndex;
        this.mask = mask;
        this.contours = contours;
    }

    public void attackFromBullet(Bullet bullet, float contactX, float contactY, float normalX, float normalY) {
        float polygonRadius = 1.75f;
        float radius = 4.0f;

        double x = body.getTransform().getTranslationX();
        double y = body.getTransform().getTranslationY();
        double sin = body.getTransform().getSint();
        double cos = body.getTransform().getCost();

        Path64 clip = DamageUtils.createCirclePath(contactX - x, contactY - y, -sin, cos, 12, polygonRadius);

        DamageUtils.damage(this, contactX, contactY, clip, radius);
    }

    @Override
    public void setupFixture(BodyFixture bodyFixture) {
        bodyFixture.setFilter(new ShipWreckFilter(this));
        bodyFixture.setDensity(PhysicsUtils.DEFAULT_FIXTURE_DENSITY);
    }

    @Override
    public void update() {
        if (lifeTime++ >= maxLifeTime) {
            setDead();
        }
    }

    @Override
    public void sendSpawnPacket() {
        Server.getInstance().getNetworkSystem().sendTCPPacketToAllNearby(new PacketShipWreck(this), getX(), getY(), WorldServer.PACKET_SPAWN_DISTANCE);
    }

    @Override
    public void destroy() {
        setDead();
    }

    @Override
    public float getX() {
        return (float) body.getTransform().getTranslationX();
    }

    @Override
    public float getY() {
        return (float) body.getTransform().getTranslationY();
    }

    @Override
    public float getSin() {
        return (float) body.getTransform().getSint();
    }

    @Override
    public float getCos() {
        return (float) body.getTransform().getCost();
    }
}