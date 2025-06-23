package net.bfsr.engine.event.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.bfsr.engine.event.Event;
import net.bfsr.engine.world.entity.RigidBody;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor
public class RigidBodyRemovedFromWorldEvent extends Event {
    private final RigidBody rigidBody;
    private final int tick;
}