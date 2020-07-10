package ru.krogenit.bfsr.network.packet.client;

import java.io.IOException;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import ru.krogenit.bfsr.client.particle.ParticleWreck;
import ru.krogenit.bfsr.component.weapon.WeaponSlot;
import ru.krogenit.bfsr.entity.CollisionObject;
import ru.krogenit.bfsr.entity.bullet.Bullet;
import ru.krogenit.bfsr.entity.ship.PlayerServer;
import ru.krogenit.bfsr.entity.ship.Ship;
import ru.krogenit.bfsr.math.Direction;
import ru.krogenit.bfsr.network.PacketBuffer;
import ru.krogenit.bfsr.network.client.ClientPacket;
import ru.krogenit.bfsr.network.packet.common.PacketShipEngine;
import ru.krogenit.bfsr.network.packet.server.*;
import ru.krogenit.bfsr.network.server.NetworkManagerServer;
import ru.krogenit.bfsr.server.MainServer;
import ru.krogenit.bfsr.world.WorldServer;

@AllArgsConstructor
@NoArgsConstructor
public class PacketNeedObjectInfo extends ClientPacket {
	private int objectId;

	@Override
	public void read(PacketBuffer data) throws IOException {
		objectId = data.readInt();
	}

	@Override
	public void write(PacketBuffer data) throws IOException {
		data.writeInt(objectId);
	}

	@Override
	public void processOnServerSide(NetworkManagerServer networkManager, MainServer server, WorldServer world, PlayerServer player) {
		CollisionObject obj = world.getEntityById(objectId);
		if (obj != null) {
			if (obj instanceof Ship) {
				Ship ship = (Ship) obj;
				networkManager.scheduleOutboundPacket(new PacketSpawnShip(ship));
				networkManager.scheduleOutboundPacket(new PacketShipName(ship));
				networkManager.scheduleOutboundPacket(new PacketShipFaction(ship));
				Direction dir = ship.getPrevMoveDir();
				if(dir != null) networkManager.scheduleOutboundPacket(new PacketShipEngine(objectId, dir.ordinal()));
				for(WeaponSlot slot : ship.getWeaponSlots()) {
					if(slot != null) networkManager.scheduleOutboundPacket(new PacketShipSetWeaponSlot(ship, slot));
				}

				if(ship.isControlledByPlayer() && ship.getOwner() == player) {
					networkManager.scheduleOutboundPacket(new PacketSetPlayerShip(ship.getId()));
				}
			} else if (obj instanceof Bullet) {
				Bullet b = (Bullet) obj;
				networkManager.scheduleOutboundPacket(new PacketSpawnBullet(b));
			} else if(obj instanceof ParticleWreck) {
				ParticleWreck p = (ParticleWreck) obj;
				networkManager.scheduleOutboundPacket(new PacketSpawnParticle(p));
			}
		}
	}
}