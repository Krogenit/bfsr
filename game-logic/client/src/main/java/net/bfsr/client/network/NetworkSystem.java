package net.bfsr.client.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.bfsr.client.Client;
import net.bfsr.client.gui.connect.GuiDisconnected;
import net.bfsr.client.gui.main.GuiMainMenu;
import net.bfsr.client.network.manager.NetworkManagerTCP;
import net.bfsr.client.network.manager.NetworkManagerUDP;
import net.bfsr.engine.gui.Gui;
import net.bfsr.engine.gui.NoGui;
import net.bfsr.engine.network.ConnectionState;
import net.bfsr.engine.network.NetworkHandler;
import net.bfsr.engine.network.packet.Packet;
import net.bfsr.engine.network.packet.PacketRegistry;
import net.bfsr.engine.network.packet.client.PacketHandshake;
import net.bfsr.engine.network.packet.client.PacketLogin;
import net.bfsr.engine.network.packet.common.PacketPing;
import net.bfsr.engine.network.packet.common.PacketRegisterTCP;
import net.bfsr.engine.util.Side;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@RequiredArgsConstructor
public class NetworkSystem extends NetworkHandler {
    private static final long PING_CHECK_INTERVAL = 1000;
    private static final double CHANGE_TIME_EVENT_THRESHOLD = 1_000_000;

    private final Client client;

    private final NetworkManagerTCP networkManagerTCP = new NetworkManagerTCP();
    private final NetworkManagerUDP networkManagerUDP = new NetworkManagerUDP();

    private final PacketRegistry<NetworkSystem> packetRegistry = new PacketRegistry<>();

    private final Queue<Packet> inboundPacketQueue = new ConcurrentLinkedQueue<>();

    @Getter
    @Setter
    private ConnectionState connectionState = ConnectionState.DISCONNECTED;

    private long lastPingCheck;
    @Setter
    @Getter
    private int connectionId;

    @Getter
    private double clientToServerTimeDiffInNanos;
    @Getter
    private double averageClientToServerTimeDiffInNanos;
    private final DoubleList clientToServerTimeResults = new DoubleArrayList();

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
        sendPacketTCP(new PacketHandshake());
        sendPacketTCP(new PacketLogin(login));
    }

    public void update(double time) {
        if (connectionState != ConnectionState.DISCONNECTED) {
            processReceivedPackets(time);

            if (connectionState == ConnectionState.CONNECTED) {
                long now = System.currentTimeMillis();
                if (now - lastPingCheck > PING_CHECK_INTERVAL) {
                    sendPacketUDP(new PacketPing(Side.CLIENT));
                    lastPingCheck = now;
                }
            }
        }
    }

    private void processReceivedPackets(double time) {
        int size = inboundPacketQueue.size();
        for (int i = 0; !inboundPacketQueue.isEmpty() && i < size; i++) {
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

    public void addClientToServerTimeDiffResult(long clientToServerTimeDiff) {
        this.clientToServerTimeDiffInNanos = clientToServerTimeDiff;
        this.clientToServerTimeResults.add(clientToServerTimeDiffInNanos);

        if (clientToServerTimeResults.size() > 100) {
            clientToServerTimeResults.removeFirst();
        }

        double allTimeDiffs = 0.0f;
        for (int i = 0; i < clientToServerTimeResults.size(); i++) {
            allTimeDiffs += clientToServerTimeResults.getDouble(i);
        }

        double oldAverageClientToServerTimeDiffInNanos = averageClientToServerTimeDiffInNanos;
        this.averageClientToServerTimeDiffInNanos = allTimeDiffs / clientToServerTimeResults.size();

        double timeDiff = Math.abs(oldAverageClientToServerTimeDiffInNanos - averageClientToServerTimeDiffInNanos);
        if (timeDiff > CHANGE_TIME_EVENT_THRESHOLD) {
            client.onClientToServerTimeDiffChange();
        }
    }

    public void onDisconnect(String reason) {
        Gui currentGui = client.getGuiManager().getGui();
        Gui parentGui = currentGui == NoGui.get() ? new GuiMainMenu() : currentGui;

        if (connectionState == ConnectionState.CONNECTED) {
            client.addFutureTask(() -> {
                client.quitToMainMenu();
                client.openGui(new GuiDisconnected(parentGui, "disconnect.lost", reason));
            });
        } else if (connectionState == ConnectionState.LOGIN) {
            client.addFutureTask(() -> client.openGui(new GuiDisconnected(parentGui, "login.failed", reason)));
        } else {
            client.addFutureTask(() -> client.openGui(new GuiDisconnected(parentGui, "other", reason)));
        }

        connectionState = ConnectionState.DISCONNECTED;

        shutdown();
        clear();
        client.addFutureTask(client::stopLocalServer);
    }

    @Override
    public void addPacketToQueue(Packet packet) {
        inboundPacketQueue.add(packet);
    }

    public Packet decodePacket(ByteBuf buffer) throws IOException {
        int packetId = buffer.readByte();

        try {
            Packet packet = packetRegistry.createPacket(packetId);
            packet.read(buffer, client);
            return packet;
        } catch (IOException e) {
            throw new IOException("Can't read packet with id " + packetId, e);
        } catch (Exception e) {
            throw new IOException("Can't create packet with id " + packetId, e);
        }
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

    @Override
    public void clear() {
        super.clear();
        inboundPacketQueue.clear();
        setConnectionState(ConnectionState.DISCONNECTED);
        clientToServerTimeResults.clear();
        averageClientToServerTimeDiffInNanos = 0;
    }
}