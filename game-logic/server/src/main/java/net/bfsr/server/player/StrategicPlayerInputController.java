package net.bfsr.server.player;

import net.bfsr.network.packet.server.component.PacketWeaponSlotShoot;
import net.bfsr.server.ai.AiFactory;
import net.bfsr.server.entity.EntityTrackingManager;
import net.bfsr.server.network.handler.PlayerNetworkHandler;

public class StrategicPlayerInputController extends PlayerInputController {
    public StrategicPlayerInputController(Player player, PlayerNetworkHandler networkHandler,
                                          EntityTrackingManager trackingManager, AiFactory aiFactory) {
        super(player, networkHandler, trackingManager, aiFactory);
    }

    @Override
    public void update(int frame) {
        if (ship == null) {
            return;
        }

        if (mouseStates[0]) {
            ship.shoot(weaponSlot -> {
                weaponSlot.createBullet(false);
                trackingManager.sendPacketToPlayersTrackingEntity(ship.getId(), new PacketWeaponSlotShoot(
                        ship.getId(), weaponSlot.getId(), gameLogic.getFrame()));
            });
        }
    }
}
