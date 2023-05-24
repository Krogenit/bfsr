package net.bfsr.client.network;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.client.Core;
import net.bfsr.client.gui.GuiDisconnected;
import net.bfsr.client.gui.menu.GuiMainMenu;
import net.bfsr.client.network.manager.NetworkManagerTCP;
import net.bfsr.client.network.manager.NetworkManagerUDP;
import net.bfsr.client.network.packet.PacketIn;
import net.bfsr.client.network.packet.PacketRegistry;
import net.bfsr.client.network.packet.common.PacketPing;
import net.bfsr.network.ConnectionState;
import net.bfsr.network.Packet;
import net.bfsr.network.PacketOut;

import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class NetworkSystem {
    private final NetworkManagerTCP networkManagerTCP = new NetworkManagerTCP();
    private final NetworkManagerUDP networkManagerUDP = new NetworkManagerUDP();

    private final PacketRegistry packetRegistry = new PacketRegistry();

    private final Queue<PacketIn> inboundPacketQueue = new ConcurrentLinkedQueue<>();

    @Getter
    @Setter
    private ConnectionState connectionState = ConnectionState.NOT_CONNECTED;

    @Setter
    @Getter
    private long handshakeTime;
    private long lastPingCheck;

    public void init() {
        packetRegistry.registerPackets();
    }

    public void connectTCP(InetAddress address, int port) {
        networkManagerTCP.connect(this, address, port);
        connectionState = ConnectionState.HANDSHAKE;
    }

    public void connectUDP(InetAddress address, int port) {
        networkManagerUDP.connect(this, address, port);
    }

    public void update() {
        if (connectionState != ConnectionState.NOT_CONNECTED) {
            processReceivedPackets();

            if (connectionState == ConnectionState.PLAY) {
                long now = System.currentTimeMillis();
                if (now - lastPingCheck > 1000) {
                    sendPacketUDP(new PacketPing());
                    lastPingCheck = now;
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
        packet.processOnClientSide();
    }

    public void sendPacketTCP(PacketOut packet) {
        networkManagerTCP.sendPacket(packet);
    }

    public void sendPacketUDP(PacketOut packet) {
        networkManagerUDP.sendPacket(packet);
    }

    public void onDisconnect(String reason) {
        if (connectionState == ConnectionState.PLAY) {
            Core.get().addFutureTask(() -> {
                Core.get().setWorld(null);
                Core.get().setCurrentGui(new GuiDisconnected(new GuiMainMenu(), "disconnect.lost", reason));
            });
        } else if (connectionState == ConnectionState.LOGIN) {
            Core.get().addFutureTask(() -> Core.get().setCurrentGui(new GuiDisconnected(new GuiMainMenu(), "login.failed", reason)));
        } else {
            Core.get().addFutureTask(() -> Core.get().setCurrentGui(new GuiDisconnected(new GuiMainMenu(), "other", reason)));
        }

        connectionState = ConnectionState.NOT_CONNECTED;

        shutdown();
        clear();
        Core.get().addFutureTask(() -> Core.get().stopServer());
    }

    public void addPacketToInboundQueue(PacketIn packet) {
        inboundPacketQueue.add(packet);
    }

    public PacketIn createPacket(int packetId) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        return packetRegistry.createPacket(packetId);
    }

    public int getPacketId(Packet packet) {
        return packetRegistry.getPacketId(packet);
    }

    public void shutdown() {
        networkManagerTCP.shutdown();
        networkManagerUDP.shutdown();
    }

    public boolean isChannelOpen() {
        return networkManagerTCP.isChannelOpen() && networkManagerUDP.isChannelOpen();
    }

    public void closeChannels() {
        networkManagerTCP.closeChannel();
        networkManagerUDP.closeChannel();
    }

    public void clear() {
        inboundPacketQueue.clear();
        setConnectionState(ConnectionState.NOT_CONNECTED);
    }
}