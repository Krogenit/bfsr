package net.bfsr.server.network.packet.handler.play.player;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.engine.network.packet.PacketHandler;
import net.bfsr.faction.Faction;
import net.bfsr.network.packet.client.PacketFactionSelect;
import net.bfsr.server.ServerGameLogic;
import net.bfsr.server.entity.ship.ShipSpawner;
import net.bfsr.server.network.handler.PlayerNetworkHandler;
import net.bfsr.server.player.Player;

import java.net.InetSocketAddress;

public class PacketFactionSelectHandler extends PacketHandler<PacketFactionSelect, PlayerNetworkHandler> {
    private final ServerGameLogic gameLogic = ServerGameLogic.get();
    private final ShipSpawner shipSpawner = gameLogic.getShipSpawner();

    @Override
    public void handle(PacketFactionSelect packet, PlayerNetworkHandler playerNetworkHandler, ChannelHandlerContext ctx,
                       InetSocketAddress remoteAddress) {
        Player player = playerNetworkHandler.getPlayer();
        Faction faction = Faction.values()[packet.getFaction()];
        player.setFaction(faction);
        playerNetworkHandler.getPlayerManager().respawnPlayer(playerNetworkHandler.getWorld(), player, 0, 0, gameLogic.getFrame(),
                shipSpawner);
    }
}