package net.bfsr.server.network.packet.client;

import io.netty.buffer.ByteBuf;
import lombok.NoArgsConstructor;
import net.bfsr.command.Command;
import net.bfsr.common.math.MathUtils;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.ShipFactory;
import net.bfsr.entity.ship.ShipOutfitter;
import net.bfsr.faction.Faction;
import net.bfsr.network.util.ByteBufUtils;
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
        if (cmd == Command.SPAWN_SHIP) {
            Vector2f pos = new Vector2f(Float.parseFloat(args[0]), Float.parseFloat(args[1]));
            Faction fact = Faction.values()[rand.nextInt(Faction.values().length)];
            Ship ship = switch (fact) {
                case HUMAN -> ShipFactory.get().createBotHumanSmall(world, pos.x, pos.y, rand.nextFloat() * MathUtils.TWO_PI);
                case SAIMON -> ShipFactory.get().createBotSaimonSmall(world, pos.x, pos.y, rand.nextFloat() * MathUtils.TWO_PI);
                case ENGI -> ShipFactory.get().createBotEngiSmall(world, pos.x, pos.y, rand.nextFloat() * MathUtils.TWO_PI);
            };
            ShipOutfitter.get().outfit(ship);
            world.addShip(ship);
        }
    }
}