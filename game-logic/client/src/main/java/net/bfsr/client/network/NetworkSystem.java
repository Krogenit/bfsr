package net.bfsr.client.network;

import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;
import lombok.Setter;
import net.bfsr.client.Client;
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
    private static final long PING_CHECK_INTERVAL = 5000;

    private final NetworkManagerTCP networkManagerTCP = new NetworkManagerTCP();
    private final NetworkManagerUDP networkManagerUDP = new NetworkManagerUDP();

    private final PacketRegistry<NetworkSystem> packetRegistry = new PacketRegistry<>();

    private final Queue<Packet> inboundPacketQueue = new ConcurrentLinkedQueue<>();

    @Getter
    @Setter
    private ConnectionState connectionState = ConnectionState.DISCONNECTED;

    @Setter
    @Getter
    private long handshakeTime;
    private long lastPingCheck;
    @Setter
    @Getter
    private int connectionId;

    private String login;

    public void init() {
        packetRegistry.registerPackets(Side.CLIENT);
    }

    public void connect(InetAddress address, int port, String login) {
        this.login = login;

        networkManagerTCP.connect(this, address, port);
        networkManagerUDP.connect(this, address, port);
        connectionState = ConnectionState.CONNECTING;

        sendPacketTCP(new PacketRegisterTCP());
    }

    public void onChannelsRegistered() {
        sendPacketTCP(new PacketHandshake(handshakeTime = System.nanoTime()));
        sendPacketTCP(new PacketLogin(login));
    }

    public void update(double time) {
        if (connectionState != ConnectionState.DISCONNECTED) {
            processReceivedPackets(time);

            if (connectionState == ConnectionState.CONNECTED) {
                long now = System.currentTimeMillis();
                if (now - lastPingCheck > PING_CHECK_INTERVAL) {
                    sendPacketUDP(new PacketPing(Side.CLIENT, System.nanoTime() - handshakeTime));
                    lastPingCheck = now;
                }
            }
        }
    }

    private void processReceivedPackets(double time) {
        for (int i = 0; !inboundPacketQueue.isEmpty() && i < 1000; i++) {
            Packet packet = inboundPacketQueue.poll();
            if (packet.canProcess(time)) {
                processPacket(packet);
            } else {
                inboundPacketQueue.add(packet);
            }
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
        if (connectionState == ConnectionState.CONNECTED) {
            Client.get().addFutureTask(() -> {
                Client.get().quitToMainMenu();
                Client.get().openGui(new GuiDisconnected(new GuiMainMenu(), "disconnect.lost", reason));
            });
        } else if (connectionState == ConnectionState.LOGIN) {
            Client.get().addFutureTask(() -> Client.get().openGui(new GuiDisconnected(new GuiMainMenu(), "login.failed", reason)));
        } else {
            Client.get().addFutureTask(() -> Client.get().openGui(new GuiDisconnected(new GuiMainMenu(), "other", reason)));
        }

        connectionState = ConnectionState.DISCONNECTED;

        shutdown();
        clear();
        Client.get().addFutureTask(() -> Client.get().stopLocalServer());
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
        setConnectionState(ConnectionState.DISCONNECTED);
    }
}