package net.bfsr.server.network.packet.handler.play;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.command.Command;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.ShipFactory;
import net.bfsr.entity.ship.ShipOutfitter;
import net.bfsr.faction.Faction;
import net.bfsr.network.packet.PacketHandler;
import net.bfsr.network.packet.client.PacketCommand;
import net.bfsr.server.network.handler.PlayerNetworkHandler;
import net.bfsr.world.World;
import org.joml.Vector2f;

import java.net.InetSocketAddress;
import java.util.Random;

public class PacketCommandHandler extends PacketHandler<PacketCommand, PlayerNetworkHandler> {
    private final Command[] commands = Command.values();

    @Override
    public void handle(PacketCommand packet, PlayerNetworkHandler playerNetworkHandler, ChannelHandlerContext ctx, InetSocketAddress remoteAddress) {
        Command cmd = commands[packet.getCommand()];
        World world = playerNetworkHandler.getWorld();
        Random rand = world.getRand();
        if (cmd == Command.SPAWN_SHIP) {
            String[] args = packet.getArgs();
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