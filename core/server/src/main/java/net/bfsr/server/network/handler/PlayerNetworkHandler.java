package net.bfsr.server.network.handler;

import com.google.common.collect.Queues;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultAddressedEnvelope;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.SocketChannel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import net.bfsr.network.GuiType;
import net.bfsr.network.PacketOut;
import net.bfsr.server.MainServer;
import net.bfsr.server.entity.ship.Ship;
import net.bfsr.server.network.ConnectionState;
import net.bfsr.server.network.packet.PacketIn;
import net.bfsr.server.network.packet.common.PacketKeepAlive;
import net.bfsr.server.network.packet.server.entity.PacketRemoveObject;
import net.bfsr.server.network.packet.server.gui.PacketOpenGui;
import net.bfsr.server.network.packet.server.login.PacketDisconnectLogin;
import net.bfsr.server.network.packet.server.login.PacketJoinGame;
import net.bfsr.server.network.packet.server.login.PacketLoginTCPSuccess;
import net.bfsr.server.network.packet.server.login.PacketLoginUDPSuccess;
import net.bfsr.server.network.pipeline.MessageHandlerUDP;
import net.bfsr.server.player.PlayerServer;
import net.bfsr.server.world.WorldServer;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;

@RequiredArgsConstructor
@Getter
@Log4j2
public class PlayerNetworkHandler {
    private static final int LOGIN_TIMEOUT_IN_MILLS = 5000;
    private static final int KEEP_ALIVE_PERIOD_IN_MILLS = 2000;

    private final SocketChannel socketChannel;
    private DatagramChannel datagramChannel;
    private InetSocketAddress remoteAddress;

    private final boolean singlePlayer;
    private final MainServer server = MainServer.getInstance();
    private final WorldServer world = server.getWorld();
    @Setter
    private PlayerServer player;
    @Setter
    private ConnectionState connectionState = ConnectionState.HANDSHAKE;
    private ConnectionState connectionStateBeforeDisconnect;

    private final Queue<PacketIn> inboundPacketQueue = Queues.newConcurrentLinkedQueue();

    @Setter
    private long loginStartTime;
    @Setter
    private long handshakeClientTime;
    private long lastKeepAlivePacketTime;

    private String terminationReason;

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
        for (int i = 1000; !inboundPacketQueue.isEmpty() && i >= 0; --i) {
            PacketIn packet = inboundPacketQueue.poll();
            processPacket(packet);
        }
    }

    private void processPacket(PacketIn packet) {
        packet.processOnServerSide(this);
    }

    public void addPacketToQueue(PacketIn packet) {
        inboundPacketQueue.add(packet);
    }

    public void sendTCPPacket(PacketOut packet) {
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

    public void sendUDPPacket(PacketOut packet) {
        if (datagramChannel.eventLoop().inEventLoop()) {
            datagramChannel.writeAndFlush(new DefaultAddressedEnvelope<PacketOut, SocketAddress>(packet, remoteAddress)).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
        } else {
            datagramChannel.eventLoop().execute(() -> {
                if (connectionState != ConnectionState.NOT_CONNECTED) {
                    datagramChannel.writeAndFlush(new DefaultAddressedEnvelope<PacketOut, SocketAddress>(packet, remoteAddress))
                            .addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
                }
            });
        }
    }

    public void loginTCP(String username) {
        if (singlePlayer) {
            log.debug("Player logging in");
            server.getDataBase().authorizeUser(username, "test");
            player = server.getDataBase().getPlayer(username);
            log.debug("Player created");
        } else {
            String message = server.getDataBase().authorizeUser(username, "password");

            if (message != null) {
                disconnect(message);
                return;
            }

            player = server.getDataBase().getPlayer(username);
            if (!server.getWorld().canJoin(player)) {
                disconnect("Already in game");
                return;
            }
        }

        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(username.getBytes(StandardCharsets.UTF_8));
            byte[] digest = messageDigest.digest();
            log.debug("Player hash created");
            player.setDigest(digest);
            MainServer.getInstance().getNetworkSystem().addHandler(username, this);
            sendTCPPacket(new PacketLoginTCPSuccess(digest));
        } catch (NoSuchAlgorithmException e) {
            log.error("Couldn't create player hash", e);
            disconnect("Player's hash creation error");
        }
    }

    public void loginUDP(String login, byte[] digest, ChannelHandlerContext ctx, InetSocketAddress remoteAddress) {
        if (player == null) {
            log.error("Player object not found for login {} {}", login, remoteAddress);
            ctx.channel().close();
            return;
        }

        if (!Arrays.equals(digest, player.getDigest())) {
            log.error("Player {} sent wrong digest", login);
            ctx.channel().close();
            return;
        }

        datagramChannel = (DatagramChannel) ctx.channel();
        this.remoteAddress = remoteAddress;
        ((MessageHandlerUDP) datagramChannel.pipeline().get("handler")).setPlayerNetworkHandler(this);
        ctx.writeAndFlush(new DefaultAddressedEnvelope<PacketOut, SocketAddress>(new PacketLoginUDPSuccess(), remoteAddress)).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    public void joinGame() {
        connectionState = ConnectionState.PLAY;
        player.setNetworkHandler(this);
        world.addNewPlayer(player);
        sendTCPPacket(new PacketJoinGame(world.getSeed()));
        if (player.getFaction() != null) {
            server.getDataBase().getPlayerShips(player);
            if (player.getShips().isEmpty()) {
                server.getPlayerManager().respawnPlayer(player, 0, 0);
            }
        } else {
            sendTCPPacket(new PacketOpenGui(GuiType.SELECT_FACTION));
        }
    }

    protected void disconnect(String reason) {
        try {
            log.info("Disconnecting {}: {}", socketChannel.remoteAddress(), reason);
            sendTCPPacket(new PacketDisconnectLogin(reason));
            closeChannel(reason);
        } catch (Exception e) {
            log.error("Error whilst disconnecting player", e);
        }
    }

    public void onDisconnected() {
        if (connectionStateBeforeDisconnect == ConnectionState.PLAY) {
            log.info("{} lost connection: {}", player, terminationReason);
            server.getWorld().removePlayer(player);
            server.getDataBase().saveUser(player);
            List<Ship> ships = player.getShips();
            for (int i = 0, shipsSize = ships.size(); i < shipsSize; i++) {
                Ship s = ships.get(i);
                s.setOwner(null);
                s.setDead(true);
                server.getNetworkSystem().sendUDPPacketToAllNearby(new PacketRemoveObject(s), s.getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
            }

            if (server.isSinglePlayer()) {
                log.info("Stopping local server");
                server.stop();
            }
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