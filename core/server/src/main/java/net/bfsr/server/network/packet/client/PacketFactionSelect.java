package net.bfsr.server.network.packet.client;

import lombok.NoArgsConstructor;
import net.bfsr.faction.Faction;
import net.bfsr.math.MathUtils;
import net.bfsr.network.PacketBuffer;
import net.bfsr.server.MainServer;
import net.bfsr.server.component.weapon.WeaponGausSmall;
import net.bfsr.server.component.weapon.WeaponLaserSmall;
import net.bfsr.server.component.weapon.WeaponPlasmSmall;
import net.bfsr.server.entity.Ship;
import net.bfsr.server.entity.ship.ShipEngiSmall0;
import net.bfsr.server.entity.ship.ShipHumanSmall0;
import net.bfsr.server.entity.ship.ShipSaimonSmall0;
import net.bfsr.server.network.NetworkManagerServer;
import net.bfsr.server.network.PacketIn;
import net.bfsr.server.player.PlayerServer;
import net.bfsr.server.world.WorldServer;

import java.io.IOException;

@NoArgsConstructor
public class PacketFactionSelect implements PacketIn {
    private int faction;

    public PacketFactionSelect(Faction faction) {
        this.faction = faction.ordinal();
    }

    @Override
    public void read(PacketBuffer data) throws IOException {
        faction = data.readInt();
    }

    @Override
    public void processOnServerSide(NetworkManagerServer networkManager) {
        WorldServer world = networkManager.getWorld();
        Faction faction = Faction.values()[this.faction];
        Ship playerShip = null;
        switch (faction) {
            case HUMAN:
                playerShip = new ShipHumanSmall0(world, 0, 0, world.getRand().nextFloat() * MathUtils.TWO_PI, false);
                playerShip.init();
                playerShip.addWeaponToSlot(0, new WeaponPlasmSmall(playerShip));
                playerShip.addWeaponToSlot(1, new WeaponPlasmSmall(playerShip));

                break;
            case SAIMON:
                playerShip = new ShipSaimonSmall0(world, 0, 0, world.getRand().nextFloat() * MathUtils.TWO_PI, false);
                playerShip.init();
                playerShip.addWeaponToSlot(0, new WeaponLaserSmall(playerShip));
                playerShip.addWeaponToSlot(1, new WeaponLaserSmall(playerShip));

                break;
            case ENGI:
                playerShip = new ShipEngiSmall0(world, 0, 0, world.getRand().nextFloat() * MathUtils.TWO_PI, false);
                playerShip.init();
                playerShip.addWeaponToSlot(0, new WeaponGausSmall(playerShip));
                playerShip.addWeaponToSlot(1, new WeaponGausSmall(playerShip));

                break;
        }

        PlayerServer player = networkManager.getPlayer();
        playerShip.setOwner(player);
        playerShip.setFaction(faction);
        playerShip.setName(player.getUserName());
        player.setFaction(faction);
        player.addShip(playerShip);
        player.setPlayerShip(playerShip);
        MainServer.getInstance().getDataBase().saveUser(player);
    }
}