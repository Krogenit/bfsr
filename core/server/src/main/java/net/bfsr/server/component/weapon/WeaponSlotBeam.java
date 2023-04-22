package net.bfsr.server.component.weapon;

import lombok.Getter;
import net.bfsr.component.weapon.WeaponType;
import net.bfsr.config.weapon.beam.BeamData;
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

public class WeaponSlotBeam extends WeaponSlot {
    private final Vector2 start = new Vector2();
    private final BeamFilter filter = new BeamFilter(null);
    @Getter
    private final float beamMaxRange;
    @Getter
    private float currentBeamRange;
    private float beamPower;
    private boolean maxPower;
    @Getter
    private final Vector2f collisionPoint = new Vector2f();
    private final BulletDamage damage;
    private final Ray ray = new Ray(0);
    private final DetectFilter<Body, BodyFixture> detectFilter = new DetectFilter<>(true, true, filter);
    private final Vector2 rayDirection = new Vector2();

    public WeaponSlotBeam(BeamData beamData) {
        super(beamData, WeaponType.BEAM);
        this.beamMaxRange = beamData.getBeamMaxRange();
        this.damage = beamData.getDamage();
    }

    @Override
    public void update() {
        super.update();

        if (reloadTimer > 0) {
            if (reloadTimer <= timeToReload / 3.0f) {
                maxPower = false;
                if (beamPower > 0.0f) {
                    beamPower -= 3.5f * TimeUtils.UPDATE_DELTA_TIME;
                    if (beamPower < 0) beamPower = 0;
                }
            } else {
                if (!maxPower && beamPower < 1.0f) {
                    beamPower += 3.5f * TimeUtils.UPDATE_DELTA_TIME;
                    if (beamPower > 1.0f) beamPower = 1.0f;
                } else {
                    maxPower = true;
                }

                if (maxPower) {
                    beamPower = world.getRand().nextFloat() / 3.0f + 0.66f;
                }
            }

            rayCast();
        } else {
            if (beamPower > 0.0f) {
                beamPower -= 3.5f * TimeUtils.UPDATE_DELTA_TIME;
                if (beamPower < 0) beamPower = 0;
            }
        }
    }

    @Override
    protected void shoot() {
        Server.getInstance().getNetworkSystem().sendUDPPacketToAllNearby(new PacketWeaponShoot(ship.getId(), id), ship.getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
        reloadTimer = timeToReload;
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
                    ship.attackShip(damage, this.ship, collisionPoint.x, collisionPoint.y, ship.getFaction() == this.ship.getFaction() ? beamPower / 2.0f * 60.0f * TimeUtils.UPDATE_DELTA_TIME :
                            beamPower * 60.0f * TimeUtils.UPDATE_DELTA_TIME);
                } else if (userData instanceof Wreck wreck) {
                    wreck.damage(damage.getHull() * beamPower);
                }
            }
        }
    }

    @Override
    protected void createBullet() {}
}