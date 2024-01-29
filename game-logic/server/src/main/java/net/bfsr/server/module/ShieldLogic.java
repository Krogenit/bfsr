package net.bfsr.server.module;

import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.shield.Shield;
import net.bfsr.module.CommonShieldLogic;
import net.bfsr.network.packet.server.component.PacketShieldRebuild;
import net.bfsr.network.packet.server.component.PacketShieldRemove;
import net.bfsr.server.ServerGameLogic;
import net.bfsr.server.entity.EntityTrackingManager;

public class ShieldLogic extends CommonShieldLogic {
    private final EntityTrackingManager trackingManager = ServerGameLogic.getInstance().getEntityTrackingManager();

    @Override
    public void update(Shield shield) {
        if (shield.isAlive() && shield.getShieldHp() <= 0) {
            shield.removeShield();
            Ship ship = shield.getShip();
            trackingManager.sendPacketToPlayersTrackingEntity(ship.getId(), new PacketShieldRemove(ship.getId(),
                    ship.getWorld().getTimestamp()));
        }

        super.update(shield);

        if (shield.getRebuildingTime() < shield.getTimeToRebuild()) {
            shield.rebuilding();

            if (shield.getRebuildingTime() >= shield.getTimeToRebuild()) {
                shield.rebuildShield();

                Ship ship = shield.getShip();
                trackingManager.sendPacketToPlayersTrackingEntity(ship.getId(), new PacketShieldRebuild(ship.getId(),
                        ship.getWorld().getTimestamp()));
            }
        }
    }
}