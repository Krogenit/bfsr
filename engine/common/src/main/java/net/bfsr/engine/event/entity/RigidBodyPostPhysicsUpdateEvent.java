package net.bfsr.engine.event.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.bfsr.engine.entity.RigidBody;
import net.bfsr.engine.event.Event;

@Getter
@RequiredArgsConstructor
public class RigidBodyPostPhysicsUpdateEvent extends Event {
    private final RigidBody rigidBody;
}