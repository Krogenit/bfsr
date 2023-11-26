package net.bfsr.client.network;

import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;
import lombok.Setter;
import net.bfsr.client.Core;
import net.bfsr.client.gui.connect.GuiDisconnected;
import net.bfsr.client.gui.main.GuiMainMenu;
import net.bfsr.client.network.manager.NetworkManagerTCP;
import net.bfsr.client.network.manager.NetworkManagerUDP;
import net.bfsr.engine.util.Side;
import net.bfsr.network.ConnectionState;
import net.bfsr.network.NetworkHandler;
import net.bfsr.network.packet.Packet;
import net.bfsr.network.packet.PacketRegistry;
import net.bfsr.network.packet.client.PacketHandshake;
import net.bfsr.network.packet.client.PacketLogin;
import net.bfsr.network.packet.common.PacketPing;
import net.bfsr.network.packet.common.PacketRegisterTCP;

import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class NetworkSystem extends NetworkHandler {
    private final NetworkManagerTCP networkManagerTCP = new NetworkManagerTCP();
    private final NetworkManagerUDP networkManagerUDP = new NetworkManagerUDP();

    private final PacketRegistry<NetworkSystem> packetRegistry = new PacketRegistry<>();

    private final Queue<Packet> inboundPacketQueue = new ConcurrentLinkedQueue<>();

    @Getter
    @Setter
    private ConnectionState connectionState = ConnectionState.NOT_CONNECTED;

    @Setter
    @Getter
    private long handshakeTime;
    private long lastPingCheck;

    public void init() {
        packetRegistry.registerPackets(Side.CLIENT);
    }

    public void connect(InetAddress address, int port) {
        networkManagerTCP.connect(this, address, port);
        networkManagerUDP.connect(this, address, port);
        connectionState = ConnectionState.HANDSHAKE;

        sendPacketTCP(new PacketRegisterTCP());
    }

    public void onChannelsRegistered() {
        sendPacketTCP(new PacketHandshake(handshakeTime = System.nanoTime()));
        sendPacketTCP(new PacketLogin("Local Player"));
    }

    public void update() {
        if (connectionState != ConnectionState.NOT_CONNECTED) {
            processReceivedPackets();

            if (connectionState == ConnectionState.PLAY) {
                long now = System.currentTimeMillis();
                if (now - lastPingCheck > 1000) {
                    sendPacketUDP(new PacketPing(System.nanoTime() - handshakeTime));
                    lastPingCheck = now;
                }
            }
        }
    }

    private void processReceivedPackets() {
        for (int i = 1000; !inboundPacketQueue.isEmpty() && i >= 0; --i) {
            Packet packet = inboundPacketQueue.poll();
            processPacket(packet);
        }
    }

    private void processPacket(Packet packet) {
        packetRegistry.getPacketHandler(packet).handle(packet, this);
    }

    public void handle(Packet packet, ChannelHandlerContext ctx) {
        packetRegistry.getPacketHandler(packet).handle(packet, this, ctx);
    }

    public void sendPacketTCP(Packet packet) {
        networkManagerTCP.sendPacket(packet);
    }

    public void sendPacketUDP(Packet packet) {
        networkManagerUDP.sendPacket(packet);
    }

    public void onDisconnect(String reason) {
        if (connectionState == ConnectionState.PLAY) {
            Core.get().addFutureTask(() -> {
                Core.get().setWorld(null);
                Core.get().openGui(new GuiDisconnected(new GuiMainMenu(), "disconnect.lost", reason));
            });
        } else if (connectionState == ConnectionState.LOGIN) {
            Core.get().addFutureTask(() -> Core.get().openGui(new GuiDisconnected(new GuiMainMenu(), "login.failed", reason)));
        } else {
            Core.get().addFutureTask(() -> Core.get().openGui(new GuiDisconnected(new GuiMainMenu(), "other", reason)));
        }

        connectionState = ConnectionState.NOT_CONNECTED;

        shutdown();
        clear();
        Core.get().addFutureTask(() -> Core.get().stopServer());
    }

    @Override
    public void addPacketToQueue(Packet packet) {
        inboundPacketQueue.add(packet);
    }

    public Packet createPacket(int packetId)
            throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
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