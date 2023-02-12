package net.bfsr.network.packet.client;

import lombok.NoArgsConstructor;
import net.bfsr.client.particle.ParticleSpawner;
import net.bfsr.component.weapon.small.WeaponGausSmall;
import net.bfsr.component.weapon.small.WeaponLaserSmall;
import net.bfsr.component.weapon.small.WeaponPlasmSmall;
import net.bfsr.entity.ship.PlayerServer;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.engi.ShipEngiSmall0;
import net.bfsr.entity.ship.human.ShipHumanSmall0;
import net.bfsr.entity.ship.saimon.ShipSaimonSmall0;
import net.bfsr.faction.Faction;
import net.bfsr.math.MathUtils;
import net.bfsr.network.PacketBuffer;
import net.bfsr.network.client.ClientPacket;
import net.bfsr.network.server.NetworkManagerServer;
import net.bfsr.server.EnumCommand;
import net.bfsr.server.MainServer;
import net.bfsr.world.WorldServer;
import org.dyn4j.geometry.Vector2;
import org.joml.Vector2f;

import java.io.IOException;
import java.util.Random;

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
        for (int i = 0; i < size; i++) {
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
                    case HUMAN:
                        ship = new ShipHumanSmall0(world, pos.x, pos.y, rand.nextFloat() * MathUtils.TWO_PI, true);
                        ship.init();
                        ship.addWeaponToSlot(0, new WeaponPlasmSmall(ship));
                        ship.addWeaponToSlot(1, new WeaponPlasmSmall(ship));

                        break;
                    case SAIMON:
                        ship = new ShipSaimonSmall0(world, pos.x, pos.y, rand.nextFloat() * MathUtils.TWO_PI, true);
                        ship.init();
                        ship.addWeaponToSlot(0, new WeaponLaserSmall(ship));
                        ship.addWeaponToSlot(1, new WeaponLaserSmall(ship));

                        break;
                    case ENGI:
                        ship = new ShipEngiSmall0(world, pos.x, pos.y, rand.nextFloat() * MathUtils.TWO_PI, true);
                        ship.init();
                        ship.addWeaponToSlot(0, new WeaponGausSmall(ship));
                        ship.addWeaponToSlot(1, new WeaponGausSmall(ship));

                        break;
                }

                ship.setFaction(fact);
                ship.setName("[BOT] " + ship.getFaction().toString());
                return;
            case SpawnParticle:
                pos = new Vector2f(Float.parseFloat(args[0]), Float.parseFloat(args[1]));
                Vector2 linearVelocity = player.getPlayerShip().getBody().getLinearVelocity();
                float rot = player.getPlayerShip().getRotation();
                ParticleSpawner.spawnShipWreck(player.getPlayerShip(), 0, pos.x, pos.y, rot, -rot * 30.0f + (float) linearVelocity.x * 0.8f,
                        -rot * 30.0f + (float) linearVelocity.y * 0.8f, 750.0f);
        }
    }
}