package net.bfsr.server.network;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.bfsr.engine.network.packet.Packet;
import net.bfsr.engine.network.packet.PacketRegistry;
import net.bfsr.engine.util.Side;
import net.bfsr.server.ServerGameLogic;
import net.bfsr.server.network.handler.PlayerNetworkHandler;
import net.bfsr.server.network.manager.NetworkManagerTCP;
import net.bfsr.server.network.manager.NetworkManagerUDP;
import net.bfsr.server.player.Player;
import net.bfsr.server.player.PlayerManager;
import org.joml.Vector2f;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class NetworkSystem {
    private final NetworkManagerTCP networkManagerTCP = new NetworkManagerTCP();
    private final NetworkManagerUDP networkManagerUDP = new NetworkManagerUDP();

    @Getter
    private final PacketRegistry<PlayerNetworkHandler> packetRegistry = new PacketRegistry<>();

    @Getter
    private final List<PlayerNetworkHandler> networkHandlers = new ArrayList<>();
    private final TIntObjectMap<PlayerNetworkHandler> networkHandlerMap = new TIntObjectHashMap<>();
    private final TLongObjectMap<PlayerNetworkHandler> networkHandlerByAddressMap = new TLongObjectHashMap<>();
    private final PlayerManager playerManager;
    private final ServerGameLogic serverGameLogic;

    public void init() {
        packetRegistry.registerPackets(Side.SERVER);
    }

    public void startup(ServerGameLogic serverGameLogic, InetAddress address, int port) {
        networkManagerTCP.startup(serverGameLogic, this, address, port, networkManagerUDP::getChannel);
        networkManagerUDP.startup(this, address, port);
    }

    public void update() {
        synchronized (networkHandlers) {
            for (int i = 0; i < networkHandlers.size(); i++) {
                PlayerNetworkHandler networkHandler = networkHandlers.get(i);
                networkHandler.update();
                if (networkHandler.isClosed()) {
                    networkHandlers.remove(i--);

                    if (networkHandler.getPlayer() != null) {
                        networkHandlerMap.remove(networkHandler.getConnectionId());
                        networkHandlerByAddressMap.remove(networkHandler.getRemoteAddress().getPort());
                    }

                    networkHandler.onDisconnected();
                }
            }
        }
    }

    public void handle(Packet packet, PlayerNetworkHandler networkHandler, ChannelHandlerContext ctx, InetSocketAddress recipient) {
        packetRegistry.getPacketHandler(packet).handle(packet, networkHandler, ctx, recipient);
    }

    public void handle(Packet packet, PlayerNetworkHandler networkHandler, ChannelHandlerContext ctx) {
        packetRegistry.getPacketHandler(packet).handle(packet, networkHandler, ctx);
    }

    public void sendTCPPacketTo(Packet packet, Player player) {
        player.getNetworkHandler().sendTCPPacket(packet);
    }

    public void sendUDPPacketTo(Packet packet, Player player) {
        player.getNetworkHandler().sendUDPPacket(packet);
    }

    public void sendTCPPacketToAllExcept(Packet packet, Player player) {
        sendPacketToAllExcept(player, playerNetworkHandler -> playerNetworkHandler.sendTCPPacket(packet));
    }

    public void sendUDPPacketToAllExcept(Packet packet, Player player) {
        sendPacketToAllExcept(player, playerNetworkHandler -> playerNetworkHandler.sendUDPPacket(packet));
    }

    private void sendPacketToAllExcept(Player player, Consumer<PlayerNetworkHandler> protocol) {
        List<Player> players = playerManager.getPlayers();
        for (int i = 0, playersSize = players.size(); i < playersSize; i++) {
            Player player1 = players.get(i);
            if (player1 != player) {
                protocol.accept(player1.getNetworkHandler());
            }
        }
    }

    public void sendTCPPacketToAllNearby(Packet packet, Vector2f pos, float dist) {
        sendPacketToAllNearby(pos.x, pos.y, dist, playerNetworkHandler -> playerNetworkHandler.sendTCPPacket(packet));
    }

    public void sendUDPPacketToAllNearby(Packet packet, Vector2f pos, float dist) {
        sendPacketToAllNearby(pos.x, pos.y, dist, playerNetworkHandler -> playerNetworkHandler.sendUDPPacket(packet));
    }

    public void sendTCPPacketToAllNearby(Packet packet, float x, float y, float dist) {
        sendPacketToAllNearby(x, y, dist, playerNetworkHandler -> playerNetworkHandler.sendTCPPacket(packet));
    }

    public void sendUDPPacketToAllNearby(Packet packet, float x, float y, float dist) {
        sendPacketToAllNearby(x, y, dist, playerNetworkHandler -> playerNetworkHandler.sendUDPPacket(packet));
    }

    private void sendPacketToAllNearby(float x, float y, float dist, Consumer<PlayerNetworkHandler> protocol) {
        List<Player> players = playerManager.getPlayers();
        for (int i = 0, playersSize = players.size(); i < playersSize; i++) {
            Player player = players.get(i);
            if (player.getPosition().distance(x, y) <= dist) {
                protocol.accept(player.getNetworkHandler());
            }
        }
    }

    public void sendTCPPacketToAllNearbyExcept(Packet packet, Vector2f pos, float dist, Player player1) {
        this.sendPacketToAllNearbyExcept(pos.x, pos.y, dist, player1,
                playerNetworkHandler -> playerNetworkHandler.sendTCPPacket(packet));
    }

    public void sendUDPPacketToAllNearbyExcept(Packet packet, Vector2f pos, float dist, Player player1) {
        this.sendPacketToAllNearbyExcept(pos.x, pos.y, dist, player1,
                playerNetworkHandler -> playerNetworkHandler.sendUDPPacket(packet));
    }

    private void sendPacketToAllNearbyExcept(float x, float y, float dist, Player player1,
                                             Consumer<PlayerNetworkHandler> protocol) {
        List<Player> players = playerManager.getPlayers();
        for (int i = 0, playersSize = players.size(); i < playersSize; i++) {
            Player player = players.get(i);
            if (player1 != player && player.getPosition().distance(x, y) <= dist) {
                protocol.accept(player.getNetworkHandler());
            }
        }
    }

    public void sendTCPPacketToAll(Packet packet) {
        sendPacketToAll(playerNetworkHandler -> playerNetworkHandler.sendTCPPacket(packet));
    }

    public void sendUDPPacketToAll(Packet packet) {
        sendPacketToAll(playerNetworkHandler -> playerNetworkHandler.sendUDPPacket(packet));
    }

    private void sendPacketToAll(Consumer<PlayerNetworkHandler> protocol) {
        List<Player> players = playerManager.getPlayers();
        for (int i = 0, playersSize = players.size(); i < playersSize; i++) {
            protocol.accept(players.get(i).getNetworkHandler());
        }
    }

    public void registerHandler(PlayerNetworkHandler playerNetworkHandler) {
        synchronized (networkHandlers) {
            networkHandlers.add(playerNetworkHandler);
            networkHandlerMap.put(playerNetworkHandler.getConnectionId(), playerNetworkHandler);
        }
    }

    public PlayerNetworkHandler getHandler(int connectionId) {
        return networkHandlerMap.get(connectionId);
    }

    public PlayerNetworkHandler getHandler(long address) {
        return networkHandlerByAddressMap.get(address);
    }

    public Packet createPacket(int packetId)
            throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        return packetRegistry.createPacket(packetId);
    }

    public int getPacketId(Packet packet) {
        return packetRegistry.getPacketId(packet);
    }

    public void shutdown() {
        networkManagerTCP.shutdown();
        networkManagerUDP.shutdown();
    }

    public void registerUDPRemoteAddress(PlayerNetworkHandler handler, InetSocketAddress remoteAddress) {
        handler.setRemoteAddress(remoteAddress);

        long address = convertAddress(remoteAddress);
        synchronized (networkHandlers) {
            networkHandlerByAddressMap.put(address, handler);
        }
    }

    public static long convertAddress(InetSocketAddress socketAddress) {
        int ip = socketAddress.getAddress().hashCode();
        int port = socketAddress.getPort();
        return (((long) ip) << 32) | (port & 0xffffffffL);
    }

    public Packet decodePacket(ByteBuf buffer) throws IOException {
        int packetId = buffer.readByte();

        try {
            Packet packet = createPacket(packetId);
            packet.read(buffer, serverGameLogic);
            return packet;
        } catch (Exception e) {
            throw new IOException("Can't decode packet with id " + packetId, e);
        }
    }
}