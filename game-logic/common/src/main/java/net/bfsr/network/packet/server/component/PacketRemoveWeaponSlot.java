package net.bfsr.network.packet.server.component;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.network.packet.PacketAdapter;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class PacketRemoveWeaponSlot extends PacketAdapter {
    private int shipId;
    private int slotId;

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeInt(shipId);
        data.writeByte(slotId);
    }

    @Override
    public void read(ByteBuf data) throws IOException {
        shipId = data.readInt();
        slotId = data.readByte();
    }
}