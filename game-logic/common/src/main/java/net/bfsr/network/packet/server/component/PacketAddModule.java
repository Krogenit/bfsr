package net.bfsr.network.packet.server.component;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.engine.network.packet.PacketScheduled;
import net.bfsr.entity.ship.module.Module;
import net.bfsr.entity.ship.module.ModuleType;

import java.io.IOException;

@Getter
@NoArgsConstructor
public class PacketAddModule extends PacketScheduled {
    private int entityId;
    private int id;
    private int dataId;
    private ModuleType type;

    public PacketAddModule(int entityId, Module module, double timestamp) {
        super(timestamp);
        this.entityId = entityId;
        this.id = module.getId();
        this.type = module.getType();
        this.dataId = module.getDataId();
    }

    @Override
    public void write(ByteBuf data) throws IOException {
        super.write(data);
        data.writeInt(entityId);
        data.writeShort(id);
        data.writeByte(type.ordinal());
        data.writeShort(dataId);
    }

    @Override
    public void read(ByteBuf data) throws IOException {
        super.read(data);
        entityId = data.readInt();
        id = data.readShort();
        type = ModuleType.get(data.readByte());
        dataId = data.readShort();
    }
}