package net.bfsr.event.damage;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.bfsr.damage.Damageable;
import net.bfsr.engine.event.Event;

@RequiredArgsConstructor
@Getter()
public final class DamageEvent extends Event {
    private final Damageable damageable;
}