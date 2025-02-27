package net.bfsr.network.packet.server.component;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.engine.network.packet.PacketScheduled;

import java.io.IOException;

@NoArgsConstructor
@Getter
public class PacketRemoveWeaponSlot extends PacketScheduled {
    private int shipId;
    private int slotId;

    public PacketRemoveWeaponSlot(int shipId, int slotId, double timestamp) {
        super(timestamp);
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
    public void read(ByteBuf data) throws IOException {
        super.read(data);
        shipId = data.readInt();
        slotId = data.readByte();
    }
}