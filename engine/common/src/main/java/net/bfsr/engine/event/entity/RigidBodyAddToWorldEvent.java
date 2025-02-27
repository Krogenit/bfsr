package net.bfsr.engine.event.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.bfsr.engine.entity.RigidBody;
import net.bfsr.engine.event.Event;

@AllArgsConstructor
@Getter
public class RigidBodyAddToWorldEvent extends Event {
    private final RigidBody rigidBody;
}