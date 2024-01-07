package net.bfsr.client.network.packet.handler;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.client.Core;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.engine.util.Side;
import net.bfsr.network.packet.PacketHandler;
import net.bfsr.network.packet.common.PacketPing;

import java.net.InetSocketAddress;

public class PacketPingHandler extends PacketHandler<PacketPing, NetworkSystem> {
    @Override
    public void handle(PacketPing packet, NetworkSystem netHandler, ChannelHandlerContext ctx, InetSocketAddress remoteAddress) {
        if (packet.getSide() == Side.CLIENT) {
            long clientToServerPing = packet.getOneWayTime() / 1000;
            double rtt = System.nanoTime() - packet.getOriginalSentTime();
            Core core = Core.get();
            core.getGuiManager().getHud().setPing(clientToServerPing / 1000.0f);

            double clientToServerDiffTime = packet.getResponseSentTime() - packet.getOriginalSentTime() - rtt / 2;
            core.setClientToServerDiffTime(clientToServerDiffTime);
        } else {
            netHandler.sendPacketUDP(new PacketPing(0, packet.getOriginalSentTime(), System.nanoTime(), packet.getSide()));
        }
    }
}