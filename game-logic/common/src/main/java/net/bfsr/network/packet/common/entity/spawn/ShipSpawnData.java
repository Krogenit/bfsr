package net.bfsr.network.packet.common.entity.spawn;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.weapon.WeaponSlot;
import net.bfsr.network.util.ByteBufUtils;

import java.util.List;

@Getter
@NoArgsConstructor
public class ShipSpawnData extends RigidBodySpawnData {
    private Ship ship;
    private boolean isSpawned;
    private Slot[] slotList;
    private String name;
    private byte faction;

    @AllArgsConstructor
    @Getter
    public static class Slot {
        private final int type;
        private final int dataIndex;
        private final int id;
    }

    public ShipSpawnData(Ship ship) {
        super(ship);
        this.ship = ship;
    }

    @Override
    public void writeData(ByteBuf data) {
        super.writeData(data);
        data.writeBoolean(ship.isSpawned());

        List<WeaponSlot> weaponSlots = ship.getModules().getWeaponSlots();
        data.writeByte(weaponSlots.size());
        for (int i = 0; i < weaponSlots.size(); i++) {
            WeaponSlot weaponSlot = weaponSlots.get(i);
            data.writeByte(weaponSlot.getWeaponType().ordinal());
            data.writeShort(weaponSlot.getGunData().getId());
            data.writeInt(weaponSlot.getId());
        }

        ByteBufUtils.writeString(data, ship.getName());
        data.writeByte(ship.getFaction().ordinal());
    }

    @Override
    public void readData(ByteBuf data) {
        super.readData(data);
        isSpawned = data.readBoolean();

        byte slotsCount = data.readByte();
        slotList = new Slot[slotsCount];
        for (int i = 0; i < slotsCount; i++) {
            slotList[i] = new Slot(data.readByte(), data.readShort(), data.readInt());
        }

        name = ByteBufUtils.readString(data);
        faction = data.readByte();
    }

    @Override
    public EntityPacketSpawnType getType() {
        return EntityPacketSpawnType.SHIP;
    }
}