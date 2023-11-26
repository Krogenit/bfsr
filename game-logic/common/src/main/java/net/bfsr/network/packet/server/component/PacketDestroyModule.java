package net.bfsr.network.packet.server.component;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.entity.ship.module.ModuleType;
import net.bfsr.network.packet.PacketAdapter;

import java.io.IOException;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PacketDestroyModule extends PacketAdapter {
    private static final ModuleType[] TYPES = ModuleType.values();

    private int entityId;
    private int id;
    private ModuleType type;

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeInt(entityId);
        data.writeShort(id);
        data.writeByte(type.ordinal());
    }

    @Override
    public void read(ByteBuf data) throws IOException {
        entityId = data.readInt();
        id = data.readShort();
        type = TYPES[data.readByte()];
    }
}