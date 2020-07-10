package ru.krogenit.bfsr.network.packet.common;

import lombok.NoArgsConstructor;
import org.joml.Vector2f;
import ru.krogenit.bfsr.core.Core;
import ru.krogenit.bfsr.entity.CollisionObject;
import ru.krogenit.bfsr.entity.ship.PlayerServer;
import ru.krogenit.bfsr.entity.ship.Ship;
import ru.krogenit.bfsr.network.Packet;
import ru.krogenit.bfsr.network.PacketBuffer;
import ru.krogenit.bfsr.network.client.NetworkManagerClient;
import ru.krogenit.bfsr.network.packet.client.PacketNeedObjectInfo;
import ru.krogenit.bfsr.network.server.NetworkManagerServer;
import ru.krogenit.bfsr.server.MainServer;
import ru.krogenit.bfsr.world.WorldServer;

import java.io.IOException;

@NoArgsConstructor
public class PacketObjectPosition extends Packet {
	private int id;
	private Vector2f pos;
	private float rot;
	private Vector2f velocity;
	private float angularVelocity;

	public PacketObjectPosition(CollisionObject obj) {
		this.id = obj.getId();
		this.pos = obj.getPosition();
		this.rot = obj.getRotation();
		this.velocity = obj.getVelocity();
		this.angularVelocity = obj.getAngularVelocity();
	}

	public void read(PacketBuffer data) throws IOException {
		id = data.readInt();
		pos = data.readVector2f();
		rot = data.readFloat();
		velocity = data.readVector2f();
		angularVelocity = data.readFloat();
	}

	public void write(PacketBuffer data) throws IOException {
		data.writeInt(id);
		data.writeVector2f(pos);
		data.writeFloat(rot);
		data.writeVector2f(velocity);
		data.writeFloat(angularVelocity);
	}

	@Override
	public void processOnClientSide(NetworkManagerClient networkManager) {
		Core core = Core.getCore();
		CollisionObject obj = core.getWorld().getEntityById(id);
		if (obj != null) {
			obj.updateClientPositionFromPacket(pos, rot, velocity, angularVelocity);
		} else {
			core.sendPacket(new PacketNeedObjectInfo(id));
		}
	}

	@Override
	public void processOnServerSide(NetworkManagerServer networkManager, MainServer server, WorldServer world, PlayerServer player) {
		CollisionObject obj = world.getEntityById(id);
		if (obj != null) {
			Ship s = (Ship) obj;
			s.updateServerPositionFromPacket(pos, rot, velocity, angularVelocity);
		}
	}
}