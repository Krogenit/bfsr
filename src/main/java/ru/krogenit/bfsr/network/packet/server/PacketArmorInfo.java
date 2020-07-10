package ru.krogenit.bfsr.network.packet.server;

import lombok.NoArgsConstructor;
import ru.krogenit.bfsr.component.Armor;
import ru.krogenit.bfsr.component.ArmorPlate;
import ru.krogenit.bfsr.core.Core;
import ru.krogenit.bfsr.entity.CollisionObject;
import ru.krogenit.bfsr.entity.ship.Ship;
import ru.krogenit.bfsr.network.PacketBuffer;
import ru.krogenit.bfsr.network.client.NetworkManagerClient;
import ru.krogenit.bfsr.network.server.ServerPacket;
import ru.krogenit.bfsr.world.WorldClient;

import java.io.IOException;

@NoArgsConstructor
public class PacketArmorInfo extends ServerPacket {

	private int id;
	private float armorValue;
	private int armorPlateId;

	public PacketArmorInfo(Ship ship, ArmorPlate plate) {
		this.id = ship.getId();
		this.armorValue = plate.getArmor();
		this.armorPlateId = plate.getId();
	}

	@Override
	public void read(PacketBuffer data) throws IOException {
		id = data.readInt();
		armorValue = data.readFloat();
		armorPlateId = data.readInt();
	}

	@Override
	public void write(PacketBuffer data) throws IOException {
		data.writeInt(id);
		data.writeFloat(armorValue);
		data.writeInt(armorPlateId);
	}

	@Override
	public void processOnClientSide(NetworkManagerClient networkManager) {
		WorldClient world = Core.getCore().getWorld();
		CollisionObject obj = world.getEntityById(id);
		if (obj != null) {
			Ship ship = (Ship) obj;
			Armor armor = ship.getArmor();
			if(armor != null) {
				ArmorPlate plate = armor.getArmorPlate(armorPlateId);
				if(plate != null) {
					plate.setArmor(armorValue);
				}
			}
		}
	}
}