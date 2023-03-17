package net.bfsr.server;

import lombok.Getter;
import net.bfsr.faction.Faction;
import net.bfsr.math.MathUtils;
import net.bfsr.server.component.weapon.WeaponGausSmall;
import net.bfsr.server.component.weapon.WeaponLaserSmall;
import net.bfsr.server.component.weapon.WeaponPlasmSmall;
import net.bfsr.server.entity.ship.Ship;
import net.bfsr.server.entity.ship.ShipEngiSmall0;
import net.bfsr.server.entity.ship.ShipHumanSmall0;
import net.bfsr.server.entity.ship.ShipSaimonSmall0;
import net.bfsr.server.player.PlayerServer;
import net.bfsr.server.world.WorldServer;

public class PlayerManager {
    @Getter
    private static PlayerManager instance;
    private final WorldServer world;

    public PlayerManager(WorldServer world) {
        this.world = world;
        instance = this;
    }

    public void respawnPlayer(PlayerServer player, float x, float y) {
        Faction faction = player.getFaction();
        Ship playerShip = null;
        switch (faction) {
            case HUMAN:
                playerShip = new ShipHumanSmall0(world, x, y, world.getRand().nextFloat() * MathUtils.TWO_PI, false);
                playerShip.init();
                playerShip.addWeaponToSlot(0, new WeaponPlasmSmall(playerShip));
                playerShip.addWeaponToSlot(1, new WeaponPlasmSmall(playerShip));
                break;
            case SAIMON:
                playerShip = new ShipSaimonSmall0(world, x, y, world.getRand().nextFloat() * MathUtils.TWO_PI, false);
                playerShip.init();
                playerShip.addWeaponToSlot(0, new WeaponLaserSmall(playerShip));
                playerShip.addWeaponToSlot(1, new WeaponLaserSmall(playerShip));
                break;
            case ENGI:
                playerShip = new ShipEngiSmall0(world, x, y, world.getRand().nextFloat() * MathUtils.TWO_PI, false);
                playerShip.init();
                playerShip.addWeaponToSlot(0, new WeaponGausSmall(playerShip));
                playerShip.addWeaponToSlot(1, new WeaponGausSmall(playerShip));
                break;
        }

        playerShip.setOwner(player);
        playerShip.setFaction(faction);
        playerShip.setName(player.getUserName());
        playerShip.sendSpawnPacket();
        player.addShip(playerShip);
        player.setPlayerShip(playerShip);
    }
}
