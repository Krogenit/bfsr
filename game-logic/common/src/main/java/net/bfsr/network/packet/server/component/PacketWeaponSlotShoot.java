package net.bfsr.network.packet.server.component;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.engine.logic.GameLogic;
import net.bfsr.engine.network.packet.PacketAnnotation;
import net.bfsr.engine.network.packet.PacketScheduled;
import net.bfsr.network.packet.PacketIdRegistry;

import java.io.IOException;

@Getter
@NoArgsConstructor
@PacketAnnotation(id = PacketIdRegistry.WEAPON_SLOT_SHOOT)
public class PacketWeaponSlotShoot extends PacketScheduled {
    private int id;
    private int slot;

    public PacketWeaponSlotShoot(int id, int slot, int frame) {
        super(frame);
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
    public void read(ByteBuf data, GameLogic gameLogic) throws IOException {
        super.read(data, gameLogic);
        id = data.readInt();
        slot = data.readByte();
    }
}