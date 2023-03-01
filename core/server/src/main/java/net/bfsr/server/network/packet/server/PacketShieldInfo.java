package net.bfsr.server.network.packet.server;

import io.netty.buffer.ByteBuf;
import lombok.NoArgsConstructor;
import net.bfsr.network.PacketOut;
import net.bfsr.server.entity.ship.Ship;

import java.io.IOException;

@NoArgsConstructor
public class PacketShieldInfo implements PacketOut {
    private int id;
    private float shieldValue;

    public PacketShieldInfo(Ship ship) {
        this.id = ship.getId();
        this.shieldValue = ship.getShield().getShield();
    }

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeInt(id);
        data.writeFloat(shieldValue);
    }
}