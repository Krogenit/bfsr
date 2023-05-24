package net.bfsr.client.network.packet.handler;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.client.Core;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.network.packet.PacketHandler;
import net.bfsr.network.packet.common.PacketPing;

import java.net.InetSocketAddress;

public class PacketPingHandler extends PacketHandler<PacketPing, NetworkSystem> {
    @Override
    public void handle(PacketPing packet, NetworkSystem netHandler, ChannelHandlerContext ctx, InetSocketAddress remoteAddress) {
        Core.get().getGuiManager().getGuiInGame().setPing(packet.getTime() / 1_000_000.0f);
    }
}