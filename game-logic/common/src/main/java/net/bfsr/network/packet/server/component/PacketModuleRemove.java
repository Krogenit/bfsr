package net.bfsr.network.packet.server.component;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.engine.logic.GameLogic;
import net.bfsr.engine.network.packet.PacketAnnotation;
import net.bfsr.engine.network.packet.PacketScheduled;
import net.bfsr.entity.ship.module.ModuleType;
import net.bfsr.network.packet.PacketIdRegistry;

import java.io.IOException;

@Getter
@NoArgsConstructor
@PacketAnnotation(id = PacketIdRegistry.COMPONENT_REMOVE)
public class PacketModuleRemove extends PacketScheduled {
    private int entityId;
    private int id;
    private ModuleType type;

    public PacketModuleRemove(int entityId, int id, ModuleType type, double timestamp) {
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
    public void read(ByteBuf data, GameLogic gameLogic) throws IOException {
        super.read(data, gameLogic);
        entityId = data.readInt();
        id = data.readShort();
        type = ModuleType.get(data.readByte());
    }
}