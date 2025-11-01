package net.bfsr.server.ai;

import lombok.RequiredArgsConstructor;
import net.bfsr.engine.ai.Ai;
import net.bfsr.server.ai.task.AiAttackTarget;
import net.bfsr.server.ai.task.AiJumpTask;
import net.bfsr.server.ai.task.AiSearchTarget;
import net.bfsr.server.entity.EntityTrackingManager;
import org.joml.Vector2f;

@RequiredArgsConstructor
public class AiFactory {
    private final EntityTrackingManager entityTrackingManager;

    public Ai createAi() {
        Ai ai = new Ai();
        ai.addTask(new AiSearchTarget(AiAggressiveType.ATTACK, 400.0f));
        ai.addTask(new AiAttackTarget(400.0f, entityTrackingManager));
        return ai;
    }

    public Ai createJumpAi(float x, float y) {
        Ai ai = new Ai();
        ai.addTask(new AiJumpTask(new Vector2f(x, y)));
        return ai;
    }
}