package net.bfsr.client.network.packet.server;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.client.core.Core;
import net.bfsr.client.entity.ship.Ship;
import net.bfsr.client.network.packet.PacketIn;
import net.bfsr.entity.GameObject;
import net.bfsr.faction.Faction;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketShipFaction implements PacketIn {
    private static final Faction[] FACTIONS = Faction.values();

    private int id;
    private int faction;

    @Override
    public void read(ByteBuf data) throws IOException {
        id = data.readInt();
        faction = data.readInt();
    }

    @Override
    public void processOnClientSide() {
        GameObject obj = Core.get().getWorld().getEntityById(id);
        if (obj instanceof Ship ship) {
            ship.setFaction(FACTIONS[faction]);
        }
    }
}