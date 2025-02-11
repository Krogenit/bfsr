package net.bfsr.client.network.packet.handler.play.player;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.client.Client;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.network.packet.PacketHandler;
import net.bfsr.network.packet.server.player.PacketSetPlayerShip;

import java.net.InetSocketAddress;

public class PacketSetPlayerShipHandler extends PacketHandler<PacketSetPlayerShip, NetworkSystem> {
    @Override
    public void handle(PacketSetPlayerShip packet, NetworkSystem networkSystem, ChannelHandlerContext ctx,
                       InetSocketAddress remoteAddress) {
        Client.get().getPlayerInputController().setControlledShipId(packet.getId());
    }
}