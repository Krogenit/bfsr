package net.bfsr.network.packet.client;

import lombok.NoArgsConstructor;
import net.bfsr.component.weapon.small.WeaponGausSmall;
import net.bfsr.component.weapon.small.WeaponLaserSmall;
import net.bfsr.component.weapon.small.WeaponPlasmSmall;
import net.bfsr.entity.ship.PlayerServer;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.engi.ShipEngiSmall0;
import net.bfsr.entity.ship.human.ShipHumanSmall0;
import net.bfsr.entity.ship.saimon.ShipSaimonSmall0;
import net.bfsr.faction.Faction;
import net.bfsr.math.RotationHelper;
import net.bfsr.network.PacketBuffer;
import net.bfsr.network.client.ClientPacket;
import net.bfsr.network.server.NetworkManagerServer;
import net.bfsr.server.MainServer;
import net.bfsr.world.WorldServer;
import org.joml.Vector2f;

import java.io.IOException;

@NoArgsConstructor
public class PacketFactionSelect extends ClientPacket {

    private int faction;

    public PacketFactionSelect(Faction faction) {
        this.faction = faction.ordinal();
    }

    @Override
    public void read(PacketBuffer data) throws IOException {
        faction = data.readInt();
    }

    @Override
    public void write(PacketBuffer data) throws IOException {
        data.writeInt(faction);
    }

    @Override
    public void processOnServerSide(NetworkManagerServer networkManager, MainServer server, WorldServer world, PlayerServer player) {
        Faction faction = Faction.values()[this.faction];
        Ship playerShip = null;
        switch (faction) {
            case Human:
                playerShip = new ShipHumanSmall0(world, new Vector2f(), world.getRand().nextFloat() * RotationHelper.TWOPI, false);
                playerShip.addWeaponToSlot(0, new WeaponPlasmSmall(playerShip));
                playerShip.addWeaponToSlot(1, new WeaponPlasmSmall(playerShip));

                break;
            case Saimon:
                playerShip = new ShipSaimonSmall0(world, new Vector2f(), world.getRand().nextFloat() * RotationHelper.TWOPI, false);
                playerShip.addWeaponToSlot(0, new WeaponLaserSmall(playerShip));
                playerShip.addWeaponToSlot(1, new WeaponLaserSmall(playerShip));

                break;
            case Engi:
                playerShip = new ShipEngiSmall0(world, new Vector2f(), world.getRand().nextFloat() * RotationHelper.TWOPI, false);
                playerShip.addWeaponToSlot(0, new WeaponGausSmall(playerShip));
                playerShip.addWeaponToSlot(1, new WeaponGausSmall(playerShip));

                break;
        }

        playerShip.setOwner(player);
        playerShip.setFaction(faction);
        playerShip.setName(player.getUserName());
        player.setFaction(faction);
        player.addShip(playerShip);
        player.setPlayerShip(playerShip);
        server.getDataBase().saveUser(player);
    }
}