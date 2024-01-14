package net.bfsr.client.renderer.component;

import gnu.trove.map.TMap;
import gnu.trove.map.hash.THashMap;
import net.bfsr.entity.ship.module.weapon.WeaponSlot;
import net.bfsr.entity.ship.module.weapon.WeaponSlotBeam;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public final class WeaponRenderRegistry {
    private static final TMap<Class<? extends WeaponSlot>, Constructor<? extends WeaponSlotRender>> renderRegistry = new THashMap<>();

    public static void init() {
        try {
            renderRegistry.put(WeaponSlot.class, WeaponSlotRender.class.getConstructor(WeaponSlot.class));
            renderRegistry.put(WeaponSlotBeam.class, WeaponSlotBeamRender.class.getConstructor(WeaponSlotBeam.class));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static WeaponSlotRender<?> createRender(WeaponSlot weaponSlot) {
        try {
            return renderRegistry.get(weaponSlot.getClass()).newInstance(weaponSlot);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}