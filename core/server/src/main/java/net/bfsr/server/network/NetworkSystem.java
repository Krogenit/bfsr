package net.bfsr.server.network;

import gnu.trove.map.TMap;
import gnu.trove.map.hash.THashMap;
import lombok.RequiredArgsConstructor;
import net.bfsr.network.PacketOut;
import net.bfsr.server.MainServer;
import net.bfsr.server.network.handler.PlayerNetworkHandler;
import net.bfsr.server.network.manager.NetworkManagerTCP;
import net.bfsr.server.network.manager.NetworkManagerUDP;
import net.bfsr.server.network.packet.PacketIn;
import net.bfsr.server.network.packet.PacketRegistry;
import net.bfsr.server.player.PlayerServer;
import org.joml.Vector2f;

import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class NetworkSystem {
    private final MainServer server;

    private final NetworkManagerTCP networkManagerTCP = new NetworkManagerTCP();
    private final NetworkManagerUDP networkManagerUDP = new NetworkManagerUDP();

    private final PacketRegistry packetRegistry = new PacketRegistry();

    private final List<PlayerNetworkHandler> networkHandlers = new ArrayList<>();
    private final TMap<String, PlayerNetworkHandler> networkHandlerMap = new THashMap<>();

    public void init() {
        packetRegistry.registerPackets();
    }

    public void startup(InetAddress address, int port) {
        networkManagerTCP.startup(this, address, port);
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
                        networkHandlerMap.remove(networkHandler.getPlayer().getUserName());
                    }

                    networkHandler.onDisconnected();
                }
            }
        }
    }

    public void sendTCPPacketTo(PacketOut packet, PlayerServer player) {
        player.getNetworkHandler().sendTCPPacket(packet);
    }

    public void sendUDPPacketTo(PacketOut packet, PlayerServer player) {
        player.getNetworkHandler().sendUDPPacket(packet);
    }

    public void sendTCPPacketToAllExcept(PacketOut packet, PlayerServer player) {
        sendPacketToAllExcept(player, playerNetworkHandler -> playerNetworkHandler.sendTCPPacket(packet));
    }

    public void sendUDPPacketToAllExcept(PacketOut packet, PlayerServer player) {
        sendPacketToAllExcept(player, playerNetworkHandler -> playerNetworkHandler.sendUDPPacket(packet));
    }

    private void sendPacketToAllExcept(PlayerServer player, Consumer<PlayerNetworkHandler> protocol) {
        List<PlayerServer> players = server.getWorld().getPlayers();
        for (int i = 0, playersSize = players.size(); i < playersSize; i++) {
            PlayerServer player1 = players.get(i);
            if (player1 != player) protocol.accept(player1.getNetworkHandler());
        }
    }

    public void sendTCPPacketToAllNearby(PacketOut packet, Vector2f pos, float dist) {
        sendPacketToAllNearby(pos.x, pos.y, dist, playerNetworkHandler -> playerNetworkHandler.sendTCPPacket(packet));
    }

    public void sendUDPPacketToAllNearby(PacketOut packet, Vector2f pos, float dist) {
        sendPacketToAllNearby(pos.x, pos.y, dist, playerNetworkHandler -> playerNetworkHandler.sendUDPPacket(packet));
    }

    public void sendTCPPacketToAllNearby(PacketOut packet, float x, float y, float dist) {
        sendPacketToAllNearby(x, y, dist, playerNetworkHandler -> playerNetworkHandler.sendTCPPacket(packet));
    }

    public void sendUDPPacketToAllNearby(PacketOut packet, float x, float y, float dist) {
        sendPacketToAllNearby(x, y, dist, playerNetworkHandler -> playerNetworkHandler.sendUDPPacket(packet));
    }

    private void sendPacketToAllNearby(float x, float y, float dist, Consumer<PlayerNetworkHandler> protocol) {
        List<PlayerServer> players = server.getWorld().getPlayers();
        for (int i = 0, playersSize = players.size(); i < playersSize; i++) {
            PlayerServer player = players.get(i);
            if (player.getPosition().distance(x, y) <= dist) {
                protocol.accept(player.getNetworkHandler());
            }
        }
    }

    public void sendTCPPacketToAllNearbyExcept(PacketOut packet, Vector2f pos, float dist, PlayerServer player1) {
        this.sendPacketToAllNearbyExcept(pos.x, pos.y, dist, player1, playerNetworkHandler -> playerNetworkHandler.sendTCPPacket(packet));
    }

    public void sendUDPPacketToAllNearbyExcept(PacketOut packet, Vector2f pos, float dist, PlayerServer player1) {
        this.sendPacketToAllNearbyExcept(pos.x, pos.y, dist, player1, playerNetworkHandler -> playerNetworkHandler.sendUDPPacket(packet));
    }

    private void sendPacketToAllNearbyExcept(float x, float y, float dist, PlayerServer player1, Consumer<PlayerNetworkHandler> protocol) {
        List<PlayerServer> players = server.getWorld().getPlayers();
        for (int i = 0, playersSize = players.size(); i < playersSize; i++) {
            PlayerServer player = players.get(i);
            if (player1 != player && player.getPosition().distance(x, y) <= dist) {
                protocol.accept(player.getNetworkHandler());
            }
        }
    }

    public void sendTCPPacketToAll(PacketOut packet) {
        sendPacketToAll(playerNetworkHandler -> playerNetworkHandler.sendTCPPacket(packet));
    }

    public void sendUDPPacketToAll(PacketOut packet) {
        sendPacketToAll(playerNetworkHandler -> playerNetworkHandler.sendUDPPacket(packet));
    }

    private void sendPacketToAll(Consumer<PlayerNetworkHandler> protocol) {
        List<PlayerServer> players = server.getWorld().getPlayers();
        for (int i = 0, playersSize = players.size(); i < playersSize; i++) {
            protocol.accept(players.get(i).getNetworkHandler());
        }
    }

    public void addHandler(PlayerNetworkHandler playerNetworkHandler) {
        synchronized (networkHandlers) {
            networkHandlers.add(playerNetworkHandler);
        }
    }

    public void addHandler(String login, PlayerNetworkHandler playerNetworkHandler) {
        networkHandlerMap.put(login, playerNetworkHandler);
    }

    public PlayerNetworkHandler getHandler(String login) {
        return networkHandlerMap.get(login);
    }

    public PacketIn createPacket(int packetId) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        return packetRegistry.createPacket(packetId);
    }

    public int getPacketId(PacketOut packet) {
        return packetRegistry.getPacketId(packet);
    }

    public void shutdown() {
        networkManagerTCP.shutdown();
        networkManagerUDP.shutdown();
    }
}
