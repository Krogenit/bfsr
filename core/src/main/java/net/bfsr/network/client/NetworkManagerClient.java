package net.bfsr.network.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.local.LocalChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.Setter;
import net.bfsr.client.gui.Gui;
import net.bfsr.client.gui.GuiDisconnected;
import net.bfsr.client.gui.menu.GuiMainMenu;
import net.bfsr.core.Core;
import net.bfsr.network.*;
import net.bfsr.network.packet.PacketPing;
import net.bfsr.network.status.EnumConnectionState;

import java.net.InetAddress;
import java.net.SocketAddress;

public class NetworkManagerClient extends NetworkManager {

    private final Core core = Core.getCore();
    @Setter
    private Gui currentGui;
    private long lastPingCheck;

    public NetworkManagerClient(Gui currentGui) {
        super(true);
        this.currentGui = currentGui;
    }

    @Override
    protected void update() {
        if (connectionState == EnumConnectionState.PLAY) {
            long now = System.currentTimeMillis();
            if (lastPingCheck == 0) {
                lastPingCheck = now;
            } else if (now - lastPingCheck > 1000) {
                scheduleOutboundPacket(new PacketPing(now));
                lastPingCheck = now;
            }
        }
    }

    @Override
    protected void processPacket(Packet packet) {
        packet.processOnClientSide(this);
    }

    @Override
    protected void onConnectionStateTransition(EnumConnectionState newState) {
        switch (connectionState) {
            case PLAY:
                throw new IllegalStateException("Unexpected protocol change!");
            case LOGIN:
                logger.debug("Switching protocol from " + connectionState + " to " + newState);
                break;
        }
    }

    @Override
    public void onDisconnect(String reason) {
        switch (connectionState) {
            case PLAY:
                this.core.setCurrentGui(new GuiDisconnected(new GuiMainMenu(), "disconnect.lost", reason));
                break;
            case LOGIN:
                this.core.setCurrentGui(new GuiDisconnected(currentGui, "connect.failed", reason));
                break;
        }
    }

    public static NetworkManagerClient provideLanClient(InetAddress netAdress, int port, Gui currentGui) {
        final NetworkManagerClient networkmanager = new NetworkManagerClient(currentGui);
        (new Bootstrap()).group(networkmanager.getEventLoops()).handler(new ChannelInitializer<Channel>() {

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
                        .addLast("timeout", new ReadTimeoutHandler(20))
                        .addLast("splitter", new PacketDecoder2())
                        .addLast("decoder", new PacketDecoder(NetworkManager.statistics))
                        .addLast("prepender", new PacketEncoder2())
                        .addLast("encoder", new PacketEncoder(NetworkManager.statistics))
                        .addLast("packet_handler", networkmanager);
            }
        }).channel(NioSocketChannel.class).connect(netAdress, port).syncUninterruptibly();
        return networkmanager;
    }

    public static NetworkManagerClient provideLocalClient(SocketAddress socketAddress, Gui currentGui) {
        final NetworkManagerClient networkmanager = new NetworkManagerClient(currentGui);
        (new Bootstrap()).group(networkmanager.getEventLoops()).handler(new ChannelInitializer<Channel>() {

            protected void initChannel(Channel channel) {
                channel.pipeline().addLast("packet_handler", networkmanager);
            }
        }).channel(LocalChannel.class).connect(socketAddress).syncUninterruptibly();
        return networkmanager;
    }
}
