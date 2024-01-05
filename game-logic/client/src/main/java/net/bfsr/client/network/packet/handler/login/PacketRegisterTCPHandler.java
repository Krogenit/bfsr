package net.bfsr.client.network.packet.handler.login;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.network.packet.PacketHandler;
import net.bfsr.network.packet.common.PacketRegisterTCP;
import net.bfsr.network.packet.common.PacketRegisterUDP;

import java.net.InetSocketAddress;

public class PacketRegisterTCPHandler extends PacketHandler<PacketRegisterTCP, NetworkSystem> {
    @Override
    public void handle(PacketRegisterTCP packet, NetworkSystem networkSystem, ChannelHandlerContext ctx,
                       InetSocketAddress remoteAddress) {
        networkSystem.sendPacketUDP(new PacketRegisterUDP(packet.getConnectionId()));
    }
}