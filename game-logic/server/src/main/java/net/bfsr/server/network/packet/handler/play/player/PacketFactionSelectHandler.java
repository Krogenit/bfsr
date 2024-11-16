package net.bfsr.server.network.packet.handler.play.player;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.config.entity.ship.ShipRegistry;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.ShipFactory;
import net.bfsr.entity.ship.ShipOutfitter;
import net.bfsr.faction.Faction;
import net.bfsr.network.packet.PacketHandler;
import net.bfsr.network.packet.client.PacketFactionSelect;
import net.bfsr.server.ServerGameLogic;
import net.bfsr.server.network.handler.PlayerNetworkHandler;
import net.bfsr.server.player.Player;
import net.bfsr.world.World;

import java.net.InetSocketAddress;

public class PacketFactionSelectHandler extends PacketHandler<PacketFactionSelect, PlayerNetworkHandler> {
    @Override
    public void handle(PacketFactionSelect packet, PlayerNetworkHandler playerNetworkHandler, ChannelHandlerContext ctx,
                       InetSocketAddress remoteAddress) {
        World world = playerNetworkHandler.getWorld();
        Faction faction = Faction.values()[packet.getFaction()];

        ShipFactory shipFactory = new ShipFactory(ServerGameLogic.getInstance().getConfigConverterManager().getConverter(
                ShipRegistry.class), new ShipOutfitter(ServerGameLogic.getInstance().getConfigConverterManager()));

        Ship playerShip = switch (faction) {
            case HUMAN -> shipFactory.createPlayerShipHumanSmall(world, 0, 0,
                    world.getRand().nextFloat() * MathUtils.TWO_PI);
            case SAIMON -> shipFactory.createPlayerShipSaimonSmall(world, 0, 0,
                    world.getRand().nextFloat() * MathUtils.TWO_PI);
            case ENGI -> shipFactory.createPlayerShipEngiSmall(world, 0, 0, world.getRand().nextFloat() * MathUtils.TWO_PI);
        };

        shipFactory.getShipOutfitter().outfit(playerShip);
        Player player = playerNetworkHandler.getPlayer();
        playerShip.setOwner(player.getUsername());
        playerShip.setName(player.getUsername());
        world.add(playerShip, false);

        player.setFaction(faction);
        player.addShip(playerShip);
        player.setShip(playerShip);
    }
}