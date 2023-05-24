package net.bfsr.server.network.packet.handler.play.player;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.ShipFactory;
import net.bfsr.entity.ship.ShipOutfitter;
import net.bfsr.faction.Faction;
import net.bfsr.network.packet.PacketHandler;
import net.bfsr.network.packet.client.PacketFactionSelect;
import net.bfsr.server.network.handler.PlayerNetworkHandler;
import net.bfsr.server.player.Player;
import net.bfsr.server.world.WorldServer;

import java.net.InetSocketAddress;

public class PacketFactionSelectHandler extends PacketHandler<PacketFactionSelect, PlayerNetworkHandler> {
    @Override
    public void handle(PacketFactionSelect packet, PlayerNetworkHandler playerNetworkHandler, ChannelHandlerContext ctx, InetSocketAddress remoteAddress) {
        WorldServer world = playerNetworkHandler.getWorld();
        Faction faction = Faction.values()[packet.getFaction()];
        Ship playerShip = switch (faction) {
            case HUMAN -> ShipFactory.get().createPlayerShipHumanSmall(world, 0, 0, world.getRand().nextFloat() * MathUtils.TWO_PI);
            case SAIMON -> ShipFactory.get().createPlayerShipSaimonSmall(world, 0, 0, world.getRand().nextFloat() * MathUtils.TWO_PI);
            case ENGI -> ShipFactory.get().createPlayerShipEngiSmall(world, 0, 0, world.getRand().nextFloat() * MathUtils.TWO_PI);
        };

        ShipOutfitter.get().outfit(playerShip);
        Player player = playerNetworkHandler.getPlayer();
        playerShip.setOwner(player.getUsername());
        playerShip.setName(player.getUsername());
        world.addShip(playerShip);

        player.setFaction(faction);
        player.addShip(playerShip);
        player.setShip(playerShip);
    }
}