package net.bfsr.network.packet.server;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.core.Core;
import net.bfsr.entity.CollisionObject;
import net.bfsr.entity.ship.Ship;
import net.bfsr.faction.Faction;
import net.bfsr.network.PacketBuffer;
import net.bfsr.network.client.NetworkManagerClient;
import net.bfsr.network.server.ServerPacket;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketShipFaction extends ServerPacket {

    private int id;
    private int faction;

    public PacketShipFaction(Ship ship) {
        this.id = ship.getId();
        this.faction = ship.getFaction().ordinal();
    }

    @Override
    public void read(PacketBuffer data) throws IOException {
        id = data.readInt();
        faction = data.readInt();
    }

    @Override
    public void write(PacketBuffer data) throws IOException {
        data.writeInt(id);
        data.writeInt(faction);
    }

    @Override
    public void processOnClientSide(NetworkManagerClient networkManager) {
        CollisionObject obj = Core.get().getWorld().getEntityById(id);
        if (obj instanceof Ship) {
            Ship ship = (Ship) obj;
            ship.setFaction(Faction.values()[faction]);
        }
    }
}