package net.bfsr.network.packet.server.entity;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.entity.RigidBody;
import net.bfsr.network.packet.PacketAdapter;

import java.io.IOException;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class PacketRemoveObject extends PacketAdapter {
    private int id;

    public PacketRemoveObject(RigidBody obj) {
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