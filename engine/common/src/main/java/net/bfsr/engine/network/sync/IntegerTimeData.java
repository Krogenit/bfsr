package net.bfsr.engine.network.sync;

import lombok.Getter;

@Getter
public class IntegerTimeData extends ChronologicalTimeData {
    private int value;

    IntegerTimeData(int value, double time) {
        super(time);
        this.value = value;
    }

    public void offset(int offset) {
        value += offset;
    }

    @Override
    public String toString() {
        return "time: " + time / 1_000_000_000.0 + ", value: " + value;
    }
}