package net.bfsr.server.network.packet.server.entity;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.entity.RigidBody;
import net.bfsr.network.PacketOut;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketRemoveObject implements PacketOut {
    private int id;

    public PacketRemoveObject(RigidBody obj) {
        this.id = obj.getId();
    }

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeInt(id);
    }
}