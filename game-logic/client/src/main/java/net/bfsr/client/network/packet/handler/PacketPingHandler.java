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
        long nanoTime = System.nanoTime();
        if (packet.getSide() == Side.CLIENT) {
            long roundTripTimeNanos = packet.getRoundTripTime();
            Client client = Client.get();
            client.getEventBus().publish(new PingEvent(roundTripTimeNanos / 2_000_000.0f));
            client.setClientToServerTimeDiff(nanoTime - packet.getOtherSideHandleTime());
        } else {
            netHandler.sendPacketUDP(new PacketPing(packet.getOriginalSentTime(), nanoTime, packet.getSide()));
        }
    }
}