package net.bfsr.server.ai;

import lombok.RequiredArgsConstructor;
import net.bfsr.engine.ai.Ai;
import net.bfsr.server.ai.task.AiAttackTarget;
import net.bfsr.server.ai.task.AiSearchTarget;
import net.bfsr.server.entity.EntityTrackingManager;

@RequiredArgsConstructor
public class AiFactory {
    private final EntityTrackingManager entityTrackingManager;

    public Ai createAi() {
        Ai ai = new Ai();
        ai.addTask(new AiSearchTarget(AiAggressiveType.ATTACK, 4000.0f));
        ai.addTask(new AiAttackTarget(4000.0f, entityTrackingManager));
        return ai;
    }
}