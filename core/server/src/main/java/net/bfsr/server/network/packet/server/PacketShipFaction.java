package net.bfsr.server.network.packet.server;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.network.PacketBuffer;
import net.bfsr.network.PacketOut;
import net.bfsr.server.entity.ship.Ship;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketShipFaction implements PacketOut {
    private int id;
    private int faction;

    public PacketShipFaction(Ship ship) {
        this.id = ship.getId();
        this.faction = ship.getFaction().ordinal();
    }

    @Override
    public void write(PacketBuffer data) throws IOException {
        data.writeInt(id);
        data.writeInt(faction);
    }
}