package net.bfsr.client.network.packet.server.entity.ship;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.bfsr.client.Core;
import net.bfsr.client.network.packet.PacketIn;
import net.bfsr.client.world.WorldClient;
import net.bfsr.component.weapon.WeaponSlot;
import net.bfsr.component.weapon.WeaponSlotBeam;
import net.bfsr.component.weapon.WeaponType;
import net.bfsr.config.component.weapon.beam.BeamRegistry;
import net.bfsr.config.component.weapon.gun.GunRegistry;
import net.bfsr.config.entity.ship.ShipRegistry;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.ShipFactory;
import net.bfsr.entity.ship.ShipOutfitter;
import net.bfsr.faction.Faction;
import net.bfsr.network.util.ByteBufUtils;
import org.joml.Vector2f;

import java.io.IOException;

public class PacketSpawnShip implements PacketIn {
    private static final Faction[] FACTIONS = Faction.values();
    private static final WeaponType[] WEAPON_TYPES = WeaponType.values();

    private int id;
    private int dataIndex;
    private Vector2f position;
    private float sin, cos;
    private boolean isSpawned;
    private Slot[] slotList;
    private String name;
    private byte faction;

    @AllArgsConstructor
    @Getter
    private static class Slot {
        private final int type;
        private final int dataIndex;
        private final int id;
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

    @Override
    public void processOnClientSide() {
        WorldClient world = Core.get().getWorld();
        if (world.getEntityById(id) == null) {
            Ship ship = ShipFactory.get().create(world, id, position.x, position.y, sin, cos, FACTIONS[faction], ShipRegistry.INSTANCE.get(dataIndex));
            ship.setName(name);
            ShipOutfitter.get().outfit(ship);
            createWeaponSlots(ship);
            world.addShip(ship);
            if (isSpawned) ship.setSpawned();
        }
    }

    private void createWeaponSlots(Ship ship) {
        for (int i = 0; i < slotList.length; i++) {
            Slot slot = slotList[i];

            WeaponSlot weaponSlot;
            if (WEAPON_TYPES[slot.getType()] == WeaponType.BEAM) {
                weaponSlot = new WeaponSlotBeam(BeamRegistry.INSTANCE.get(slot.getDataIndex()));
            } else {
                weaponSlot = new WeaponSlot(GunRegistry.INSTANCE.get(slot.getDataIndex()));
            }

            ship.addWeaponToSlot(slot.getId(), weaponSlot);
        }
    }
}