package net.bfsr.client.network.packet.handler.login;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.engine.network.packet.PacketHandler;
import net.bfsr.engine.network.packet.common.PacketRegisterTCP;
import net.bfsr.engine.network.packet.common.PacketRegisterUDP;

import java.net.InetSocketAddress;

public class PacketRegisterTCPHandler extends PacketHandler<PacketRegisterTCP, NetworkSystem> {
    @Override
    public void handle(PacketRegisterTCP packet, NetworkSystem networkSystem, ChannelHandlerContext ctx,
                       InetSocketAddress remoteAddress) {
        networkSystem.setConnectionId(packet.getConnectionId());
        networkSystem.sendPacketUDP(new PacketRegisterUDP(packet.getConnectionId()));
    }
}