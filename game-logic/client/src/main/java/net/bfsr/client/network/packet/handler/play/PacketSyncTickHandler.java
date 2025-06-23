package net.bfsr.client.network.packet.handler.play;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.client.Client;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.engine.network.packet.PacketHandler;
import net.bfsr.engine.network.packet.server.PacketSyncTick;

import java.net.InetSocketAddress;

public class PacketSyncTickHandler extends PacketHandler<PacketSyncTick, NetworkSystem> {
    private final Client client = Client.get();

    @Override
    public void handle(PacketSyncTick packet, NetworkSystem networkSystem, ChannelHandlerContext ctx, InetSocketAddress remoteAddress) {
        client.addServerTickData(packet.getTick(), packet.getTime());
    }
}
