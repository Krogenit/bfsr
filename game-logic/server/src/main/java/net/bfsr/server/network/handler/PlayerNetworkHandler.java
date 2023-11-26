package net.bfsr.server.network.handler;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultAddressedEnvelope;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.SocketChannel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.ShipOutfitter;
import net.bfsr.network.ConnectionState;
import net.bfsr.network.GuiType;
import net.bfsr.network.NetworkHandler;
import net.bfsr.network.packet.Packet;
import net.bfsr.network.packet.PacketRegistry;
import net.bfsr.network.packet.common.PacketKeepAlive;
import net.bfsr.network.packet.server.gui.PacketOpenGui;
import net.bfsr.network.packet.server.login.PacketDisconnectLogin;
import net.bfsr.network.packet.server.login.PacketJoinGame;
import net.bfsr.network.packet.server.login.PacketLoginTCPSuccess;
import net.bfsr.network.packet.server.login.PacketLoginUDPSuccess;
import net.bfsr.server.ServerGameLogic;
import net.bfsr.server.network.pipeline.MessageHandlerUDP;
import net.bfsr.server.player.Player;
import net.bfsr.server.player.PlayerManager;
import net.bfsr.world.World;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@RequiredArgsConstructor
@Getter
@Log4j2
public class PlayerNetworkHandler extends NetworkHandler {
    private static final int LOGIN_TIMEOUT_IN_MILLS = 5000;
    private static final int KEEP_ALIVE_PERIOD_IN_MILLS = 2000;

    private final SocketChannel socketChannel;
    private DatagramChannel datagramChannel;
    private InetSocketAddress remoteAddress;
    @Setter
    private ConnectionState connectionState = ConnectionState.HANDSHAKE;
    private ConnectionState connectionStateBeforeDisconnect;
    private final Queue<Packet> inboundPacketQueue = new ConcurrentLinkedQueue<>();

    @Setter
    private long loginStartTime;
    @Setter
    private long handshakeClientTime;
    private long lastKeepAlivePacketTime;
    private String terminationReason;

    private final boolean singlePlayer;
    private final ServerGameLogic server = ServerGameLogic.getInstance();
    private final World world = server.getWorld();
    private final PlayerManager playerManager = server.getPlayerManager();
    private Player player;
    private final PacketRegistry<PlayerNetworkHandler> packetRegistry = ServerGameLogic.getNetwork().getPacketRegistry();

    public void update() {
        if (connectionState != ConnectionState.NOT_CONNECTED) {
            processReceivedPackets();

            if (connectionState == ConnectionState.PLAY) {
                long now = System.currentTimeMillis();
                if (now - lastKeepAlivePacketTime > KEEP_ALIVE_PERIOD_IN_MILLS) {
                    sendUDPPacket(new PacketKeepAlive());
                    lastKeepAlivePacketTime = now;
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
                if (connectionState != ConnectionState.NOT_CONNECTED) {
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
                if (connectionState != ConnectionState.NOT_CONNECTED) {
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

        log.debug("Player logging in");
        try {
            if (singlePlayer) {
                player = playerManager.getPlayerService().authUser(username, "test");
                log.debug("Player created");
            } else {
                player = playerManager.getPlayerService().authUser(username, "password");
            }
        } catch (Exception e) {
            log.error("Couldn't auth user {}", username, e);
            disconnect("Player service not available, try again later");
            return;
        }

        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(username.getBytes(StandardCharsets.UTF_8));
            byte[] digest = messageDigest.digest();
            log.debug("Player hash created");
            player.setDigest(digest);
            ServerGameLogic.getInstance().getNetworkSystem().addHandler(username, this);
            sendTCPPacket(new PacketLoginTCPSuccess(digest));
        } catch (NoSuchAlgorithmException e) {
            log.error("Couldn't create player hash", e);
            disconnect("Player's hash creation error");
        }
    }

    public void loginUDP(String login, byte[] digest, ChannelHandlerContext ctx, InetSocketAddress remoteAddress) {
        if (player == null) {
            log.error("Player object not found for login {} {}", login, remoteAddress);
            sendTCPPacket(new PacketDisconnectLogin("Player object not found"));
            closeChannel("Player object not found");
            return;
        }

        if (!Arrays.equals(digest, player.getDigest())) {
            log.error("Player {} sent wrong digest", login);
            sendTCPPacket(new PacketDisconnectLogin("Wrong digest"));
            closeChannel("Wrong digest");
            return;
        }

        datagramChannel = (DatagramChannel) ctx.channel();
        this.remoteAddress = remoteAddress;
        ((MessageHandlerUDP) datagramChannel.pipeline().get("handler")).setPlayerNetworkHandler(this);
        ctx.writeAndFlush(new DefaultAddressedEnvelope<Packet, SocketAddress>(new PacketLoginUDPSuccess(), remoteAddress))
                .addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    public void joinGame() {
        connectionState = ConnectionState.PLAY;
        player.setNetworkHandler(this);
        playerManager.addPlayer(player);
        sendTCPPacket(new PacketJoinGame(world.getSeed()));
        if (player.getFaction() != null) {
            List<Ship> ships = player.getShips();
            if (ships.isEmpty()) {
                server.getPlayerManager().respawnPlayer(player, 0, 0);
            } else {
                initShips(player);
                spawnShips(player);
                player.setShip(player.getShip(0));
            }
        } else {
            sendTCPPacket(new PacketOpenGui(GuiType.SELECT_FACTION));
        }
    }

    private void initShips(Player player) {
        List<Ship> ships = player.getShips();
        for (int i = 0; i < ships.size(); i++) {
            Ship ship = ships.get(i);
            ship.init(world, world.getNextId());
            ship.setName(player.getUsername());
            ship.setOwner(player.getUsername());
            ship.setFaction(player.getFaction());
            ShipOutfitter.get().outfit(ship);
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
        if (connectionStateBeforeDisconnect == ConnectionState.PLAY) {
            log.info("{} lost connection: {}", player, terminationReason);
            server.onPlayerDisconnected(player);
        } else {
            log.info("{} lost connection: {}", socketChannel.remoteAddress(), terminationReason);
        }
    }

    public void closeChannel(String reason) {
        socketChannel.close();
        terminationReason = reason;
        connectionStateBeforeDisconnect = connectionState;
        connectionState = ConnectionState.NOT_CONNECTED;
    }

    public boolean isClosed() {
        return !socketChannel.isOpen();
    }
}