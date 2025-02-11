package net.bfsr.server.network.packet.handler.play;

import io.netty.channel.ChannelHandlerContext;
import it.unimi.dsi.util.XoRoShiRo128PlusRandom;
import net.bfsr.ai.Ai;
import net.bfsr.command.Command;
import net.bfsr.config.entity.ship.ShipRegistry;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.entity.RigidBody;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.ShipFactory;
import net.bfsr.faction.Faction;
import net.bfsr.network.packet.PacketHandler;
import net.bfsr.network.packet.client.PacketCommand;
import net.bfsr.server.ServerGameLogic;
import net.bfsr.server.network.handler.PlayerNetworkHandler;
import net.bfsr.world.World;

import java.net.InetSocketAddress;

public class PacketCommandHandler extends PacketHandler<PacketCommand, PlayerNetworkHandler> {
    private final Command[] commands = Command.values();
    private final XoRoShiRo128PlusRandom random = new XoRoShiRo128PlusRandom();
    private final ShipFactory shipFactory = ServerGameLogic.get().getShipFactory();
    private final ShipRegistry shipRegistry = ServerGameLogic.get().getConfigConverterManager().getConverter(ShipRegistry.class);

    @Override
    public void handle(PacketCommand packet, PlayerNetworkHandler playerNetworkHandler, ChannelHandlerContext ctx,
                       InetSocketAddress remoteAddress) {
        Command cmd = commands[packet.getCommand()];
        World world = playerNetworkHandler.getWorld();
        if (cmd == Command.SPAWN_SHIP) {
            String[] args = packet.getArgs();
            int shipId = Integer.parseInt(args[0]);
            float x = Float.parseFloat(args[1]);
            float y = Float.parseFloat(args[2]);

            Faction faction = Faction.get((byte) random.nextInt(Faction.values().length));
            Ai ai = Ai.NO_AI;

            Ship ship = shipFactory.createBot(world, x, y, random.nextFloat() * MathUtils.TWO_PI, faction, shipRegistry.get(shipId), ai);
            world.add(ship, false);
            ship.setSpawned();
        } else if (cmd == Command.DESTROY_SHIP) {
            String[] args = packet.getArgs();
            int shipId = Integer.parseInt(args[0]);
            RigidBody rigidBody = world.getEntityById(shipId);
            if (rigidBody instanceof Ship ship) {
                ship.setDestroying();
            }
        }
    }
}