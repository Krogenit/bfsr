package net.bfsr.server.component.weapon;

import lombok.Getter;
import net.bfsr.entity.bullet.BulletDamage;
import net.bfsr.server.collision.filter.BeamFilter;
import net.bfsr.server.core.Server;
import net.bfsr.server.entity.ship.Ship;
import net.bfsr.server.entity.wreck.Wreck;
import net.bfsr.server.network.packet.common.PacketWeaponShoot;
import net.bfsr.server.world.WorldServer;
import net.bfsr.util.TimeUtils;
import org.dyn4j.collision.narrowphase.Raycast;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Ray;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.DetectFilter;
import org.dyn4j.world.World;
import org.dyn4j.world.result.RaycastResult;
import org.joml.Vector2f;
import org.joml.Vector4f;

public abstract class WeaponSlotBeam extends WeaponSlot {
    private final Vector2 start = new Vector2();
    private final BeamFilter filter = new BeamFilter(null);
    @Getter
    private final float beamMaxRange;
    @Getter
    private float currentBeamRange;
    protected boolean maxColor;
    @Getter
    private final Vector2f collisionPoint = new Vector2f();
    private final BulletDamage damage;
    @Getter
    protected final Vector4f beamColor;
    private final Ray ray = new Ray(0);
    private final DetectFilter<Body, BodyFixture> detectFilter = new DetectFilter<>(true, true, filter);
    private final Vector2 rayDirection = new Vector2();

    protected WeaponSlotBeam(float beamMaxRange, BulletDamage damage, Vector4f beamColor, float shootTimerMax, float energyCost, float scaleX, float scaleY) {
        super(shootTimerMax, energyCost, Float.MAX_VALUE, 0.0f, scaleX, scaleY);
        this.beamMaxRange = beamMaxRange;
        this.damage = damage;
        this.beamColor = beamColor;
        this.beamColor.w = 0.0f;
    }

    @Override
    public void update() {
        super.update();

        if (shootTimer > 0) {
            if (shootTimer <= shootTimerMax / 3.0f) {
                maxColor = false;
                if (beamColor.w > 0.0f) {
                    beamColor.w -= 3.5f * TimeUtils.UPDATE_DELTA_TIME;
                    if (beamColor.w < 0) beamColor.w = 0;
                }
            } else {
                if (!maxColor && beamColor.w < 1.0f) {
                    beamColor.w += 3.5f * TimeUtils.UPDATE_DELTA_TIME;
                    if (beamColor.w > 1.0f) beamColor.w = 1.0f;
                } else {
                    maxColor = true;
                }

                if (maxColor) {
                    beamColor.w = world.getRand().nextFloat() / 3.0f + 0.66f;
                }
            }

            rayCast();
        } else {
            if (beamColor.w > 0.0f) {
                beamColor.w -= 3.5f * TimeUtils.UPDATE_DELTA_TIME;
                if (beamColor.w < 0) beamColor.w = 0;
            }
        }
    }

    @Override
    protected void shoot() {
        Server.getInstance().getNetworkSystem().sendUDPPacketToAllNearby(new PacketWeaponShoot(ship.getId(), id), ship.getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
        shootTimer = shootTimerMax;
        ship.getReactor().consume(energyCost);
    }

    protected void rayCast() {
        World<Body> physicWorld = world.getPhysicWorld();

        float cos = ship.getCos();
        float sin = ship.getSin();
        float startRange = -scale.x;

        float startX = cos * startRange;
        float startY = sin * startRange;
        start.x = startX + position.x;
        start.y = startY + position.y;
        collisionPoint.x = 0;
        collisionPoint.y = 0;
        filter.setUserData(ship);
        ray.setStart(start);
        rayDirection.set(cos, sin);
        ray.setDirection(rayDirection);
        RaycastResult<Body, BodyFixture> result = physicWorld.raycastClosest(ray, beamMaxRange, detectFilter);
        if (result != null) {
            Body body = result.getBody();
            Raycast raycast = result.getRaycast();
            Vector2 point = raycast.getPoint();
            collisionPoint.x = (float) point.x;
            collisionPoint.y = (float) point.y;
            currentBeamRange = (float) raycast.getDistance();

            Object userData = body.getUserData();

            if (userData != null) {
                if (userData instanceof Ship ship) {
                    ship.attackShip(damage, this.ship, collisionPoint.x, collisionPoint.y, ship.getFaction() == this.ship.getFaction() ? beamColor.w / 2.0f * 60.0f * TimeUtils.UPDATE_DELTA_TIME :
                            beamColor.w * 60.0f * TimeUtils.UPDATE_DELTA_TIME);
                } else if (userData instanceof Wreck wreck) {
                    wreck.damage(damage.getHull() * beamColor.w);
                }
            }
        }
    }

    @Override
    protected void createBullet() {}
}