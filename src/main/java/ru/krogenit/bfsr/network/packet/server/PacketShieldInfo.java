package ru.krogenit.bfsr.network.packet.server;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import ru.krogenit.bfsr.component.shield.Shield;
import ru.krogenit.bfsr.core.Core;
import ru.krogenit.bfsr.entity.CollisionObject;
import ru.krogenit.bfsr.entity.ship.Ship;
import ru.krogenit.bfsr.network.PacketBuffer;
import ru.krogenit.bfsr.network.client.NetworkManagerClient;
import ru.krogenit.bfsr.network.server.ServerPacket;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketShieldInfo extends ServerPacket {

	private int id;
	private float shieldValue;

	public PacketShieldInfo(Ship ship) {
		this.id = ship.getId();
		this.shieldValue = ship.getShield().getShield();
	}

	@Override
	public void read(PacketBuffer data) throws IOException {
		id = data.readInt();
		shieldValue = data.readFloat();
	}

	@Override
	public void write(PacketBuffer data) throws IOException {
		data.writeInt(id);
		data.writeFloat(shieldValue);
	}

	@Override
	public void processOnClientSide(NetworkManagerClient networkManager) {
		CollisionObject obj = Core.getCore().getWorld().getEntityById(id);
		if (obj != null) {
			Ship ship = (Ship) obj;
			Shield shield = ship.getShield();
			if(shield != null) {
				shield.setShield(shieldValue);
			}
		}
	}
}