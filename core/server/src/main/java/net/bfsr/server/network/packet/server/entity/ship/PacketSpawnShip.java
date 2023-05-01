package net.bfsr.server.network.packet.server.entity.ship;

import io.netty.buffer.ByteBuf;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.bfsr.component.weapon.WeaponSlot;
import net.bfsr.entity.ship.Ship;
import net.bfsr.network.PacketOut;
import net.bfsr.network.util.ByteBufUtils;
import org.joml.Vector2f;

import java.io.IOException;
import java.util.List;

@NoArgsConstructor
@Log4j2
public class PacketSpawnShip implements PacketOut {
    private Vector2f position;
    private float sin, cos;
    private Ship ship;

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
}