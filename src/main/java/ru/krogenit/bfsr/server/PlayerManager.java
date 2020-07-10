package ru.krogenit.bfsr.server;

import lombok.Getter;
import org.joml.Vector2f;
import ru.krogenit.bfsr.component.weapon.small.WeaponGausSmall;
import ru.krogenit.bfsr.component.weapon.small.WeaponLaserSmall;
import ru.krogenit.bfsr.component.weapon.small.WeaponPlasmSmall;
import ru.krogenit.bfsr.entity.ship.PlayerServer;
import ru.krogenit.bfsr.entity.ship.Ship;
import ru.krogenit.bfsr.entity.ship.engi.ShipEngiSmall0;
import ru.krogenit.bfsr.entity.ship.human.ShipHumanSmall0;
import ru.krogenit.bfsr.entity.ship.saimon.ShipSaimonSmall0;
import ru.krogenit.bfsr.faction.Faction;
import ru.krogenit.bfsr.math.RotationHelper;
import ru.krogenit.bfsr.world.WorldServer;

public class PlayerManager {

    @Getter private static PlayerManager instance;
    private final MainServer server;
    private final WorldServer world;

    public PlayerManager(MainServer server, WorldServer world) {
        this.server = server;
        this.world = world;
        instance = this;
    }

    public void respawnPlayer(PlayerServer player, float x, float y) {
        Vector2f pos = new Vector2f(x, y);
        Faction faction = player.getFaction();
        Ship playerShip = null;
        switch(faction) {
            case Human:
                playerShip = new ShipHumanSmall0(world, pos, world.getRand().nextFloat() * RotationHelper.TWOPI, false);
                playerShip.addWeaponToSlot(0, new WeaponPlasmSmall(playerShip));
                playerShip.addWeaponToSlot(1, new WeaponPlasmSmall(playerShip));
                break;
            case Saimon:
                playerShip = new ShipSaimonSmall0(world, pos, world.getRand().nextFloat() * RotationHelper.TWOPI, false);
                playerShip.addWeaponToSlot(0, new WeaponLaserSmall(playerShip));
                playerShip.addWeaponToSlot(1, new WeaponLaserSmall(playerShip));
                break;
            case Engi:
                playerShip = new ShipEngiSmall0(world, pos, world.getRand().nextFloat() * RotationHelper.TWOPI, false);
                playerShip.addWeaponToSlot(0, new WeaponGausSmall(playerShip));
                playerShip.addWeaponToSlot(1, new WeaponGausSmall(playerShip));
                break;
        }

        playerShip.setOwner(player);
        playerShip.setFaction(faction);
        playerShip.setName(player.getUserName());
        player.addShip(playerShip);
        player.setPlayerShip(playerShip);
//		server.getDataBase().saveUser(playerMP);
    }
}
