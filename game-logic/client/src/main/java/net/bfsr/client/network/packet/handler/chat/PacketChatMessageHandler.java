package net.bfsr.client.network.packet.handler.chat;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.client.Core;
import net.bfsr.client.gui.GuiManager;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.network.packet.PacketHandler;
import net.bfsr.network.packet.common.PacketChatMessage;

import java.net.InetSocketAddress;

public class PacketChatMessageHandler extends PacketHandler<PacketChatMessage, NetworkSystem> {
    private final GuiManager guiManager = Core.get().getGuiManager();

    @Override
    public void handle(PacketChatMessage packet, NetworkSystem networkSystem, ChannelHandlerContext ctx,
                       InetSocketAddress remoteAddress) {
        guiManager.getHud().addChatMessage(packet.getMessage());
    }
}