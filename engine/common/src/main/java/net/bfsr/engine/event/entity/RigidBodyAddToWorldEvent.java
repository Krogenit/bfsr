package net.bfsr.engine.event.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.bfsr.engine.event.Event;
import net.bfsr.engine.world.entity.RigidBody;

@AllArgsConstructor
@Getter
public class RigidBodyAddToWorldEvent extends Event {
    private final RigidBody rigidBody;
}