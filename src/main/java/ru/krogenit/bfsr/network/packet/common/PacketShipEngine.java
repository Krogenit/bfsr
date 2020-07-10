package ru.krogenit.bfsr.network.packet.common;

import java.io.IOException;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.krogenit.bfsr.core.Core;
import ru.krogenit.bfsr.entity.CollisionObject;
import ru.krogenit.bfsr.entity.ship.PlayerServer;
import ru.krogenit.bfsr.entity.ship.Ship;
import ru.krogenit.bfsr.math.Direction;
import ru.krogenit.bfsr.network.Packet;
import ru.krogenit.bfsr.network.PacketBuffer;
import ru.krogenit.bfsr.network.client.NetworkManagerClient;
import ru.krogenit.bfsr.network.server.NetworkManagerServer;
import ru.krogenit.bfsr.server.MainServer;
import ru.krogenit.bfsr.world.WorldServer;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class PacketShipEngine extends Packet {

	private int id;
	private int dir;

	@Override
	public void read(PacketBuffer data) throws IOException {
		id = data.readInt();
		dir = data.readInt();
	}

	@Override
	public void write(PacketBuffer data) throws IOException {
		data.writeInt(id);
		data.writeInt(dir);
	}

	@Override
	public void processOnClientSide(NetworkManagerClient networkManager) {
		Direction direction = Direction.values()[dir];
		CollisionObject obj = Core.getCore().getWorld().getEntityById(id);
		if (obj != null) {
			((Ship) obj).setMoveDirection(direction);
		}
	}

	@Override
	public void processOnServerSide(NetworkManagerServer networkManager, MainServer server, WorldServer world, PlayerServer player) {
		server.getNetworkSystem().sendPacketToAllExcept(new PacketShipEngine(id, dir), player);
	}
}