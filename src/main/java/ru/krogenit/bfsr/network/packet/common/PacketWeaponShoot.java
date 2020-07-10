package ru.krogenit.bfsr.network.packet.common;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import ru.krogenit.bfsr.component.weapon.WeaponSlot;
import ru.krogenit.bfsr.core.Core;
import ru.krogenit.bfsr.entity.CollisionObject;
import ru.krogenit.bfsr.entity.ship.PlayerServer;
import ru.krogenit.bfsr.entity.ship.Ship;
import ru.krogenit.bfsr.network.Packet;
import ru.krogenit.bfsr.network.PacketBuffer;
import ru.krogenit.bfsr.network.client.NetworkManagerClient;
import ru.krogenit.bfsr.network.server.NetworkManagerServer;
import ru.krogenit.bfsr.server.MainServer;
import ru.krogenit.bfsr.world.WorldServer;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketWeaponShoot extends Packet {

	private int id;
	private int slot;

	@Override
	public void read(PacketBuffer data) throws IOException {
		id = data.readInt();
		slot = data.readInt();
	}

	@Override
	public void write(PacketBuffer data) throws IOException {
		data.writeInt(id);
		data.writeInt(slot);
	}

	@Override
	public void processOnClientSide(NetworkManagerClient networkManager) {
		CollisionObject obj = Core.getCore().getWorld().getEntityById(id);
		if (obj instanceof Ship) {
			Ship ship = (Ship) obj;
			WeaponSlot weaponSlot = ship.getWeaponSlot(slot);
			weaponSlot.clientShoot();
		}
	}

	@Override
	public void processOnServerSide(NetworkManagerServer networkManager, MainServer server, WorldServer world, PlayerServer player) {
		CollisionObject obj = world.getEntityById(id);
		if (obj instanceof Ship) {
			Ship ship = (Ship) obj;
			WeaponSlot weaponSlot = ship.getWeaponSlot(slot);
			weaponSlot.shoot();
		}
	}
}