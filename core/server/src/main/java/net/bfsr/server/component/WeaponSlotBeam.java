package net.bfsr.server.component;

import net.bfsr.component.weapon.WeaponSlotBeamCommon;
import net.bfsr.entity.bullet.BulletDamage;
import net.bfsr.entity.ship.ShipCommon;
import net.bfsr.server.MainServer;
import net.bfsr.server.network.packet.common.PacketWeaponShoot;
import net.bfsr.server.world.WorldServer;
import net.bfsr.util.TimeUtils;
import org.joml.Vector4f;

public abstract class WeaponSlotBeam extends WeaponSlotBeamCommon {
    protected WeaponSlotBeam(ShipCommon ship, float beamMaxRange, BulletDamage damage, Vector4f beamColor, float shootTimerMax, float energyCost, float scaleX, float scaleY) {
        super(ship, beamMaxRange, damage, beamColor, shootTimerMax, energyCost, scaleX, scaleY);
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
        MainServer.getInstance().getNetworkSystem().sendPacketToAllNearby(new PacketWeaponShoot(ship.getId(), id), ship.getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
        shootTimer = shootTimerMax;
        ship.getReactor().consume(energyCost);
    }
}
