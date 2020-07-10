package ru.krogenit.bfsr.network.packet.common;

import java.io.IOException;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import ru.krogenit.bfsr.entity.ship.PlayerServer;
import ru.krogenit.bfsr.network.Packet;
import ru.krogenit.bfsr.network.PacketBuffer;
import ru.krogenit.bfsr.network.client.NetworkManagerClient;
import ru.krogenit.bfsr.network.server.NetworkManagerServer;
import ru.krogenit.bfsr.server.MainServer;
import ru.krogenit.bfsr.world.WorldServer;

@NoArgsConstructor
@AllArgsConstructor
public class PacketKeepAlive extends Packet {

	private int time;

	@Override
	public void read(PacketBuffer data) throws IOException {
		this.time = data.readInt();
	}

	@Override
	public void write(PacketBuffer data) throws IOException {
		data.writeInt(this.time);
	}

	@Override
	public void processOnClientSide(NetworkManagerClient networkManager) {
		networkManager.scheduleOutboundPacket(new PacketKeepAlive(time));
	}

	@Override
	public void processOnServerSide(NetworkManagerServer networkManager, MainServer server, WorldServer world, PlayerServer player) {
		if (time == networkManager.getCurrentTimeInt()) {
			int var2 = (int) (System.nanoTime() / 1000000L - networkManager.getCurrentTime());
			player.setPing((player.getPing() * 3 + var2) / 4);
		}
	}

	@Override
	public boolean hasPriority() {
		return true;
	}
}