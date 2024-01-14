package net.bfsr.damage;

import lombok.RequiredArgsConstructor;
import net.bfsr.config.GameObjectConfigData;
import net.bfsr.config.component.weapon.beam.BeamData;
import net.bfsr.config.component.weapon.gun.GunData;
import net.bfsr.entity.ship.module.weapon.WeaponSlot;
import net.bfsr.entity.ship.module.weapon.WeaponSlotBeam;

import java.util.function.Function;

@RequiredArgsConstructor
public enum ConnectedObjectType {
    WEAPON_SLOT((configData) -> new WeaponSlot((GunData) configData)),
    WEAPON_SLOT_BEAM((configData -> new WeaponSlotBeam((BeamData) configData)));

    private final Function<GameObjectConfigData, ConnectedObject<?>> function;

    public ConnectedObject<?> createInstance(GameObjectConfigData configData) {
        return function.apply(configData);
    }

    private static final ConnectedObjectType[] VALUES = values();

    public static ConnectedObjectType get(byte index) {
        return VALUES[index];
    }
}