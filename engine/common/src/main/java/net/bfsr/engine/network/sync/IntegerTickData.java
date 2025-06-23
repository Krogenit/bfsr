package net.bfsr.engine.network.sync;

import lombok.Getter;

@Getter
public class IntegerTickData extends ChronologicalTickData {
    private int value;

    IntegerTickData(int id, int tick) {
        super(tick);
        this.value = id;
    }

    public void offset(int offset) {
        value += offset;
    }

    @Override
    public String toString() {
        return "tick: " + tick + ", value: " + value;
    }
}
