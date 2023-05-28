package net.bfsr.client.network.packet.handler.login;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.client.Core;
import net.bfsr.client.event.PlayerJoinGameEvent;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.network.ConnectionState;
import net.bfsr.network.packet.PacketHandler;
import net.bfsr.network.packet.server.login.PacketJoinGame;

import java.net.InetSocketAddress;

public class PacketJoinGameHandler extends PacketHandler<PacketJoinGame, NetworkSystem> {
    private final Core core = Core.get();

    @Override
    public void handle(PacketJoinGame packet, NetworkSystem networkSystem, ChannelHandlerContext ctx,
                       InetSocketAddress remoteAddress) {
        core.createWorld(packet.getSeed());
        core.closeGui();
        core.getNetworkSystem().setConnectionState(ConnectionState.PLAY);
        core.publish(new PlayerJoinGameEvent());
    }
}