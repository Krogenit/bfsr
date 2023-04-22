package net.bfsr.server;

import lombok.Getter;
import net.bfsr.faction.Faction;
import net.bfsr.math.MathUtils;
import net.bfsr.server.component.weapon.WeaponBuilder;
import net.bfsr.server.entity.ship.Ship;
import net.bfsr.server.entity.ship.ShipEngiSmall0;
import net.bfsr.server.entity.ship.ShipHumanSmall0;
import net.bfsr.server.entity.ship.ShipSaimonSmall0;
import net.bfsr.server.player.Player;
import net.bfsr.server.world.WorldServer;

public class PlayerManager {
    @Getter
    private static PlayerManager instance;
    private final WorldServer world;

    public PlayerManager(WorldServer world) {
        this.world = world;
        instance = this;
    }

    public void respawnPlayer(Player player, float x, float y) {
        Faction faction = player.getFaction();
        Ship playerShip = null;
        switch (faction) {
            case HUMAN -> {
                playerShip = new ShipHumanSmall0();
                playerShip.init(world);
                playerShip.setPosition(x, y);
                playerShip.setRotation(world.getRand().nextFloat() * MathUtils.TWO_PI);
                playerShip.addWeaponToSlot(0, WeaponBuilder.createGun("plasm_small"));
                playerShip.addWeaponToSlot(1, WeaponBuilder.createGun("plasm_small"));
            }
            case SAIMON -> {
                playerShip = new ShipSaimonSmall0();
                playerShip.init(world);
                playerShip.setPosition(x, y);
                playerShip.setRotation(world.getRand().nextFloat() * MathUtils.TWO_PI);
                playerShip.addWeaponToSlot(0, WeaponBuilder.createGun("laser_small"));
                playerShip.addWeaponToSlot(1, WeaponBuilder.createGun("laser_small"));
            }
            case ENGI -> {
                playerShip = new ShipEngiSmall0();
                playerShip.init(world);
                playerShip.setPosition(x, y);
                playerShip.setRotation(world.getRand().nextFloat() * MathUtils.TWO_PI);
                playerShip.addWeaponToSlot(0, WeaponBuilder.createGun("gaus_small"));
                playerShip.addWeaponToSlot(1, WeaponBuilder.createGun("gaus_small"));
            }
        }

        playerShip.setOwner(player);
        playerShip.setFaction(faction);
        playerShip.setName(player.getUsername());
        playerShip.sendSpawnPacket();
        player.addShip(playerShip);
        player.setPlayerShip(playerShip);
    }
}