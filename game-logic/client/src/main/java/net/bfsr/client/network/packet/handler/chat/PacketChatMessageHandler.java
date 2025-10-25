package net.bfsr.client.network.packet.handler.chat;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.client.Client;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.engine.network.packet.PacketHandler;
import net.bfsr.network.packet.common.PacketChatMessage;

import java.net.InetSocketAddress;

public class PacketChatMessageHandler extends PacketHandler<PacketChatMessage, NetworkSystem> {
    @Override
    public void handle(PacketChatMessage packet, NetworkSystem networkSystem, ChannelHandlerContext ctx,
                       InetSocketAddress remoteAddress) {
        Client.get().getHud().addChatMessage(packet.getMessage());
    }
}