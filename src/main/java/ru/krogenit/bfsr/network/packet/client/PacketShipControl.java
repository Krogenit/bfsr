package ru.krogenit.bfsr.network.packet.client;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import ru.krogenit.bfsr.entity.CollisionObject;
import ru.krogenit.bfsr.entity.ship.PlayerServer;
import ru.krogenit.bfsr.entity.ship.Ship;
import ru.krogenit.bfsr.network.PacketBuffer;
import ru.krogenit.bfsr.network.client.ClientPacket;
import ru.krogenit.bfsr.network.server.NetworkManagerServer;
import ru.krogenit.bfsr.server.MainServer;
import ru.krogenit.bfsr.world.WorldServer;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketShipControl extends ClientPacket {

	private int id;
	private boolean control;

	public void read(PacketBuffer data) throws IOException {
		id = data.readInt();
		control = data.readBoolean();
	}

	public void write(PacketBuffer data) throws IOException {
		data.writeInt(id);
		data.writeBoolean(control);
	}

	@Override
	public void processOnServerSide(NetworkManagerServer networkManager, MainServer server, WorldServer world, PlayerServer player) {
		CollisionObject obj = world.getEntityById(id);
		if (obj != null) {
			Ship ship = (Ship) obj;
			ship.setControlledByPlayer(control);
		}
	}
}