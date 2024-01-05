package net.bfsr.client.network.packet.handler.login;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.network.packet.PacketHandler;
import net.bfsr.network.packet.common.PacketRegisterUDP;

import java.net.InetSocketAddress;

public class PacketRegisterUDPHandler extends PacketHandler<PacketRegisterUDP, NetworkSystem> {
    @Override
    public void handle(PacketRegisterUDP packet, NetworkSystem networkSystem, ChannelHandlerContext ctx,
                       InetSocketAddress remoteAddress) {
        networkSystem.onChannelsRegistered();
    }
}