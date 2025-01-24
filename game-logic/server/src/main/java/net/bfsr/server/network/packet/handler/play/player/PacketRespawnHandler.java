package net.bfsr.server.network.packet.handler.play.player;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.network.packet.PacketHandler;
import net.bfsr.network.packet.client.PacketRespawn;
import net.bfsr.server.network.handler.PlayerNetworkHandler;

import java.net.InetSocketAddress;

public class PacketRespawnHandler extends PacketHandler<PacketRespawn, PlayerNetworkHandler> {
    @Override
    public void handle(PacketRespawn packet, PlayerNetworkHandler playerNetworkHandler, ChannelHandlerContext ctx,
                       InetSocketAddress remoteAddress) {
        playerNetworkHandler.getPlayerManager().respawnPlayer(playerNetworkHandler.getWorld(), playerNetworkHandler.getPlayer(),
                packet.getCamPosX(), packet.getCamPosY());
    }
}