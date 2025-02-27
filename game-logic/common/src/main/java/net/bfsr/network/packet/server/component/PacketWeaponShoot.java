package net.bfsr.network.packet.server.component;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.engine.network.packet.PacketScheduled;

import java.io.IOException;

@Getter
@NoArgsConstructor
public class PacketWeaponShoot extends PacketScheduled {
    private int id;
    private int slot;

    public PacketWeaponShoot(int id, int slot, double timestamp) {
        super(timestamp);
        this.id = id;
        this.slot = slot;
    }

    @Override
    public void write(ByteBuf data) throws IOException {
        super.write(data);
        data.writeInt(id);
        data.writeByte(slot);
    }

    @Override
    public void read(ByteBuf data) throws IOException {
        super.read(data);
        id = data.readInt();
        slot = data.readByte();
    }
}