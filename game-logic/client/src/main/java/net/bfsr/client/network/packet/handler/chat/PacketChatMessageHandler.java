package net.bfsr.client.network.packet.handler.chat;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.client.Core;
import net.bfsr.client.event.chat.ChatMessageEvent;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.engine.event.EventBus;
import net.bfsr.network.packet.PacketHandler;
import net.bfsr.network.packet.common.PacketChatMessage;

import java.net.InetSocketAddress;

public class PacketChatMessageHandler extends PacketHandler<PacketChatMessage, NetworkSystem> {
    private final EventBus eventBus = Core.get().getEventBus();
    private final ChatMessageEvent chatMessageEvent = new ChatMessageEvent();

    public PacketChatMessageHandler() {
        eventBus.optimizeEvent(chatMessageEvent);
    }

    @Override
    public void handle(PacketChatMessage packet, NetworkSystem networkSystem, ChannelHandlerContext ctx,
                       InetSocketAddress remoteAddress) {
        eventBus.publishOptimized(chatMessageEvent.setMessage(packet.getMessage()));
    }
}