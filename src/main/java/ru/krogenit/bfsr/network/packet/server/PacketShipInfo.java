package ru.krogenit.bfsr.network.packet.server;

import java.io.IOException;

import lombok.NoArgsConstructor;
import ru.krogenit.bfsr.component.Armor;
import ru.krogenit.bfsr.component.ArmorPlate;
import ru.krogenit.bfsr.component.shield.Shield;
import ru.krogenit.bfsr.core.Core;
import ru.krogenit.bfsr.entity.CollisionObject;
import ru.krogenit.bfsr.entity.ship.Ship;
import ru.krogenit.bfsr.network.PacketBuffer;
import ru.krogenit.bfsr.network.client.NetworkManagerClient;
import ru.krogenit.bfsr.network.packet.client.PacketNeedObjectInfo;
import ru.krogenit.bfsr.network.server.ServerPacket;

@NoArgsConstructor
public class PacketShipInfo extends ServerPacket {

	private int id;
	private float[] armor;
	private int crew;
	private float hull;
	private float energy;
	private float shield;

	public PacketShipInfo(Ship ship) {
		this.id = ship.getId();

		Armor armor = ship.getArmor();
		ArmorPlate[] plates = armor.getArmorPlates();
		this.armor = new float[plates.length];
		for (int i = 0; i < plates.length; i++) {
			if (plates[i] != null) {
				this.armor[i] = plates[i].getArmor();
			}
		}

		this.crew = ship.getCrew() != null ? ship.getCrew().getCrewSize() : 0;
		this.hull = ship.getHull().getHull();
		this.energy = ship.getReactor().getEnergy();
		this.shield = ship.getShield() != null ? ship.getShield().getShield() : 0;
	}

	@Override
	public void read(PacketBuffer data) throws IOException {
		id = data.readInt();

		armor = new float[data.readInt()];
		for (int i = 0; i < armor.length; i++) {
			armor[i] = data.readFloat();
		}

		crew = data.readInt();
		hull = data.readFloat();
		energy = data.readFloat();
		shield = data.readFloat();
	}

	@Override
	public void write(PacketBuffer data) throws IOException {
		data.writeInt(id);

		data.writeInt(armor.length);
		for (float v : armor) {
			data.writeFloat(v);
		}

		data.writeInt(crew);
		data.writeFloat(hull);
		data.writeFloat(energy);
		data.writeFloat(shield);
	}

	@Override
	public void processOnClientSide(NetworkManagerClient networkManager) {
		CollisionObject obj = Core.getCore().getWorld().getEntityById(id);
		if (obj != null) {
			Ship ship = (Ship) obj;
			Armor shipArmor = ship.getArmor();
			ArmorPlate[] plates = shipArmor.getArmorPlates();
			for (int i = 0; i < plates.length; i++) {
				if (plates[i] != null) plates[i].setArmor(armor[i]);
			}
			ship.getCrew().setCrewSize(crew);
			ship.getReactor().setEnergy(energy);
			ship.getHull().setHull(hull);
			Shield shipShield = ship.getShield();
			if (shipShield != null) shipShield.setShield(shield);
		} else {
			Core.getCore().sendPacket(new PacketNeedObjectInfo(id));
		}
	}
}