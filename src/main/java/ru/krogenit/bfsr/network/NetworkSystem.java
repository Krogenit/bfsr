package ru.krogenit.bfsr.network;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.local.LocalAddress;
import io.netty.channel.local.LocalServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector2f;
import ru.krogenit.bfsr.entity.ship.PlayerServer;
import ru.krogenit.bfsr.network.packet.server.PacketDisconnectPlay;
import ru.krogenit.bfsr.network.server.NetworkManagerServer;
import ru.krogenit.bfsr.server.MainServer;

import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class NetworkSystem {
	private static final Logger logger = LogManager.getLogger();
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
			this.endpoints.add((new ServerBootstrap()).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<Channel>() {

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

	public SocketAddress addLocalEndpoint() {
		ChannelFuture channelfuture;

		synchronized (this.endpoints) {
			channelfuture = (new ServerBootstrap()).channel(LocalServerChannel.class).childHandler(new ChannelInitializer<Channel>() {

				protected void initChannel(Channel channel) {
					NetworkManager networkmanager = new NetworkManagerServer(mainServer, mainServer.getWorld(), null);
					NetworkSystem.this.networkManagers.add(networkmanager);
					channel.pipeline().addLast("packet_handler", networkmanager);
				}
			}).group(eventLoops).localAddress(LocalAddress.ANY).bind().syncUninterruptibly();
			this.endpoints.add(channelfuture);
		}

		return channelfuture.channel().localAddress();
	}

	public void terminateEndpoints() {
		this.isAlive = false;

		for (ChannelFuture channelfuture : this.endpoints) {
			channelfuture.channel().close().syncUninterruptibly();
		}
		
		eventLoops.shutdownGracefully();
	}
	
	public void sendPacketToAllExcept(Packet packet, PlayerServer player) {
		List<PlayerServer> players = mainServer.getWorld().getPlayers();
		for(PlayerServer player1 : players) {
			if(player1 != player) player1.getNetworkManager().scheduleOutboundPacket(packet);
		}
	}
	
	public void sendPacketToAllNearby(Packet packet, Vector2f pos, float dist) {
		this.sendPacketToAllNearby(packet, pos.x, pos.y, dist);
	}
	
	public void sendPacketToAllNearby(Packet packet, float x, float y, float dist) {
		List<PlayerServer> players = mainServer.getWorld().getPlayers();
		for(PlayerServer player : players) {
			if(player.getPosition().distance(x, y) <= dist) {
				player.getNetworkManager().scheduleOutboundPacket(packet);
			}
		}
	}
	
	public void sendPacketToAllNearbyExcept(Packet packet, Vector2f pos, float dist, PlayerServer player1) {
		this.sendPacketToAllNearbyExcept(packet, pos.x, pos.y, dist, player1);
	}
	
	public void sendPacketToAllNearbyExcept(Packet packet, float x, float y, float dist, PlayerServer player1) {
		List<PlayerServer> players = mainServer.getWorld().getPlayers();
		for(PlayerServer player : players) {
			if(player1 != player && player.getPosition().distance(x, y) <= dist) {
				player.getNetworkManager().scheduleOutboundPacket(packet);
			}
		}
	}
	
	public void sendPacketTo(Packet packet, PlayerServer player) {
		player.getNetworkManager().scheduleOutboundPacket(packet);
	}
	
	public void sendPacketToAll(Packet packet) {
		List<PlayerServer> players = mainServer.getWorld().getPlayers();
		for(PlayerServer player : players) {
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

						logger.warn("Failed to handle packet for " + networkmanager.getSocketAddress(), exception);
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