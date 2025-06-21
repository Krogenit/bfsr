package net.bfsr.engine.network.sync;

import lombok.Getter;

@Getter
public class IntegerData extends ChronologicalData {
    private int value;

    IntegerData(int id, double time) {
        super(time);
        this.value = id;
    }

    public void offset(int offset) {
        value += offset;
    }
}
