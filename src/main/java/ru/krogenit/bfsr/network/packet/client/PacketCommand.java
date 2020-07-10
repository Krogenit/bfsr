package ru.krogenit.bfsr.network.packet.client;

import java.io.IOException;
import java.util.Random;

import lombok.NoArgsConstructor;
import org.dyn4j.geometry.Vector2;
import org.joml.Vector2f;
import ru.krogenit.bfsr.client.particle.ParticleSpawner;
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
import ru.krogenit.bfsr.server.EnumCommand;
import ru.krogenit.bfsr.server.MainServer;
import ru.krogenit.bfsr.world.WorldServer;

@NoArgsConstructor
public class PacketCommand extends ClientPacket {

	private int command;
	private String[] args;

	public PacketCommand(EnumCommand command, String... args) {
		this.command = command.ordinal();
		this.args = args;
	}

	@Override
	public void read(PacketBuffer data) throws IOException {
		command = data.readInt();
		int size = data.readInt();
		args = new String[size];
		for(int i=0;i<size;i++) {
			args[i] = data.readStringFromBuffer(256);
		}
	}

	@Override
	public void write(PacketBuffer data) throws IOException {
		data.writeInt(command);
		data.writeInt(args.length);
		for (String arg : args) {
			data.writeStringToBuffer(arg);
		}
	}

	@Override
	public void processOnServerSide(NetworkManagerServer networkManager, MainServer server, WorldServer world, PlayerServer player) {
		EnumCommand cmd = EnumCommand.values()[command];
		Random rand = world.getRand();
		switch (cmd) {
			case SpawnShip:
				Vector2f pos = new Vector2f(Float.parseFloat(args[0]), Float.parseFloat(args[1]));

				Faction fact = Faction.values()[rand.nextInt(Faction.values().length)];
				Ship ship = null;
				switch (fact) {
					case Human:
						ship = new ShipHumanSmall0(world, pos, rand.nextFloat() * RotationHelper.TWOPI, true);
						ship.addWeaponToSlot(0, new WeaponPlasmSmall(ship));
						ship.addWeaponToSlot(1, new WeaponPlasmSmall(ship));

						break;
					case Saimon:
						ship = new ShipSaimonSmall0(world, pos, rand.nextFloat() * RotationHelper.TWOPI, true);
						ship.addWeaponToSlot(0, new WeaponLaserSmall(ship));
						ship.addWeaponToSlot(1, new WeaponLaserSmall(ship));

						break;
					case Engi:
						ship = new ShipEngiSmall0(world, pos, rand.nextFloat() * RotationHelper.TWOPI, true);
						ship.addWeaponToSlot(0, new WeaponGausSmall(ship));
						ship.addWeaponToSlot(1, new WeaponGausSmall(ship));

						break;
				}

				ship.setFaction(fact);
				ship.setName("[BOT] " + ship.getFaction().toString());
				return;
			case SpawnParticle:
				pos = new Vector2f(Float.parseFloat(args[0]), Float.parseFloat(args[1]));
				Vector2 bodyVelocity = new Vector2(player.getPlayerShip().getBody().getLinearVelocity());
				float rot = player.getPlayerShip().getRotation();

				Vector2 velocity = new Vector2(rot).negate().multiply(30f).add(new Vector2(bodyVelocity).multiply(0.8f));
				ParticleSpawner.spawnShipWreck(player.getPlayerShip(), 0, pos, rot, velocity, 0.02f, 2000f);
//			bodyVelocity.negate();
//			velocity = new Vector2(rot).multiply(30f).add(new Vector2(bodyVelocity).multiply(0.8f));
//			ParticleSystem.spawnShipWreck(playerMP.getPlayerShip(), 1, pos, rot, velocity, 0.002f, 3000f);
//			ParticleSystem.spawnDamageDerbis(1, pos,
//					RotationHelper.angleToVelocity(RotationHelper.TWOPI * rand.nextFloat(), 500f * rand.nextFloat() + 250f), 1f);
		}
	}
}