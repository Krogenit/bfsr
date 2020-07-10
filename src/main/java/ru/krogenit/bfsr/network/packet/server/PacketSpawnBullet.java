package ru.krogenit.bfsr.network.packet.server;

import lombok.NoArgsConstructor;
import org.joml.Vector2f;
import ru.krogenit.bfsr.core.Core;
import ru.krogenit.bfsr.entity.CollisionObject;
import ru.krogenit.bfsr.entity.bullet.Bullet;
import ru.krogenit.bfsr.entity.ship.Ship;
import ru.krogenit.bfsr.network.PacketBuffer;
import ru.krogenit.bfsr.network.client.NetworkManagerClient;
import ru.krogenit.bfsr.network.server.ServerPacket;
import ru.krogenit.bfsr.world.WorldClient;

import java.io.IOException;
import java.lang.reflect.Constructor;

@NoArgsConstructor
public class PacketSpawnBullet extends ServerPacket {

	private int id;
	private String className;
	private Vector2f pos;
	private float rot;
	private int shipId;
	
	public PacketSpawnBullet(Bullet bullet) {
		this.id = bullet.getId();
		this.className = bullet.getClass().getName();
		this.pos = bullet.getPosition();
		this.rot = bullet.getRotation();
		this.shipId = bullet.getOwnerShip().getId();
	}

	@Override
	public void read(PacketBuffer data) throws IOException {
		id = data.readInt();
		className = data.readStringFromBuffer(1024);
		pos = data.readVector2f();
		rot = data.readFloat();
		shipId = data.readInt();
	}

	@Override
	public void write(PacketBuffer data) throws IOException {
		data.writeInt(id);
		data.writeStringToBuffer(className);
		data.writeVector2f(pos);
		data.writeFloat(rot);
		data.writeInt(shipId);
	}

	@Override
	public void processOnClientSide(NetworkManagerClient networkManager) {
		if (Core.getCore().getWorld().getEntityById(id) == null) {
			try {
				WorldClient world = Core.getCore().getWorld();
				CollisionObject obj = world.getEntityById(shipId);
				if (obj != null) {
					Class<?> clazz = Class.forName(className);
					Constructor<?> ctr = clazz.getConstructor(WorldClient.class, int.class, float.class, Vector2f.class, Ship.class);
					ctr.newInstance(world, id, rot, pos, (Ship) obj);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}