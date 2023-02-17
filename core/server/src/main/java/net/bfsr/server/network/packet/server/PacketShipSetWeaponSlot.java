package net.bfsr.server.network.packet.server;

import lombok.NoArgsConstructor;
import net.bfsr.network.PacketBuffer;
import net.bfsr.network.PacketOut;
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
    public void write(PacketBuffer data) throws IOException {
        data.writeInt(id);
        data.writeStringToBuffer(slot);
        data.writeInt(slotId);
    }
}