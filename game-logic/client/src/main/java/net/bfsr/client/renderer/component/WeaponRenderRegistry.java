package net.bfsr.client.renderer.component;

import gnu.trove.map.TMap;
import gnu.trove.map.hash.THashMap;
import net.bfsr.entity.ship.module.weapon.WeaponSlot;
import net.bfsr.entity.ship.module.weapon.WeaponSlotBeam;

import java.util.function.BiFunction;

public final class WeaponRenderRegistry {
    private final TMap<Class<? extends WeaponSlot>, BiFunction<WeaponSlot, Float, ? extends WeaponSlotRender>> renderRegistry = new THashMap<>();

    public WeaponRenderRegistry() {
        renderRegistry.put(WeaponSlot.class, WeaponSlotRender::new);
        renderRegistry.put(WeaponSlotBeam.class, (weaponSlot, z) -> new WeaponSlotBeamRender((WeaponSlotBeam) weaponSlot, z));
    }

    public WeaponSlotRender createRender(WeaponSlot weaponSlot, float z) {
        return renderRegistry.get(weaponSlot.getClass()).apply(weaponSlot, z);
    }
}