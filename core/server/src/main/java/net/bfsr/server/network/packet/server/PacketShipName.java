package net.bfsr.server.network.packet.server;

import io.netty.buffer.ByteBuf;
import lombok.NoArgsConstructor;
import net.bfsr.network.PacketOut;
import net.bfsr.network.util.ByteBufUtils;
import net.bfsr.server.entity.ship.Ship;

import java.io.IOException;

@NoArgsConstructor
public class PacketShipName implements PacketOut {
    private int id;
    private String name;

    public PacketShipName(Ship ship) {
        this.id = ship.getId();
        this.name = ship.getName();
    }

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeInt(id);
        ByteBufUtils.writeString(data, name);
    }
}