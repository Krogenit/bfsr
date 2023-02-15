package net.bfsr.server.component;

import net.bfsr.component.weapon.WeaponSlotCommon;
import net.bfsr.entity.ship.ShipCommon;
import net.bfsr.server.MainServer;
import net.bfsr.server.network.packet.common.PacketWeaponShoot;
import net.bfsr.server.world.WorldServer;

public abstract class WeaponSlot extends WeaponSlotCommon {
    protected WeaponSlot(ShipCommon ship, float shootTimerMax, float energyCost, float bulletSpeed, float alphaReducer, float scaleX, float scaleY) {
        super(ship, shootTimerMax, energyCost, bulletSpeed, alphaReducer, scaleX, scaleY);
    }

    @Override
    protected void shoot() {
        MainServer.getInstance().getNetworkSystem().sendPacketToAllNearby(new PacketWeaponShoot(ship.getId(), id), ship.getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
        createBullet();
        shootTimer = shootTimerMax;
        ship.getReactor().consume(energyCost);
    }

    @Override
    protected void spawnShootParticles() {

    }
}
