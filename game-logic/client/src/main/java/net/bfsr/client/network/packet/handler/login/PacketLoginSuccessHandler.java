package net.bfsr.client.network.packet.handler.login;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.log4j.Log4j2;
import net.bfsr.GameplayMode;
import net.bfsr.client.Client;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.engine.network.ConnectionState;
import net.bfsr.engine.network.packet.PacketHandler;
import net.bfsr.engine.network.packet.server.login.PacketJoinGame;
import net.bfsr.engine.network.packet.server.login.PacketLoginSuccess;

import java.net.InetSocketAddress;

@Log4j2
public class PacketLoginSuccessHandler extends PacketHandler<PacketLoginSuccess, NetworkSystem> {
    private final Client client = Client.get();

    @Override
    public void handle(PacketLoginSuccess packet, NetworkSystem netHandler, ChannelHandlerContext ctx,
                       InetSocketAddress remoteAddress) {
        client.setFrame(packet.getFrame());
        client.setTime(packet.getServerTime());
        client.setGameplayMode(GameplayMode.values()[packet.getGameplayMode()]);
        client.createWorld(packet.getSeed());
        client.getNetworkSystem().setConnectionState(ConnectionState.CONNECTED);
        client.sendTCPPacket(new PacketJoinGame());
    }
}