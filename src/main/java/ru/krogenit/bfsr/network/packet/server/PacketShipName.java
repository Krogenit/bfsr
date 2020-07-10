package ru.krogenit.bfsr.network.packet.server;

import lombok.NoArgsConstructor;
import ru.krogenit.bfsr.core.Core;
import ru.krogenit.bfsr.entity.CollisionObject;
import ru.krogenit.bfsr.entity.ship.Ship;
import ru.krogenit.bfsr.network.PacketBuffer;
import ru.krogenit.bfsr.network.client.NetworkManagerClient;
import ru.krogenit.bfsr.network.server.ServerPacket;

import java.io.IOException;

@NoArgsConstructor
public class PacketShipName extends ServerPacket {

	private int id;
	private String name;

	public PacketShipName(Ship ship) {
		this.id = ship.getId();
		this.name = ship.getName();
	}

	@Override
	public void read(PacketBuffer data) throws IOException {
		id = data.readInt();
		name = data.readStringFromBuffer(2048);
	}

	@Override
	public void write(PacketBuffer data) throws IOException {
		data.writeInt(id);
		data.writeStringToBuffer(name);
	}

	@Override
	public void processOnClientSide(NetworkManagerClient networkManager) {
		CollisionObject obj = Core.getCore().getWorld().getEntityById(id);
		if (obj != null) {
			Ship ship = (Ship) obj;
			ship.setName(name);
		}
	}
}