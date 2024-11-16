package net.bfsr.damage;

import lombok.RequiredArgsConstructor;
import net.bfsr.network.packet.common.entity.spawn.connectedobject.BeamSlotConnectedObjectSpawnData;
import net.bfsr.network.packet.common.entity.spawn.connectedobject.ConnectedObjectSpawnData;
import net.bfsr.network.packet.common.entity.spawn.connectedobject.WeaponSlotConnectObjectSpawnData;

import java.util.function.Supplier;

@RequiredArgsConstructor
public enum ConnectedObjectType {
    WEAPON_SLOT(WeaponSlotConnectObjectSpawnData::new),
    WEAPON_SLOT_BEAM(BeamSlotConnectedObjectSpawnData::new);

    private final Supplier<ConnectedObjectSpawnData> supplier;

    public ConnectedObjectSpawnData createInstance() {
        return supplier.get();
    }

    private static final ConnectedObjectType[] VALUES = values();

    public static ConnectedObjectType get(byte index) {
        return VALUES[index];
    }
}