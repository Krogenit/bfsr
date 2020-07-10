package ru.krogenit.bfsr.network.packet.client;

import java.io.IOException;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.joml.Vector2f;
import ru.krogenit.bfsr.component.weapon.small.WeaponGausSmall;
import ru.krogenit.bfsr.component.weapon.small.WeaponLaserSmall;
import ru.krogenit.bfsr.component.weapon.small.WeaponPlasmSmall;
import ru.krogenit.bfsr.entity.ship.PlayerServer;
import ru.krogenit.bfsr.entity.ship.Ship;
import ru.krogenit.bfsr.entity.ship.engi.ShipEngiSmall0;
import ru.krogenit.bfsr.entity.ship.human.ShipHumanSmall0;
import ru.krogenit.bfsr.entity.ship.saimon.ShipSaimonSmall0;
import ru.krogenit.bfsr.faction.Faction;
import ru.krogenit.bfsr.math.RotationHelper;
import ru.krogenit.bfsr.network.PacketBuffer;
import ru.krogenit.bfsr.network.client.ClientPacket;
import ru.krogenit.bfsr.network.server.NetworkManagerServer;
import ru.krogenit.bfsr.server.MainServer;
import ru.krogenit.bfsr.world.WorldServer;

@NoArgsConstructor
public class PacketFactionSelect extends ClientPacket {

	private int faction;

	public PacketFactionSelect(Faction faction) {
		this.faction = faction.ordinal();
	}

	@Override
	public void read(PacketBuffer data) throws IOException {
		faction = data.readInt();
	}

	@Override
	public void write(PacketBuffer data) throws IOException {
		data.writeInt(faction);
	}

	@Override
	public void processOnServerSide(NetworkManagerServer networkManager, MainServer server, WorldServer world, PlayerServer player) {
		Faction faction = Faction.values()[this.faction];
		Ship playerShip = null;
		switch (faction) {
			case Human:
				playerShip = new ShipHumanSmall0(world, new Vector2f(), world.getRand().nextFloat() * RotationHelper.TWOPI, false);
				playerShip.addWeaponToSlot(0, new WeaponPlasmSmall(playerShip));
				playerShip.addWeaponToSlot(1, new WeaponPlasmSmall(playerShip));

				break;
			case Saimon:
				playerShip = new ShipSaimonSmall0(world, new Vector2f(), world.getRand().nextFloat() * RotationHelper.TWOPI, false);
				playerShip.addWeaponToSlot(0, new WeaponLaserSmall(playerShip));
				playerShip.addWeaponToSlot(1, new WeaponLaserSmall(playerShip));

				break;
			case Engi:
				playerShip = new ShipEngiSmall0(world, new Vector2f(), world.getRand().nextFloat() * RotationHelper.TWOPI, false);
				playerShip.addWeaponToSlot(0, new WeaponGausSmall(playerShip));
				playerShip.addWeaponToSlot(1, new WeaponGausSmall(playerShip));

				break;
		}

		playerShip.setOwner(player);
		playerShip.setFaction(faction);
		playerShip.setName(player.getUserName());
		player.setFaction(faction);
		player.addShip(playerShip);
		player.setPlayerShip(playerShip);
		server.getDataBase().saveUser(player);
	}
}