package net.bfsr.server.ai;

import lombok.RequiredArgsConstructor;
import net.bfsr.ai.Ai;
import net.bfsr.ai.AiAggressiveType;
import net.bfsr.server.ai.task.AiAttackTarget;
import net.bfsr.server.ai.task.AiSearchTarget;
import net.bfsr.server.entity.EntityTrackingManager;

@RequiredArgsConstructor
public class AiFactory {
    private final EntityTrackingManager entityTrackingManager;

    public Ai createAi() {
        Ai ai = new Ai();
        ai.setAggressiveType(AiAggressiveType.ATTACK);
        ai.addTask(new AiSearchTarget(4000.0f));
        ai.addTask(new AiAttackTarget(4000.0f, entityTrackingManager));
        return ai;
    }
}