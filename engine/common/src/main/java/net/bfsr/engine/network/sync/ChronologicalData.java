package net.bfsr.engine.network.sync;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public abstract class ChronologicalData<T extends ChronologicalData<T>> {
    protected int frame;

    public abstract void getInterpolated(T other, int frame, float interpolation, T destination);
}