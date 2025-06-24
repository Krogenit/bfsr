package net.bfsr.server.module;

import lombok.RequiredArgsConstructor;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.shield.Shield;
import net.bfsr.module.CommonShieldLogic;
import net.bfsr.network.packet.server.component.PacketShieldRebuild;
import net.bfsr.network.packet.server.component.PacketShieldRebuildingTime;
import net.bfsr.network.packet.server.component.PacketShieldRemove;
import net.bfsr.server.ServerGameLogic;
import net.bfsr.server.entity.EntityTrackingManager;

@RequiredArgsConstructor
public class ShieldLogic extends CommonShieldLogic {
    private final ServerGameLogic gameLogic = ServerGameLogic.get();
    private final EntityTrackingManager trackingManager;

    @Override
    public void update(Shield shield) {
        super.update(shield);

        if (shield.getRebuildingTime() < shield.getTimeToRebuild()) {
            shield.rebuilding();

            if (shield.getRebuildingTime() >= shield.getTimeToRebuild()) {
                shield.rebuildShield();

                Ship ship = shield.getShip();
                trackingManager.sendPacketToPlayersTrackingEntity(ship.getId(),
                        new PacketShieldRebuild(ship.getId(), gameLogic.getFrame()));
            }
        }
    }

    @Override
    public void onShieldRemove(Shield shield) {
        Ship ship = shield.getShip();
        trackingManager.sendPacketToPlayersTrackingEntity(ship.getId(), new PacketShieldRemove(ship.getId(), gameLogic.getFrame()));
    }

    @Override
    public void onRebuildingTimeUpdate(Shield shield) {
        Ship ship = shield.getShip();
        trackingManager.sendPacketToPlayersTrackingEntity(ship.getId(),
                new PacketShieldRebuildingTime(ship.getId(), 0, gameLogic.getFrame()));
    }
}