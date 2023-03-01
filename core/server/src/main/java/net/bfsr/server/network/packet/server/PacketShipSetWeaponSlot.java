package net.bfsr.server.network.packet.server;

import io.netty.buffer.ByteBuf;
import lombok.NoArgsConstructor;
import net.bfsr.network.PacketOut;
import net.bfsr.network.util.ByteBufUtils;
import net.bfsr.server.component.weapon.WeaponSlot;
import net.bfsr.server.entity.ship.Ship;

import java.io.IOException;

@NoArgsConstructor
public class PacketShipSetWeaponSlot implements PacketOut {
    private int id;
    private String slot;
    private int slotId;

    public PacketShipSetWeaponSlot(Ship ship, WeaponSlot slot) {
        this.id = ship.getId();
        this.slot = slot.getClass().getSimpleName();
        this.slotId = slot.getId();
    }

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeInt(id);
        ByteBufUtils.writeString(data, slot);
        data.writeInt(slotId);
    }
}