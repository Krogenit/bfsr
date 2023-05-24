package net.bfsr.network.packet.server.entity.ship;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.network.packet.PacketAdapter;

import java.io.IOException;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class PacketShipSetSpawned extends PacketAdapter {
    private int id;

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeInt(id);
    }

    @Override
    public void read(ByteBuf data) throws IOException {
        id = data.readInt();
    }
}