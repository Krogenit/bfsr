package net.bfsr.server.network.packet.handler.play.player.input;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.entity.GameObject;
import net.bfsr.entity.ship.Ship;
import net.bfsr.network.packet.PacketHandler;
import net.bfsr.network.packet.client.PacketShipControl;
import net.bfsr.server.ServerGameLogic;
import net.bfsr.server.network.handler.PlayerNetworkHandler;
import net.bfsr.server.player.Player;
import net.bfsr.server.player.PlayerManager;

import java.net.InetSocketAddress;

public class PacketShipControlHandler extends PacketHandler<PacketShipControl, PlayerNetworkHandler> {
    private final PlayerManager playerManager = ServerGameLogic.getInstance().getPlayerManager();

    @Override
    public void handle(PacketShipControl packet, PlayerNetworkHandler playerNetworkHandler, ChannelHandlerContext ctx,
                       InetSocketAddress remoteAddress) {
        GameObject obj = playerNetworkHandler.getWorld().getEntityById(packet.getId());
        if (obj instanceof Ship ship) {
            Player player = playerNetworkHandler.getPlayer();
            if (packet.isControl()) {
                player.getPlayerInputController().setShip(ship);
                playerManager.setPlayerControlledShip(player, ship);
            } else {
                player.getPlayerInputController().setShip(null);
                playerManager.removePlayerControlledShip(ship);
            }
        }
    }
}