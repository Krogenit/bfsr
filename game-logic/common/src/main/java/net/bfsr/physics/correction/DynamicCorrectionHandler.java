package net.bfsr.physics.correction;

import net.bfsr.entity.RigidBody;

public class DynamicCorrectionHandler extends CorrectionHandler {
    private final float correctionChanging;
    private final CorrectionHandler nextCorrectionHandler;
    private final CorrectionHandler interpolatedCorrectionHandler;

    public DynamicCorrectionHandler(float correctionAmount, float correctionChanging, CorrectionHandler interpolatedCorrectionHandler,
                                    CorrectionHandler nextCorrectionHandler) {
        super(correctionAmount);
        this.interpolatedCorrectionHandler = interpolatedCorrectionHandler;
        this.nextCorrectionHandler = nextCorrectionHandler;
        this.correctionChanging = correctionChanging;
        this.interpolatedCorrectionHandler.correctionAmount = correctionAmount;
    }

    @Override
    public void update(double timestamp) {
        interpolatedCorrectionHandler.update(timestamp);
        correctionAmount += correctionChanging;
        if (correctionAmount >= 1.0f) {
            correctionAmount = 1.0f;
            rigidBody.setCorrectionHandler(nextCorrectionHandler);
        }

        interpolatedCorrectionHandler.correctionAmount = correctionAmount;
    }

    @Override
    public CorrectionHandler setRigidBody(RigidBody rigidBody) {
        nextCorrectionHandler.setRigidBody(rigidBody);
        interpolatedCorrectionHandler.setRigidBody(rigidBody);
        return super.setRigidBody(rigidBody);
    }

    public void setCorrectionAmount(float value) {
        correctionAmount = value;
    }
}
