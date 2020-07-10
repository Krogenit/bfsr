package ru.krogenit.bfsr.server;

import java.net.SocketAddress;

import lombok.Getter;
import ru.krogenit.bfsr.client.gui.menu.GuiMainMenu;
import ru.krogenit.bfsr.core.Core;
import ru.krogenit.bfsr.network.NetworkManager;
import ru.krogenit.bfsr.network.client.NetworkManagerClient;
import ru.krogenit.bfsr.network.packet.PacketHandshake;
import ru.krogenit.bfsr.network.packet.PacketLoginStart;
import ru.krogenit.bfsr.network.status.EnumConnectionState;

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
		while(!server.isRunning()) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		SocketAddress socketAddress = server.getNetworkSystem().addLocalEndpoint();
		NetworkManagerClient networkManager = NetworkManagerClient.provideLocalClient(socketAddress, new GuiMainMenu());
		networkManager.scheduleOutboundPacket(new PacketHandshake(5, socketAddress.toString(), 0, EnumConnectionState.LOGIN));
		networkManager.scheduleOutboundPacket(new PacketLoginStart("Local Player", "local", false));
		Core.getCore().setNetworkManager(networkManager);
	}
}
