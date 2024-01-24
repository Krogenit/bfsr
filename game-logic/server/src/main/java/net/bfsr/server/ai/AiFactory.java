package net.bfsr.server.ai;

import net.bfsr.ai.Ai;
import net.bfsr.ai.AiAggressiveType;
import net.bfsr.server.ai.task.AiAttackTarget;
import net.bfsr.server.ai.task.AiSearchTarget;

public final class AiFactory {
    public static Ai createAi() {
        Ai ai = new Ai();
        ai.setAggressiveType(AiAggressiveType.ATTACK);
        ai.addTask(new AiSearchTarget(4000.0f));
        ai.addTask(new AiAttackTarget(4000.0f));
        return ai;
    }
}