package net.bfsr.physics.correction;

import net.bfsr.entity.RigidBody;

public class DynamicCorrectionHandler extends CorrectionHandler {
    private final float correctionChanging;
    private final CorrectionHandler nextCorrectionHandler;

    public DynamicCorrectionHandler(float correctionAmount, float correctionChanging, CorrectionHandler nextCorrectionHandler) {
        super(correctionAmount);
        this.nextCorrectionHandler = nextCorrectionHandler;
        this.correctionChanging = correctionChanging;
    }

    @Override
    public void update(double timestamp) {
        super.update(timestamp);
        correctionAmount += correctionChanging;
        if (correctionAmount >= 1.0f) {
            rigidBody.setCorrectionHandler(nextCorrectionHandler);
        }
    }

    @Override
    public CorrectionHandler setRigidBody(RigidBody rigidBody) {
        nextCorrectionHandler.setRigidBody(rigidBody);
        return super.setRigidBody(rigidBody);
    }
}
