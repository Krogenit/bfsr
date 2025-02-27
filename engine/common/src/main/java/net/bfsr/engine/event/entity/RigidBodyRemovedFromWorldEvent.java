package net.bfsr.engine.event.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.bfsr.engine.entity.RigidBody;
import net.bfsr.engine.event.Event;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor
public class RigidBodyRemovedFromWorldEvent extends Event {
    private final RigidBody rigidBody;
}