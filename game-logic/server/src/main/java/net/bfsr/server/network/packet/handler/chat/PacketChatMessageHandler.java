package net.bfsr.server.network.packet.handler.chat;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.network.packet.PacketHandler;
import net.bfsr.network.packet.common.PacketChatMessage;
import net.bfsr.server.ServerGameLogic;
import net.bfsr.server.network.handler.PlayerNetworkHandler;

import java.net.InetSocketAddress;

public class PacketChatMessageHandler extends PacketHandler<PacketChatMessage, PlayerNetworkHandler> {
    @Override
    public void handle(PacketChatMessage packet, PlayerNetworkHandler playerNetworkHandler, ChannelHandlerContext ctx, InetSocketAddress remoteAddress) {
        ServerGameLogic.getInstance().getNetworkSystem().sendTCPPacketToAll(new PacketChatMessage(packet.getMessage()));
    }
}