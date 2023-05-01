package net.bfsr.event.entity.bullet;

import net.bfsr.entity.bullet.Bullet;
import net.bfsr.entity.ship.Ship;

public record BulletDamageShipHullEvent(Bullet bullet, Ship ship, float contactX, float contactY) {}