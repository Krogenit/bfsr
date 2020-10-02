package ru.krogenit.bfsr.network.packet.server;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import ru.krogenit.bfsr.core.Core;
import ru.krogenit.bfsr.entity.CollisionObject;
import ru.krogenit.bfsr.entity.ship.Ship;
import ru.krogenit.bfsr.faction.Faction;
import ru.krogenit.bfsr.network.PacketBuffer;
import ru.krogenit.bfsr.network.client.NetworkManagerClient;
import ru.krogenit.bfsr.network.server.ServerPacket;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketShipFaction extends ServerPacket {

	private int id;
	private int faction;

	public PacketShipFaction(Ship ship) {
		this.id = ship.getId();
		this.faction = ship.getFaction().ordinal();
	}

	@Override
	public void read(PacketBuffer data) throws IOException {
		id = data.readInt();
		faction = data.readInt();
	}

	@Override
	public void write(PacketBuffer data) throws IOException {
		data.writeInt(id);
		data.writeInt(faction);
	}

	@Override
	public void processOnClientSide(NetworkManagerClient networkManager) {
		CollisionObject obj = Core.getCore().getWorld().getEntityById(id);
		if (obj instanceof Ship) {
			Ship ship = (Ship) obj;
			ship.setFaction(Faction.values()[faction]);
		}
	}
}