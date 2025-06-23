package net.bfsr.engine.network.sync;

import net.bfsr.engine.Engine;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class DataTickHistory<T extends ChronologicalTickData> {
    protected final List<T> dataList = new ArrayList<>();
    protected final int historyLengthTicks;

    public DataTickHistory(double historyLengthMillis) {
        this.historyLengthTicks = Engine.convertMillisecondsToTicks(historyLengthMillis);
    }

    public void addData(T data) {
        boolean added = false;
        for (int i = 0; i < dataList.size(); i++) {
            T epd = dataList.get(i);
            if (data.getTick() > epd.getTick()) {
                dataList.add(i, data);
                added = true;
                break;
            }
        }

        if (!added) {
            dataList.add(data);
        }

        removeOld(data.getTick());
    }

    public void forEach(Consumer<T> consumer) {
        for (int i = 0; i < dataList.size(); i++) {
            consumer.accept(dataList.get(i));
        }
    }

    protected void removeOld(int tickOfEntryAdded) {
        double thresh = tickOfEntryAdded - historyLengthTicks;
        while (dataList.size() > 0) {
            T epd = dataList.getLast();
            if (epd.getTick() < thresh) {
                dataList.removeLast();
            } else {
                break;
            }
        }
    }

    public T get(int tick) {
        if (dataList.isEmpty()) {
            return null;
        }

        if (dataList.getFirst().getTick() < tick) {
            return dataList.getFirst();
        } else if (dataList.getLast().getTick() > tick) {
            return dataList.getLast();
        }

        T firstEPD = null;
        for (int i = 0, size = dataList.size(); i < size; i++) {
            T secondEPD = dataList.get(i);
            if (firstEPD != null) {
                if (firstEPD.getTick() >= tick && secondEPD.getTick() < tick) {
                    return secondEPD;
                }
            }

            firstEPD = secondEPD;
        }

        return null;
    }

    public @Nullable T getFirst() {
        return dataList.isEmpty() ? null : dataList.get(0);
    }

    public @Nullable T getAndRemoveFirst() {
        return dataList.isEmpty() ? null : dataList.remove(0);
    }

    public void clear() {
        dataList.clear();
    }
}