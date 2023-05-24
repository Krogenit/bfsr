package net.bfsr.client.listener;

import net.bfsr.client.listener.entity.bullet.BulletEventListener;
import net.bfsr.client.listener.entity.ship.ShipEventListener;
import net.bfsr.client.listener.entity.wreck.WreckEventListener;
import net.bfsr.client.listener.module.shield.ShieldEventListener;
import net.bfsr.client.listener.module.weapon.BeamEventListener;
import net.bfsr.client.listener.module.weapon.WeaponEventListener;
import net.bfsr.engine.util.Side;
import net.bfsr.event.EventBus;

public final class Listeners {
    public static void registerListeners() {
        EventBus.subscribe(Side.CLIENT, new ShipEventListener());
        EventBus.subscribe(Side.CLIENT, new ShieldEventListener());
        EventBus.subscribe(Side.CLIENT, new WeaponEventListener());
        EventBus.subscribe(Side.CLIENT, new BulletEventListener());
        EventBus.subscribe(Side.CLIENT, new WreckEventListener());
        EventBus.subscribe(Side.CLIENT, new BeamEventListener());
    }
}