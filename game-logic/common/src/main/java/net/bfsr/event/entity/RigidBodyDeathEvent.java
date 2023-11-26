package net.bfsr.event.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.bfsr.engine.event.Event;
import net.bfsr.entity.RigidBody;

@AllArgsConstructor
@Getter
public class RigidBodyDeathEvent extends Event {
    private final RigidBody<?> rigidBody;
}