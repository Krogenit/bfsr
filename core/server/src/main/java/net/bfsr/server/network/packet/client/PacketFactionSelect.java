package net.bfsr.server.network.packet.client;

import io.netty.buffer.ByteBuf;
import net.bfsr.faction.Faction;
import net.bfsr.math.MathUtils;
import net.bfsr.server.component.weapon.WeaponBuilder;
import net.bfsr.server.entity.ship.Ship;
import net.bfsr.server.entity.ship.ShipEngiSmall0;
import net.bfsr.server.entity.ship.ShipHumanSmall0;
import net.bfsr.server.entity.ship.ShipSaimonSmall0;
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
        Ship playerShip = null;
        switch (faction) {
            case HUMAN:
                playerShip = new ShipHumanSmall0();
                playerShip.init(world);
                playerShip.setRotation(world.getRand().nextFloat() * MathUtils.TWO_PI);
                playerShip.addWeaponToSlot(0, WeaponBuilder.createGun("plasm_small"));
                playerShip.addWeaponToSlot(1, WeaponBuilder.createGun("plasm_small"));
                break;
            case SAIMON:
                playerShip = new ShipSaimonSmall0();
                playerShip.init(world);
                playerShip.setRotation(world.getRand().nextFloat() * MathUtils.TWO_PI);
                playerShip.addWeaponToSlot(0, WeaponBuilder.createGun("laser_small"));
                playerShip.addWeaponToSlot(1, WeaponBuilder.createGun("laser_small"));
                break;
            case ENGI:
                playerShip = new ShipEngiSmall0();
                playerShip.init(world);
                playerShip.setRotation(world.getRand().nextFloat() * MathUtils.TWO_PI);
                playerShip.addWeaponToSlot(0, WeaponBuilder.createGun("gaus_small"));
                playerShip.addWeaponToSlot(1, WeaponBuilder.createGun("gaus_small"));
                break;
        }

        Player player = playerNetworkHandler.getPlayer();
        playerShip.setOwner(player);
        playerShip.setFaction(faction);
        playerShip.setName(player.getUsername());
        playerShip.sendSpawnPacket();
        player.setFaction(faction);
        player.addShip(playerShip);
        player.setPlayerShip(playerShip);
    }
}