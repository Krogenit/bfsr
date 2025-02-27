package net.bfsr.network.packet.server.component;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.engine.network.packet.PacketScheduled;

import java.io.IOException;

@Getter
@NoArgsConstructor
public class PacketShieldRemove extends PacketScheduled {
    private int id;

    public PacketShieldRemove(int id, double timestamp) {
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