package net.bfsr.network.packet.server.component;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.engine.logic.GameLogic;
import net.bfsr.engine.network.packet.PacketAnnotation;
import net.bfsr.engine.network.packet.PacketScheduled;
import net.bfsr.network.packet.PacketIdRegistry;

import java.io.IOException;

@NoArgsConstructor
@Getter
@PacketAnnotation(id = PacketIdRegistry.WEAPON_SLOT_REMOVE)
public class PacketWeaponSlotRemove extends PacketScheduled {
    private int shipId;
    private int slotId;

    public PacketWeaponSlotRemove(int shipId, int slotId, int frame) {
        super(frame);
        this.shipId = shipId;
        this.slotId = slotId;
    }

    @Override
    public void write(ByteBuf data) throws IOException {
        super.write(data);
        data.writeInt(shipId);
        data.writeByte(slotId);
    }

    @Override
    public void read(ByteBuf data, GameLogic gameLogic) throws IOException {
        super.read(data, gameLogic);
        shipId = data.readInt();
        slotId = data.readByte();
    }
}