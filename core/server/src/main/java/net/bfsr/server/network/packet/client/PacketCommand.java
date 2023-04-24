package net.bfsr.server.network.packet.client;

import io.netty.buffer.ByteBuf;
import lombok.NoArgsConstructor;
import net.bfsr.command.Command;
import net.bfsr.faction.Faction;
import net.bfsr.math.MathUtils;
import net.bfsr.network.util.ByteBufUtils;
import net.bfsr.server.component.weapon.WeaponBuilder;
import net.bfsr.server.entity.ship.Ship;
import net.bfsr.server.entity.ship.ShipEngiSmall0;
import net.bfsr.server.entity.ship.ShipHumanSmall0;
import net.bfsr.server.entity.ship.ShipSaimonSmall0;
import net.bfsr.server.network.handler.PlayerNetworkHandler;
import net.bfsr.server.network.packet.PacketIn;
import net.bfsr.server.world.WorldServer;
import org.joml.Vector2f;

import java.io.IOException;
import java.util.Random;

@NoArgsConstructor
public class PacketCommand implements PacketIn {
    private int command;
    private String[] args;

    @Override
    public void read(ByteBuf data) throws IOException {
        command = data.readInt();
        int size = data.readInt();
        args = new String[size];
        for (int i = 0; i < size; i++) {
            args[i] = ByteBufUtils.readString(data);
        }
    }

    @Override
    public void processOnServerSide(PlayerNetworkHandler playerNetworkHandler) {
        Command cmd = Command.values()[command];
        WorldServer world = playerNetworkHandler.getWorld();
        Random rand = world.getRand();
        switch (cmd) {
            case SPAWN_SHIP:
                Vector2f pos = new Vector2f(Float.parseFloat(args[0]), Float.parseFloat(args[1]));

                Faction fact = Faction.values()[rand.nextInt(Faction.values().length)];
                Ship ship = null;
                switch (fact) {
                    case HUMAN:
                        ship = new ShipHumanSmall0();
                        ship.init(world);
                        ship.setSpawned();
                        ship.setPosition(pos.x, pos.y);
                        ship.setRotation(rand.nextFloat() * MathUtils.TWO_PI);
                        ship.addWeaponToSlot(0, WeaponBuilder.createGun("plasm_small"));
                        ship.addWeaponToSlot(1, WeaponBuilder.createGun("plasm_small"));

                        break;
                    case SAIMON:
                        ship = new ShipSaimonSmall0();
                        ship.init(world);
                        ship.setSpawned();
                        ship.setPosition(pos.x, pos.y);
                        ship.setRotation(rand.nextFloat() * MathUtils.TWO_PI);
                        ship.addWeaponToSlot(0, WeaponBuilder.createGun("laser_small"));
                        ship.addWeaponToSlot(1, WeaponBuilder.createGun("laser_small"));

                        break;
                    case ENGI:
                        ship = new ShipEngiSmall0();
                        ship.init(world);
                        ship.setSpawned();
                        ship.setPosition(pos.x, pos.y);
                        ship.setRotation(rand.nextFloat() * MathUtils.TWO_PI);
                        ship.addWeaponToSlot(0, WeaponBuilder.createGun("gaus_small"));
                        ship.addWeaponToSlot(1, WeaponBuilder.createGun("gaus_small"));

                        break;
                }

                ship.setFaction(fact);
                ship.setName("[BOT] " + ship.getFaction().toString());
                ship.sendSpawnPacket();
        }
    }
}