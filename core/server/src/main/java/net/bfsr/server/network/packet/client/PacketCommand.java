package net.bfsr.server.network.packet.client;

import lombok.NoArgsConstructor;
import net.bfsr.command.Command;
import net.bfsr.entity.ship.ShipCommon;
import net.bfsr.faction.Faction;
import net.bfsr.math.MathUtils;
import net.bfsr.network.PacketBuffer;
import net.bfsr.server.component.weapon.WeaponGausSmall;
import net.bfsr.server.component.weapon.WeaponLaserSmall;
import net.bfsr.server.component.weapon.WeaponPlasmSmall;
import net.bfsr.server.entity.ship.ShipEngiSmall0;
import net.bfsr.server.entity.ship.ShipHumanSmall0;
import net.bfsr.server.entity.ship.ShipSaimonSmall0;
import net.bfsr.server.entity.wreck.WreckSpawner;
import net.bfsr.server.network.NetworkManagerServer;
import net.bfsr.server.network.PacketIn;
import net.bfsr.server.player.PlayerServer;
import net.bfsr.server.world.WorldServer;
import org.dyn4j.geometry.Vector2;
import org.joml.Vector2f;

import java.io.IOException;
import java.util.Random;

@NoArgsConstructor
public class PacketCommand implements PacketIn {
    private int command;
    private String[] args;

    public PacketCommand(Command command, String... args) {
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
    public void processOnServerSide(NetworkManagerServer networkManager) {
        Command cmd = Command.values()[command];
        WorldServer world = networkManager.getWorld();
        Random rand = world.getRand();
        switch (cmd) {
            case SPAWN_SHIP:
                Vector2f pos = new Vector2f(Float.parseFloat(args[0]), Float.parseFloat(args[1]));

                Faction fact = Faction.values()[rand.nextInt(Faction.values().length)];
                ShipCommon ship = null;
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
            case SPAWN_PARTICLE:
                PlayerServer player = networkManager.getPlayer();
                pos = new Vector2f(Float.parseFloat(args[0]), Float.parseFloat(args[1]));
                Vector2 linearVelocity = player.getPlayerShip().getBody().getLinearVelocity();
                float rot = player.getPlayerShip().getRotation();
                WreckSpawner.spawnShipWreck(player.getPlayerShip(), 0, pos.x, pos.y, rot, -rot * 30.0f + (float) linearVelocity.x * 0.8f,
                        -rot * 30.0f + (float) linearVelocity.y * 0.8f, 750.0f);
        }
    }
}