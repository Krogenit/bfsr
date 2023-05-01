package net.bfsr.server.network.packet.client;

import io.netty.buffer.ByteBuf;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.ShipFactory;
import net.bfsr.entity.ship.ShipOutfitter;
import net.bfsr.faction.Faction;
import net.bfsr.math.MathUtils;
import net.bfsr.server.network.handler.PlayerNetworkHandler;
import net.bfsr.server.network.packet.PacketIn;
import net.bfsr.server.player.Player;
import net.bfsr.server.world.WorldServer;

import java.io.IOException;

public class PacketFactionSelect implements PacketIn {
    private int faction;

    @Override
    public void read(ByteBuf data) throws IOException {
        faction = data.readInt();
    }

    @Override
    public void processOnServerSide(PlayerNetworkHandler playerNetworkHandler) {
        WorldServer world = playerNetworkHandler.getWorld();
        Faction faction = Faction.values()[this.faction];
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