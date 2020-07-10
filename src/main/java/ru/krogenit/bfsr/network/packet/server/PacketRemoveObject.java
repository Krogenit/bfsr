package ru.krogenit.bfsr.network.packet.server;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import ru.krogenit.bfsr.client.particle.Particle;
import ru.krogenit.bfsr.core.Core;
import ru.krogenit.bfsr.entity.CollisionObject;
import ru.krogenit.bfsr.entity.bullet.Bullet;
import ru.krogenit.bfsr.entity.ship.Ship;
import ru.krogenit.bfsr.network.PacketBuffer;
import ru.krogenit.bfsr.network.client.NetworkManagerClient;
import ru.krogenit.bfsr.network.server.ServerPacket;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketRemoveObject extends ServerPacket {
	private int id;

	public PacketRemoveObject(CollisionObject obj) {
		this.id = obj.getId();
	}

	public void read(PacketBuffer data) throws IOException {
		id = data.readInt();
	}

	public void write(PacketBuffer data) throws IOException {
		data.writeInt(id);
	}

	@Override
	public void processOnClientSide(NetworkManagerClient networkManager) {
		CollisionObject obj = Core.getCore().getWorld().getEntityById(id);
		if (obj != null) {
			if(obj instanceof Ship) {
				((Ship) obj).destroyShip();
			} else if(obj instanceof Bullet) {
				obj.setDead(true);
			} else if(obj instanceof Particle) {
				Particle p = (Particle) obj;
				p.setDead(true);
			}
		}
	}
}