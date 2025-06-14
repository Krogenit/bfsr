package net.bfsr.server.network.handler;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.DefaultAddressedEnvelope;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.SocketChannel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import net.bfsr.engine.event.EventBus;
import net.bfsr.engine.network.ConnectionState;
import net.bfsr.engine.network.NetworkHandler;
import net.bfsr.engine.network.packet.Packet;
import net.bfsr.engine.network.packet.PacketRegistry;
import net.bfsr.engine.network.packet.common.PacketPing;
import net.bfsr.engine.network.packet.server.login.PacketDisconnectLogin;
import net.bfsr.engine.network.packet.server.login.PacketJoinGame;
import net.bfsr.engine.network.packet.server.login.PacketLoginSuccess;
import net.bfsr.engine.util.Side;
import net.bfsr.engine.world.World;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.ShipOutfitter;
import net.bfsr.network.GuiType;
import net.bfsr.network.packet.server.gui.PacketOpenGui;
import net.bfsr.server.ai.AiFactory;
import net.bfsr.server.entity.EntityTrackingManager;
import net.bfsr.server.event.PlayerDisconnectEvent;
import net.bfsr.server.event.PlayerJoinGameEvent;
import net.bfsr.server.player.Player;
import net.bfsr.server.player.PlayerManager;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@RequiredArgsConstructor
@Getter
@Log4j2
public class PlayerNetworkHandler extends NetworkHandler {
    private static final int LOGIN_TIMEOUT_IN_MILLS = 5000;
    private static final int PING_PERIOD_IN_MILLS = 2000;

    private final int connectionId;
    private final SocketChannel socketChannel;
    private final DatagramChannel datagramChannel;
    private final boolean singlePlayer;
    private final World world;
    private final PlayerManager playerManager;
    private final EntityTrackingManager entityTrackingManager;
    private final AiFactory aiFactory;
    private final EventBus eventBus;
    private final PacketRegistry<PlayerNetworkHandler> packetRegistry;
    private final ShipOutfitter shipOutfitter;

    @Setter
    private InetSocketAddress remoteAddress;
    @Setter
    private ConnectionState connectionState = ConnectionState.CONNECTING;
    private ConnectionState connectionStateBeforeDisconnect;
    private final Queue<Packet> inboundPacketQueue = new ConcurrentLinkedQueue<>();

    @Setter
    private long loginStartTime;
    private long lastPingCheckTime;
    private String terminationReason;

    private Player player;

    public void update() {
        if (connectionState != ConnectionState.DISCONNECTED) {
            processReceivedPackets();

            if (connectionState == ConnectionState.CONNECTED) {
                long now = System.currentTimeMillis();
                if (now - lastPingCheckTime > PING_PERIOD_IN_MILLS) {
                    sendUDPPacket(new PacketPing(Side.SERVER));
                    lastPingCheckTime = now;
                }
            } else if (connectionState == ConnectionState.LOGIN) {
                long now = System.currentTimeMillis();
                if (now - loginStartTime >= LOGIN_TIMEOUT_IN_MILLS) {
                    disconnect("login timeout");
                }
            }
        }
    }

    private void processReceivedPackets() {
        for (int i = 0; !inboundPacketQueue.isEmpty() && i < 1000; ++i) {
            processPacket(inboundPacketQueue.poll());
        }
    }

    private void processPacket(Packet packet) {
        packetRegistry.getPacketHandler(packet).handle(packet, this);
    }

    @Override
    public void addPacketToQueue(Packet packet) {
        inboundPacketQueue.add(packet);
    }

    public void sendTCPPacket(Packet packet) {
        if (socketChannel.eventLoop().inEventLoop()) {
            socketChannel.writeAndFlush(packet).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
        } else {
            socketChannel.eventLoop().execute(() -> {
                if (connectionState != ConnectionState.DISCONNECTED) {
                    socketChannel.writeAndFlush(packet).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
                }
            });
        }
    }

    public void sendUDPPacket(Packet packet) {
        if (datagramChannel.eventLoop().inEventLoop()) {
            datagramChannel.writeAndFlush(new DefaultAddressedEnvelope<Packet, SocketAddress>(packet, remoteAddress))
                    .addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
        } else {
            datagramChannel.eventLoop().execute(() -> {
                if (connectionState != ConnectionState.DISCONNECTED) {
                    datagramChannel.writeAndFlush(new DefaultAddressedEnvelope<Packet, SocketAddress>(packet, remoteAddress))
                            .addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
                }
            });
        }
    }

    public void loginTCP(String username) {
        if (playerManager.hasPlayer(username)) {
            disconnect("Player with this name already in game");
            return;
        }

        try {
            if (singlePlayer) {
                player = playerManager.get(username);
            } else {
                player = playerManager.get(username);
            }
        } catch (Exception e) {
            log.error("Couldn't auth user {}", username, e);
            disconnect("Player service not available, try again later");
            return;
        }

        player.init(this, entityTrackingManager, playerManager, aiFactory);
        playerManager.addPlayer(player);

        sendTCPPacket(new PacketLoginSuccess());
    }

    public void joinGame() {
        connectionState = ConnectionState.CONNECTED;
        sendTCPPacket(new PacketJoinGame(world.getSeed()));
        if (player.getFaction() != null) {
            List<Ship> ships = player.getShips();
            if (ships.isEmpty()) {
                playerManager.respawnPlayer(world, player, 0, 0);
            } else {
                initShips(player);
                spawnShips(player);
                player.setShip(player.getShip(0));
            }
        } else {
            sendTCPPacket(new PacketOpenGui(GuiType.SELECT_FACTION));
        }

        world.getEventBus().publish(new PlayerJoinGameEvent(player));
    }

    private void initShips(Player player) {
        List<Ship> ships = player.getShips();
        for (int i = 0; i < ships.size(); i++) {
            Ship ship = ships.get(i);
            ship.getDamageMask().init();
            ship.init(world, world.getNextId());
            ship.setName(player.getUsername());
            ship.setOwner(player.getUsername());
            ship.setFaction(player.getFaction());
            shipOutfitter.outfit(ship);
        }
    }

    private void spawnShips(Player player) {
        List<Ship> ships = player.getShips();
        for (int i = 0; i < ships.size(); i++) {
            Ship ship = ships.get(i);
            world.add(ship, false);
        }
    }

    private void disconnect(String reason) {
        try {
            log.info("Disconnecting {}: {}", socketChannel.remoteAddress(), reason);
            socketChannel.eventLoop().execute(() -> socketChannel.writeAndFlush(new PacketDisconnectLogin(reason))
                    .addListener(future -> closeChannel(reason)));
        } catch (Exception e) {
            log.error("Error whilst disconnecting player", e);
        }
    }

    public void onDisconnected() {
        if (connectionStateBeforeDisconnect == ConnectionState.CONNECTED) {
            log.info("{} lost connection: {}", player.getUsername(), terminationReason);
        } else {
            log.info("{} lost connection: {}", socketChannel.remoteAddress(), terminationReason);
        }

        if (player != null) {
            playerManager.removePlayer(player);
            playerManager.save(player);
            List<Ship> ships = player.getShips();
            for (int i = 0, shipsSize = ships.size(); i < shipsSize; i++) {
                Ship ship = ships.get(i);
                if (ship.getWorld() != null) {
                    ship.setDead();
                }
            }

            eventBus.publish(new PlayerDisconnectEvent(player));
            log.info("Player with username {} removed", player.getUsername());
        } else {
            log.info("Skip removing player {}", socketChannel.remoteAddress());
        }
    }

    public void closeChannel(String reason) {
        socketChannel.close();
        terminationReason = reason;
        connectionStateBeforeDisconnect = connectionState;
        connectionState = ConnectionState.DISCONNECTED;
    }

    public boolean isClosed() {
        return !socketChannel.isOpen();
    }
}