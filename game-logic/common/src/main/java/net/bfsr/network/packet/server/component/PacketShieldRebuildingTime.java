package net.bfsr.network.packet.server.component;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.engine.network.packet.PacketScheduled;

import java.io.IOException;

@Getter
@NoArgsConstructor
public class PacketShieldRebuildingTime extends PacketScheduled {
    private int id;
    private int time;

    public PacketShieldRebuildingTime(int id, int time, double timestamp) {
        super(timestamp);
        this.id = id;
        this.time = time;
    }

    @Override
    public void write(ByteBuf data) throws IOException {
        super.write(data);
        data.writeInt(id);
        data.writeInt(time);
    }

    @Override
    public void read(ByteBuf data) throws IOException {
        super.read(data);
        id = data.readInt();
        time = data.readInt();
    }
}