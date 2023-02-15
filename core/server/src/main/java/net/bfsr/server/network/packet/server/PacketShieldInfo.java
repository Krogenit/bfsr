package net.bfsr.server.network.packet.server;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.entity.ship.ShipCommon;
import net.bfsr.network.PacketBuffer;
import net.bfsr.network.PacketOut;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketShieldInfo implements PacketOut {
    private int id;
    private float shieldValue;

    public PacketShieldInfo(ShipCommon ship) {
        this.id = ship.getId();
        this.shieldValue = ship.getShield().getShield();
    }

    @Override
    public void write(PacketBuffer data) throws IOException {
        data.writeInt(id);
        data.writeFloat(shieldValue);
    }
}