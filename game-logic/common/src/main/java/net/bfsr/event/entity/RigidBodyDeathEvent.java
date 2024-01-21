package net.bfsr.event.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.bfsr.engine.event.Event;
import net.bfsr.entity.RigidBody;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor
public class RigidBodyDeathEvent extends Event {
    private final RigidBody<?> rigidBody;
}