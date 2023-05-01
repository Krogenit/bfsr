package net.bfsr.server.event.listener;

import net.bfsr.event.EventBus;
import net.bfsr.server.event.listener.damage.DamageEventListener;
import net.bfsr.server.event.listener.entity.bullet.BulletEventListener;
import net.bfsr.server.event.listener.entity.ship.ShipEventListener;
import net.bfsr.server.event.listener.entity.wreck.WreckEventListener;
import net.bfsr.server.event.listener.module.shield.ShieldEventListener;
import net.bfsr.server.event.listener.module.weapon.WeaponEventListener;
import net.bfsr.util.Side;

public final class Listeners {
    public static void init() {
        EventBus.register(Side.SERVER);
        EventBus.subscribe(Side.SERVER, new ShieldEventListener());
        EventBus.subscribe(Side.SERVER, new ShipEventListener());
        EventBus.subscribe(Side.SERVER, new WeaponEventListener());
        EventBus.subscribe(Side.SERVER, new BulletEventListener());
        EventBus.subscribe(Side.SERVER, new WreckEventListener());
        EventBus.subscribe(Side.SERVER, new DamageEventListener());
    }
}