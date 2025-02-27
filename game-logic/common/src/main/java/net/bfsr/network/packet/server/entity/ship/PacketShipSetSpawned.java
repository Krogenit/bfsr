package net.bfsr.network.packet.server.entity.ship;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.engine.network.packet.PacketScheduled;

import java.io.IOException;

@Getter
@NoArgsConstructor
public class PacketShipSetSpawned extends PacketScheduled {
    private int id;

    public PacketShipSetSpawned(int id, double timestamp) {
        super(timestamp);
        this.id = id;
    }

    @Override
    public void write(ByteBuf data) throws IOException {
        super.write(data);
        data.writeInt(id);
    }

    @Override
    public void read(ByteBuf data) throws IOException {
        super.read(data);
        id = data.readInt();
    }
}