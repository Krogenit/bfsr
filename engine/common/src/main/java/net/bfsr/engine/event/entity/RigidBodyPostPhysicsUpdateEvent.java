package net.bfsr.engine.event.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.bfsr.engine.event.Event;
import net.bfsr.engine.world.entity.RigidBody;

@Getter
@RequiredArgsConstructor
public class RigidBodyPostPhysicsUpdateEvent extends Event {
    private final RigidBody rigidBody;
}