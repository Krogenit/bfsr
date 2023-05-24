package net.bfsr.network.packet.server.entity.ship;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.component.weapon.WeaponSlot;
import net.bfsr.entity.ship.Ship;
import net.bfsr.network.packet.PacketAdapter;
import net.bfsr.network.util.ByteBufUtils;
import org.joml.Vector2f;

import java.io.IOException;
import java.util.List;

@NoArgsConstructor
@Getter
public class PacketSpawnShip extends PacketAdapter {
    private int id;
    private int dataIndex;
    private Vector2f position;
    private float sin, cos;
    private boolean isSpawned;
    private Slot[] slotList;
    private String name;
    private byte faction;
    private Ship ship;

    @AllArgsConstructor
    @Getter
    public static class Slot {
        private final int type;
        private final int dataIndex;
        private final int id;
    }

    public PacketSpawnShip(Ship ship) {
        this.ship = ship;
        this.position = ship.getPosition();
        this.sin = ship.getSin();
        this.cos = ship.getCos();
    }

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeInt(ship.getId());
        ByteBufUtils.writeVector(data, position);
        data.writeFloat(sin);
        data.writeFloat(cos);
        data.writeShort(ship.getShipData().getDataIndex());
        data.writeBoolean(ship.isSpawned());

        List<WeaponSlot> weaponSlots = ship.getWeaponSlots();
        data.writeByte(weaponSlots.size());
        for (int i = 0; i < weaponSlots.size(); i++) {
            WeaponSlot weaponSlot = weaponSlots.get(i);
            data.writeByte(weaponSlot.getType().ordinal());
            data.writeShort(weaponSlot.getGunData().getDataIndex());
            data.writeInt(weaponSlot.getId());
        }

        ByteBufUtils.writeString(data, ship.getName());
        data.writeByte(ship.getFaction().ordinal());
    }

    @Override
    public void read(ByteBuf data) throws IOException {
        id = data.readInt();
        ByteBufUtils.readVector(data, position = new Vector2f());
        sin = data.readFloat();
        cos = data.readFloat();
        dataIndex = data.readShort();
        isSpawned = data.readBoolean();

        byte slotsCount = data.readByte();
        slotList = new Slot[slotsCount];
        for (int i = 0; i < slotsCount; i++) {
            slotList[i] = new Slot(data.readByte(), data.readShort(), data.readInt());
        }

        name = ByteBufUtils.readString(data);
        faction = data.readByte();
    }
}