package net.bfsr.client.network.packet.handler.login;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.client.Client;
import net.bfsr.client.event.PlayerJoinGameEvent;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.engine.network.ConnectionState;
import net.bfsr.engine.network.packet.PacketHandler;
import net.bfsr.engine.network.packet.server.login.PacketJoinGame;

import java.net.InetSocketAddress;

public class PacketJoinGameHandler extends PacketHandler<PacketJoinGame, NetworkSystem> {
    private final Client client = Client.get();

    @Override
    public void handle(PacketJoinGame packet, NetworkSystem networkSystem, ChannelHandlerContext ctx,
                       InetSocketAddress remoteAddress) {
        client.createWorld(packet.getSeed());
        client.closeGui();
        client.getNetworkSystem().setConnectionState(ConnectionState.CONNECTED);
        client.getEventBus().publish(new PlayerJoinGameEvent());
    }
}