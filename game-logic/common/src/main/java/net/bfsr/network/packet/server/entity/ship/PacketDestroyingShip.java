package net.bfsr.network.packet.server.entity.ship;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.entity.RigidBody;
import net.bfsr.network.packet.PacketAdapter;

import java.io.IOException;

@NoArgsConstructor
@Getter
public class PacketDestroyingShip extends PacketAdapter {
    private int id;

    public PacketDestroyingShip(RigidBody obj) {
        this.id = obj.getId();
    }

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeInt(id);
    }

    @Override
    public void read(ByteBuf data) throws IOException {
        id = data.readInt();
    }
}