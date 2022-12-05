package net.bfsr.math;

public class LUT {
    static final double PIO2_HI = Double.longBitsToDouble(0x3FF921FB54400000L);
    static final double PIO2_LO = Double.longBitsToDouble(0x3DD0B4611A626331L);
    static final double TWOPI_HI = 4 * PIO2_HI;
    static final double TWOPI_LO = 4 * PIO2_LO;
    static final int SIN_COS_TABS_SIZE = 2049;
    static final double SIN_COS_DELTA_HI = TWOPI_HI / (SIN_COS_TABS_SIZE - 1);
    static final double SIN_COS_DELTA_LO = TWOPI_LO / (SIN_COS_TABS_SIZE - 1);
    static final double SIN_COS_INDEXER = 1 / (SIN_COS_DELTA_HI + SIN_COS_DELTA_LO);
    static final float[] sinTab = new float[SIN_COS_TABS_SIZE];
    static final float[] cosTab = new float[SIN_COS_TABS_SIZE];
    static final int SIN_COS_TABLE_SIZE_WITH_OFFSET = SIN_COS_TABS_SIZE - 2;

    static {
        init();
    }

    private static void init() {
        final int SIN_COS_PI_INDEX = (SIN_COS_TABS_SIZE - 1) / 2;
        final int SIN_COS_PI_MUL_2_INDEX = 2 * SIN_COS_PI_INDEX;
        final int SIN_COS_PI_MUL_0_5_INDEX = SIN_COS_PI_INDEX / 2;
        final int SIN_COS_PI_MUL_1_5_INDEX = 3 * SIN_COS_PI_INDEX / 2;
        for (int i = 0; i < SIN_COS_TABS_SIZE; i++) {
            // angle: in [0,2*PI] (doesn't seem to help to have it in [-PI,PI]).
            double angle = i * SIN_COS_DELTA_HI + i * SIN_COS_DELTA_LO;
            float sinAngle = (float) StrictMath.sin(angle);
            float cosAngle = (float) StrictMath.cos(angle);
            // For indexes corresponding to zero cosine or sine, we make sure
            // the value is zero and not an epsilon, since each value
            // corresponds to sin-or-cos(i*PI/n), where PI is a more accurate
            // definition of PI than Math.PI.
            // This allows for a much better accuracy for results close to zero.
            if (i == SIN_COS_PI_INDEX) {
                sinAngle = 0.0f;
            } else if (i == SIN_COS_PI_MUL_2_INDEX) {
                sinAngle = 0.0f;
            } else if (i == SIN_COS_PI_MUL_0_5_INDEX) {
                cosAngle = 0.0f;
            } else if (i == SIN_COS_PI_MUL_1_5_INDEX) {
                cosAngle = 0.0f;
            }
            sinTab[i] = sinAngle;
            cosTab[i] = cosAngle;
        }
    }

    public static float sin(float angle) {
        return sinTab[((int) (angle * SIN_COS_INDEXER + 0.5)) & SIN_COS_TABLE_SIZE_WITH_OFFSET];
    }

    public static float cos(float angle) {
        return cosTab[((int) (angle * SIN_COS_INDEXER + 0.5)) & SIN_COS_TABLE_SIZE_WITH_OFFSET];
    }
}
