package net.bfsr.server.network;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.extern.log4j.Log4j2;
import net.bfsr.network.Packet;
import net.bfsr.network.PacketDecoder2;
import net.bfsr.network.PacketEncoder2;
import net.bfsr.network.PingResponseHandler;
import net.bfsr.server.MainServer;
import net.bfsr.server.network.packet.server.PacketDisconnectPlay;
import net.bfsr.server.player.PlayerServer;
import org.joml.Vector2f;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@Log4j2
public class NetworkSystem {
    private static final NioEventLoopGroup eventLoops = new NioEventLoopGroup(0, (new ThreadFactoryBuilder()).setNameFormat("Netty IO #%d").setDaemon(true).build());
    public static final int READ_TIMEOUT = 30;

    private final MainServer mainServer;

    public volatile boolean isAlive;

    private final List<ChannelFuture> endpoints = Collections.synchronizedList(new ArrayList<>());
    private final List<NetworkManager> networkManagers = Collections.synchronizedList(new ArrayList<>());

    public NetworkSystem(MainServer mainServer) {
        this.mainServer = mainServer;
        this.isAlive = true;
    }

    public void addLanEndpoint(InetAddress address, int port) {
        synchronized (this.endpoints) {
            this.endpoints.add((new ServerBootstrap()).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<>() {

                protected void initChannel(Channel channel) {
                    try {
                        channel.config().setOption(ChannelOption.IP_TOS, 24);
                    } catch (ChannelException ignored) {
                    }

                    try {
                        channel.config().setOption(ChannelOption.TCP_NODELAY, Boolean.FALSE);
                    } catch (ChannelException ignored) {
                    }

                    channel.pipeline()
                            .addLast("timeout", new ReadTimeoutHandler(READ_TIMEOUT))
                            .addLast("legacy_query", new PingResponseHandler())
                            .addLast("splitter", new PacketDecoder2())
                            .addLast("decoder", new PacketDecoder(NetworkManager.statistics))
                            .addLast("prepender", new PacketEncoder2())
                            .addLast("encoder", new PacketEncoder(NetworkManager.statistics));
                    NetworkManagerServer managerServer = new NetworkManagerServer(mainServer, mainServer.getWorld(), null);
                    NetworkSystem.this.networkManagers.add(managerServer);
                    channel.pipeline().addLast("packet_handler", managerServer);
                }
            }).group(eventLoops).localAddress(address, port).bind().syncUninterruptibly());
        }
    }

    public void terminateEndpoints() {
        this.isAlive = false;

        List<ChannelFuture> channelFutures = this.endpoints;
        for (int i = 0, channelFuturesSize = channelFutures.size(); i < channelFuturesSize; i++) {
            ChannelFuture channelfuture = channelFutures.get(i);
            channelfuture.channel().close().syncUninterruptibly();
        }

        eventLoops.shutdownGracefully();
    }

    public void sendPacketToAllExcept(Packet packet, PlayerServer player) {
        List<PlayerServer> players = mainServer.getWorld().getPlayers();
        for (int i = 0, playersSize = players.size(); i < playersSize; i++) {
            PlayerServer player1 = players.get(i);
            if (player1 != player) player1.getNetworkManager().scheduleOutboundPacket(packet);
        }
    }

    public void sendPacketToAllNearby(Packet packet, Vector2f pos, float dist) {
        this.sendPacketToAllNearby(packet, pos.x, pos.y, dist);
    }

    public void sendPacketToAllNearby(Packet packet, float x, float y, float dist) {
        List<PlayerServer> players = mainServer.getWorld().getPlayers();
        for (int i = 0, playersSize = players.size(); i < playersSize; i++) {
            PlayerServer player = players.get(i);
            if (player.getPosition().distance(x, y) <= dist) {
                player.getNetworkManager().scheduleOutboundPacket(packet);
            }
        }
    }

    public void sendPacketToAllNearbyExcept(Packet packet, Vector2f pos, float dist, PlayerServer player1) {
        this.sendPacketToAllNearbyExcept(packet, pos.x, pos.y, dist, player1);
    }

    public void sendPacketToAllNearbyExcept(Packet packet, float x, float y, float dist, PlayerServer player1) {
        List<PlayerServer> players = mainServer.getWorld().getPlayers();
        for (int i = 0, playersSize = players.size(); i < playersSize; i++) {
            PlayerServer player = players.get(i);
            if (player1 != player && player.getPosition().distance(x, y) <= dist) {
                player.getNetworkManager().scheduleOutboundPacket(packet);
            }
        }
    }

    public void sendPacketTo(Packet packet, PlayerServer player) {
        player.getNetworkManager().scheduleOutboundPacket(packet);
    }

    public void sendPacketToAll(Packet packet) {
        List<PlayerServer> players = mainServer.getWorld().getPlayers();
        for (int i = 0, playersSize = players.size(); i < playersSize; i++) {
            PlayerServer player = players.get(i);
            player.getNetworkManager().scheduleOutboundPacket(packet);
        }
    }

    public void networkTick() {
        synchronized (this.networkManagers) {
            Iterator<NetworkManager> iterator = this.networkManagers.iterator();

            while (iterator.hasNext()) {
                final NetworkManager networkmanager = iterator.next();

                if (!networkmanager.isChannelOpen()) {
                    iterator.remove();

                    if (networkmanager.getExitMessage() != null) {
                        networkmanager.onDisconnect(networkmanager.getExitMessage());
                    } else {
                        networkmanager.onDisconnect("Disconnected");
                    }
                } else {
                    try {
                        networkmanager.processReceivedPackets();
                    } catch (Exception exception) {
                        if (networkmanager.isLocalChannel()) {
                            exception.printStackTrace();
                        }

                        log.warn("Failed to handle packet for " + networkmanager.getSocketAddress(), exception);
                        final String errorString = "Internal server error";
                        networkmanager.scheduleOutboundPacket(new PacketDisconnectPlay(errorString), future -> networkmanager.closeChannel(errorString));
                        networkmanager.disableAutoRead();
                    }
                }
            }
        }
    }

    public MainServer getServer() {
        return this.mainServer;
    }
}