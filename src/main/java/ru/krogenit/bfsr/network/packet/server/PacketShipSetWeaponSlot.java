package ru.krogenit.bfsr.network.packet.server;

import lombok.NoArgsConstructor;
import ru.krogenit.bfsr.component.weapon.WeaponSlot;
import ru.krogenit.bfsr.core.Core;
import ru.krogenit.bfsr.entity.CollisionObject;
import ru.krogenit.bfsr.entity.ship.Ship;
import ru.krogenit.bfsr.network.PacketBuffer;
import ru.krogenit.bfsr.network.client.NetworkManagerClient;
import ru.krogenit.bfsr.network.server.ServerPacket;

import java.io.IOException;
import java.lang.reflect.Constructor;

@NoArgsConstructor
public class PacketShipSetWeaponSlot extends ServerPacket {

	private int id;
	private String slot;
	private int slotId;

	public PacketShipSetWeaponSlot(Ship ship, WeaponSlot slot) {
		this.id = ship.getId();
		this.slot = slot.getClass().getName();
		this.slotId = slot.getId();
	}

	@Override
	public void read(PacketBuffer data) throws IOException {
		id = data.readInt();
		slot = data.readStringFromBuffer(2048);
		slotId = data.readInt();
	}

	@Override
	public void write(PacketBuffer data) throws IOException {
		data.writeInt(id);
		data.writeStringToBuffer(slot);
		data.writeInt(slotId);
	}

	@Override
	public void processOnClientSide(NetworkManagerClient networkManager) {
		CollisionObject obj = Core.getCore().getWorld().getEntityById(id);
		if (obj instanceof Ship) {
			Ship ship = (Ship) obj;
			try {
				Class<?> clazz = Class.forName(slot);
				Constructor<?> ctr = clazz.getConstructor(Ship.class);
				WeaponSlot slot = (WeaponSlot) ctr.newInstance(ship);
				ship.addWeaponToSlot(slotId, slot);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
}