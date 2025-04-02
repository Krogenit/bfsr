package net.bfsr.server.network.packet.handler.play.player.input;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.engine.network.packet.PacketHandler;
import net.bfsr.engine.world.entity.GameObject;
import net.bfsr.entity.ship.Ship;
import net.bfsr.network.packet.client.PacketShipControl;
import net.bfsr.server.network.handler.PlayerNetworkHandler;
import net.bfsr.server.player.Player;

import java.net.InetSocketAddress;

public class PacketShipControlHandler extends PacketHandler<PacketShipControl, PlayerNetworkHandler> {
    @Override
    public void handle(PacketShipControl packet, PlayerNetworkHandler playerNetworkHandler, ChannelHandlerContext ctx,
                       InetSocketAddress remoteAddress) {
        GameObject obj = playerNetworkHandler.getWorld().getEntityById(packet.getId());
        if (obj instanceof Ship ship) {
            Player player = playerNetworkHandler.getPlayer();
            if (packet.isControl()) {
                player.getPlayerInputController().setShip(ship);
            } else {
                player.getPlayerInputController().setShip(null);
            }
        }
    }
}