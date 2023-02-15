package net.bfsr.server.network.packet.server;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.entity.ship.ShipCommon;
import net.bfsr.network.PacketBuffer;
import net.bfsr.network.PacketOut;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketShipFaction implements PacketOut {
    private int id;
    private int faction;

    public PacketShipFaction(ShipCommon ship) {
        this.id = ship.getId();
        this.faction = ship.getFaction().ordinal();
    }

    @Override
    public void write(PacketBuffer data) throws IOException {
        data.writeInt(id);
        data.writeInt(faction);
    }
}