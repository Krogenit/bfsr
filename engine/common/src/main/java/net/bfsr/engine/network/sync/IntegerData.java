package net.bfsr.engine.network.sync;

import lombok.Getter;

@Getter
public class IntegerData extends ChronologicalData<IntegerData> {
    private int value;

    IntegerData(int id, int frame) {
        super(frame);
        this.value = id;
    }

    public void offset(int offset) {
        value += offset;
    }

    @Override
    public void getInterpolated(IntegerData other, int frame, float interpolation, IntegerData destination) {
        destination.value = Math.round(value + (other.value - value) * interpolation);
        destination.frame = frame;
    }

    @Override
    public String toString() {
        return "frame: " + frame + ", value: " + value;
    }
}
