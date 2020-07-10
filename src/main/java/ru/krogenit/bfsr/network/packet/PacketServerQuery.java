package ru.krogenit.bfsr.network.packet;

import java.io.IOException;

import lombok.NoArgsConstructor;
import ru.krogenit.bfsr.entity.ship.PlayerServer;
import ru.krogenit.bfsr.network.Packet;
import ru.krogenit.bfsr.network.PacketBuffer;
import ru.krogenit.bfsr.network.client.NetworkManagerClient;
import ru.krogenit.bfsr.network.server.NetworkManagerServer;
import ru.krogenit.bfsr.network.packet.server.PacketServerInfo;
import ru.krogenit.bfsr.server.MainServer;
import ru.krogenit.bfsr.world.WorldServer;

@NoArgsConstructor
public class PacketServerQuery extends Packet {

	@Override
	public void read(PacketBuffer data) throws IOException {

	}

	@Override
	public void write(PacketBuffer data) throws IOException {

	}

	@Override
	public void processOnServerSide(NetworkManagerServer networkManager, MainServer server, WorldServer world, PlayerServer player) {
		networkManager.scheduleOutboundPacket(new PacketServerInfo(server.getStatus()));
	}

	@Override
	public void processOnClientSide(NetworkManagerClient networkManager) {

	}

	@Override
	public boolean hasPriority() {
		return true;
	}
}