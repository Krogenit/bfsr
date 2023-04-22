package net.bfsr.client.network.packet.server.entity.ship;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.bfsr.client.component.weapon.WeaponSlot;
import net.bfsr.client.component.weapon.WeaponSlotBeam;
import net.bfsr.client.core.Core;
import net.bfsr.client.entity.ship.Ship;
import net.bfsr.client.network.packet.PacketIn;
import net.bfsr.client.world.WorldClient;
import net.bfsr.component.weapon.WeaponType;
import net.bfsr.config.weapon.beam.BeamRegistry;
import net.bfsr.config.weapon.gun.GunRegistry;
import net.bfsr.faction.Faction;
import net.bfsr.network.util.ByteBufUtils;
import org.joml.Vector2f;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class PacketSpawnShip implements PacketIn {
    private static final Faction[] FACTIONS = Faction.values();
    private static final WeaponType[] WEAPON_TYPES = WeaponType.values();

    private int id;
    private String shipClassName;
    private Vector2f position;
    private float rot;
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
        rot = data.readFloat();
        shipClassName = ByteBufUtils.readString(data);
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
            try {
                Class<?> clazz = Class.forName("net.bfsr.client.entity.ship." + shipClassName);
                Constructor<?> ctr = clazz.getConstructor(WorldClient.class, int.class, float.class, float.class, float.class);
                Ship ship = (Ship) ctr.newInstance(world, id, position.x, position.y, rot);
                ship.init();

                if (isSpawned) ship.setSpawned();

                createWeaponSlots(ship);

                ship.setName(name);
                ship.setFaction(FACTIONS[faction]);
            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void createWeaponSlots(Ship ship) {
        for (int i = 0; i < slotList.length; i++) {
            Slot slot = slotList[i];

            WeaponSlot weaponSlot;
            if (WEAPON_TYPES[slot.getType()] == WeaponType.BEAM) {
                weaponSlot = new WeaponSlotBeam(ship, BeamRegistry.INSTANCE.get(slot.getDataIndex()));
            } else {
                weaponSlot = new WeaponSlot(ship, GunRegistry.INSTANCE.get(slot.getDataIndex()));
            }

            ship.addWeaponToSlot(slot.getId(), weaponSlot);
        }
    }
}