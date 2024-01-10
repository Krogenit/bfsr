package net.bfsr.network.packet.server.player;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.network.packet.common.PacketScheduled;

import java.io.IOException;

@NoArgsConstructor
@Getter
public class PacketSetPlayerShip extends PacketScheduled {
    private int id;

    public PacketSetPlayerShip(int id, double timestamp) {
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