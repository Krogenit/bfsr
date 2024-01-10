package net.bfsr.network.packet.server.entity.ship;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.entity.RigidBody;
import net.bfsr.network.packet.common.PacketScheduled;

import java.io.IOException;

@Getter
@NoArgsConstructor
public class PacketDestroyingShip extends PacketScheduled {
    private int id;

    public PacketDestroyingShip(RigidBody<?> obj, double timestamp) {
        super(timestamp);
        this.id = obj.getId();
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