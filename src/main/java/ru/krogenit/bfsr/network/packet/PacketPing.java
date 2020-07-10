package ru.krogenit.bfsr.network.packet;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import ru.krogenit.bfsr.core.Core;
import ru.krogenit.bfsr.entity.ship.PlayerServer;
import ru.krogenit.bfsr.network.Packet;
import ru.krogenit.bfsr.network.PacketBuffer;
import ru.krogenit.bfsr.network.client.NetworkManagerClient;
import ru.krogenit.bfsr.network.server.NetworkManagerServer;
import ru.krogenit.bfsr.server.MainServer;
import ru.krogenit.bfsr.world.WorldServer;

import java.io.IOException;

@NoArgsConstructor
@AllArgsConstructor
public class PacketPing extends Packet {

	private long time;

	@Override
	public void read(PacketBuffer data) throws IOException {
		this.time = data.readLong();
	}

	@Override
	public void write(PacketBuffer data) throws IOException {
		data.writeLong(this.time);
	}

	@Override
	public void processOnClientSide(NetworkManagerClient networkManager) {
		Core.getCore().getGuiInGame().setPing(time);
	}

	@Override
	public void processOnServerSide(NetworkManagerServer networkManager, MainServer server, WorldServer world, PlayerServer player) {
		this.time = System.currentTimeMillis() - time;
		networkManager.scheduleOutboundPacket(new PacketPing(time));
	}

	@Override
	public boolean hasPriority() {
		return true;
	}
}