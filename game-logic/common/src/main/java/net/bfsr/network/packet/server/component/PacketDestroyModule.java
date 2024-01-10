package net.bfsr.network.packet.server.component;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.entity.ship.module.ModuleType;
import net.bfsr.network.packet.common.PacketScheduled;

import java.io.IOException;

@Getter
@NoArgsConstructor
public class PacketDestroyModule extends PacketScheduled {
    private int entityId;
    private int id;
    private ModuleType type;

    public PacketDestroyModule(int entityId, int id, ModuleType type, double timestamp) {
        super(timestamp);
        this.entityId = entityId;
        this.id = id;
        this.type = type;
    }

    @Override
    public void write(ByteBuf data) throws IOException {
        super.write(data);
        data.writeInt(entityId);
        data.writeShort(id);
        data.writeByte(type.ordinal());
    }

    @Override
    public void read(ByteBuf data) throws IOException {
        super.read(data);
        entityId = data.readInt();
        id = data.readShort();
        type = ModuleType.get(data.readByte());
    }
}