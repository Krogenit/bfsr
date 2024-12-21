package net.bfsr.server.network.packet.handler.play;

import io.netty.channel.ChannelHandlerContext;
import it.unimi.dsi.util.XoRoShiRo128PlusRandom;
import net.bfsr.ai.Ai;
import net.bfsr.command.Command;
import net.bfsr.config.entity.ship.ShipRegistry;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.ShipFactory;
import net.bfsr.entity.ship.ShipOutfitter;
import net.bfsr.faction.Faction;
import net.bfsr.network.packet.PacketHandler;
import net.bfsr.network.packet.client.PacketCommand;
import net.bfsr.server.ServerGameLogic;
import net.bfsr.server.network.handler.PlayerNetworkHandler;
import net.bfsr.world.World;
import org.joml.Vector2f;

import java.net.InetSocketAddress;

public class PacketCommandHandler extends PacketHandler<PacketCommand, PlayerNetworkHandler> {
    private final Command[] commands = Command.values();
    private final XoRoShiRo128PlusRandom random = new XoRoShiRo128PlusRandom();

    @Override
    public void handle(PacketCommand packet, PlayerNetworkHandler playerNetworkHandler, ChannelHandlerContext ctx,
                       InetSocketAddress remoteAddress) {
        Command cmd = commands[packet.getCommand()];
        World world = playerNetworkHandler.getWorld();
        if (cmd == Command.SPAWN_SHIP) {
            String[] args = packet.getArgs();
            Vector2f pos = new Vector2f(Float.parseFloat(args[0]), Float.parseFloat(args[1]));
            Faction fact = Faction.values()[random.nextInt(Faction.values().length)];
            Ai ai = Ai.NO_AI;

            ShipRegistry shipRegistry = ServerGameLogic.getInstance().getConfigConverterManager().getConverter(ShipRegistry.class);
            ShipFactory shipFactory = new ShipFactory(shipRegistry,
                    new ShipOutfitter(ServerGameLogic.getInstance().getConfigConverterManager()));

            Ship ship = switch (fact) {
                case HUMAN -> shipFactory.createBotHumanSmall(world, pos.x, pos.y, random.nextFloat() * MathUtils.TWO_PI, ai);
                case SAIMON -> shipFactory.createBotSaimonSmall(world, pos.x, pos.y, random.nextFloat() * MathUtils.TWO_PI, ai);
                case ENGI -> shipFactory.createBotEngiSmall(world, pos.x, pos.y, random.nextFloat() * MathUtils.TWO_PI, ai);
            };
            world.add(ship, false);
            ship.setSpawned();
        }
    }
}