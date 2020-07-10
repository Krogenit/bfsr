package ru.krogenit.bfsr.network.packet.client;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import ru.krogenit.bfsr.entity.ship.PlayerServer;
import ru.krogenit.bfsr.network.PacketBuffer;
import ru.krogenit.bfsr.network.client.ClientPacket;
import ru.krogenit.bfsr.network.server.NetworkManagerServer;
import ru.krogenit.bfsr.server.MainServer;
import ru.krogenit.bfsr.world.WorldServer;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketCameraPosition extends ClientPacket {
	
	private float x, y;

	@Override
	public void read(PacketBuffer data) throws IOException {
		x = data.readFloat();
		y = data.readFloat();
	}

	@Override
	public void write(PacketBuffer data) throws IOException {
		data.writeFloat(x);
		data.writeFloat(y);
	}

	@Override
	public void processOnServerSide(NetworkManagerServer networkManager, MainServer server, WorldServer world, PlayerServer player) {
		player.setPosition(x, y);
	}
}