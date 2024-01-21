package net.bfsr.entity;

import java.util.LinkedList;
import java.util.NoSuchElementException;

public class EntityDataHistory<T extends ChronologicalEntityData> {
    final LinkedList<T> dataList = new LinkedList<>();
    final double historyLengthMillis;

    EntityDataHistory(double historyLengthMillis) {
        this.historyLengthMillis = historyLengthMillis;
    }

    void addData(T data) {
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
        if (dataList.size() == 0) return null;

        if (dataList.getFirst().getTime() < time) {
            return null;
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

    public T getMostRecent() {
        try {
            return dataList.getFirst();
        } catch (NoSuchElementException e) {
            return null;
        }
    }
}