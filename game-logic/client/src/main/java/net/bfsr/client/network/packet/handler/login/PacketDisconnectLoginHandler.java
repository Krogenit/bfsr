package net.bfsr.client.network.packet.handler.login;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.client.Client;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.network.packet.PacketHandler;
import net.bfsr.network.packet.server.login.PacketDisconnectLogin;

import java.net.InetSocketAddress;

public class PacketDisconnectLoginHandler extends PacketHandler<PacketDisconnectLogin, NetworkSystem> {
    private final NetworkSystem networkSystem = Client.get().getNetworkSystem();

    @Override
    public void handle(PacketDisconnectLogin packet, NetworkSystem networkSystem, ChannelHandlerContext ctx,
                       InetSocketAddress remoteAddress) {
        this.networkSystem.closeChannels();
        this.networkSystem.onDisconnect(packet.getMessage());
    }
}