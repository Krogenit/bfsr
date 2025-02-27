package net.bfsr.engine.entity;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class EntityDataHistory<T extends ChronologicalEntityData> {
    final List<T> dataList = new ArrayList<>();
    final double historyLengthMillis;

    public EntityDataHistory(double historyLengthMillis) {
        this.historyLengthMillis = historyLengthMillis * 1_000_000;
    }

    public void addData(T data) {
        boolean added = false;
        for (int i = 0; i < dataList.size(); i++) {
            T epd = dataList.get(i);
            if (data.getTime() > epd.getTime()) {
                dataList.add(i, data);
                added = true;
                break;
            }
        }

        if (!added) {
            dataList.add(data);
        }

        removeOld(data.getTime());
    }

    protected void removeOld(double timeOfEntryAdded) {
        double thresh = timeOfEntryAdded - historyLengthMillis;
        while (dataList.size() > 100) {
            T epd = dataList.getLast();
            if (epd.getTime() < thresh) {
                dataList.removeLast();
            } else {
                break;
            }
        }
    }

    public T get(double time) {
        if (dataList.isEmpty()) {
            return null;
        }

        if (dataList.getFirst().getTime() < time) {
            return dataList.getFirst();
        } else if (dataList.getLast().getTime() > time) {
            return dataList.getLast();
        }

        T firstEPD = null;
        for (int i = 0, size = dataList.size(); i < size; i++) {
            T secondEPD = dataList.get(i);
            if (firstEPD != null) {
                if (firstEPD.getTime() >= time && secondEPD.getTime() <= time) {
                    return secondEPD;
                }
            }

            firstEPD = secondEPD;
        }

        return null;
    }

    public @Nullable T getFirst() {
        return dataList.isEmpty() ? null : dataList.getFirst();
    }

    public void clear() {
        dataList.clear();
    }
}