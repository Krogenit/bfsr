package net.bfsr.server.network.packet.server;

import lombok.NoArgsConstructor;
import net.bfsr.entity.ship.ShipCommon;
import net.bfsr.network.PacketBuffer;
import net.bfsr.network.PacketOut;

import java.io.IOException;

@NoArgsConstructor
public class PacketShipName implements PacketOut {
    private int id;
    private String name;

    public PacketShipName(ShipCommon ship) {
        this.id = ship.getId();
        this.name = ship.getName();
    }

    @Override
    public void write(PacketBuffer data) throws IOException {
        data.writeInt(id);
        data.writeStringToBuffer(name);
    }
}