package net.bfsr.server.network.packet.handler.chat;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.network.packet.PacketHandler;
import net.bfsr.network.packet.common.PacketChatMessage;
import net.bfsr.server.ServerGameLogic;
import net.bfsr.server.network.NetworkSystem;
import net.bfsr.server.network.handler.PlayerNetworkHandler;

import java.net.InetSocketAddress;

public class PacketChatMessageHandler extends PacketHandler<PacketChatMessage, PlayerNetworkHandler> {
    private final NetworkSystem networkSystem = ServerGameLogic.get().getNetworkSystem();

    @Override
    public void handle(PacketChatMessage packet, PlayerNetworkHandler playerNetworkHandler, ChannelHandlerContext ctx,
                       InetSocketAddress remoteAddress) {
        networkSystem.sendTCPPacketToAll(new PacketChatMessage(packet.getMessage()));
    }
}