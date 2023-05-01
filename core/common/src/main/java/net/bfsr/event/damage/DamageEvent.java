package net.bfsr.event.damage;

import net.bfsr.damage.Damageable;

public record DamageEvent(Damageable damageable) {}