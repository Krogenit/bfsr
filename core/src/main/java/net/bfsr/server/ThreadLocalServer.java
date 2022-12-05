package net.bfsr.server;

import lombok.Getter;
import net.bfsr.client.gui.menu.GuiMainMenu;
import net.bfsr.core.Core;
import net.bfsr.network.client.NetworkManagerClient;
import net.bfsr.network.packet.PacketHandshake;
import net.bfsr.network.packet.PacketLoginStart;
import net.bfsr.network.status.EnumConnectionState;

import java.net.SocketAddress;

@Getter
public class ThreadLocalServer extends Thread {
    private final MainServer server;

    public ThreadLocalServer() {
        server = new MainServer(true);
    }

    @Override
    public void run() {
        server.run();
    }

    public void stopServer() {
        server.stop();
    }

    public void connectToLocalServer() {
        while (!server.isRunning()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        SocketAddress socketAddress = server.getNetworkSystem().addLocalEndpoint();
        NetworkManagerClient networkManager = NetworkManagerClient.provideLocalClient(socketAddress, new GuiMainMenu());
        networkManager.scheduleOutboundPacket(new PacketHandshake(5, socketAddress.toString(), 0, EnumConnectionState.LOGIN));
        networkManager.scheduleOutboundPacket(new PacketLoginStart("Local Player", "local", false));
        Core.getCore().setNetworkManager(networkManager);
    }
}
