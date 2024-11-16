package net.bfsr.client.network.packet.handler;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.client.Client;
import net.bfsr.client.event.PingEvent;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.engine.util.Side;
import net.bfsr.network.packet.PacketHandler;
import net.bfsr.network.packet.common.PacketPing;

import java.net.InetSocketAddress;

public class PacketPingHandler extends PacketHandler<PacketPing, NetworkSystem> {
    @Override
    public void handle(PacketPing packet, NetworkSystem netHandler, ChannelHandlerContext ctx, InetSocketAddress remoteAddress) {
        if (packet.getSide() == Side.CLIENT) {
            long rtt = System.nanoTime() - packet.getOriginalSentTime();
            Client client = Client.get();
            client.getEventBus().publish(new PingEvent(rtt / 2000000.0f));
            client.setClientToServerDiffTime(rtt / 2.0);
        } else {
            netHandler.sendPacketUDP(new PacketPing(packet.getSide()));
        }
    }
}