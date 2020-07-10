package ru.krogenit.bfsr.network.packet.client;

import java.io.IOException;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import ru.krogenit.bfsr.entity.ship.PlayerServer;
import ru.krogenit.bfsr.network.PacketBuffer;
import ru.krogenit.bfsr.network.client.ClientPacket;
import ru.krogenit.bfsr.network.server.NetworkManagerServer;
import ru.krogenit.bfsr.server.MainServer;
import ru.krogenit.bfsr.world.WorldServer;

@AllArgsConstructor
@NoArgsConstructor
public class PacketKey extends ClientPacket {
	
	private int keyId;
	private int action;

	@Override
	public void read(PacketBuffer data) throws IOException {
		keyId = data.readInt();
		action = data.readInt();
	}

	@Override
	public void write(PacketBuffer data) throws IOException {
		data.writeInt(keyId);
		data.writeInt(action);
	}

	@Override
	public void processOnServerSide(NetworkManagerServer networkManager, MainServer server, WorldServer world, PlayerServer player) {

	}
}