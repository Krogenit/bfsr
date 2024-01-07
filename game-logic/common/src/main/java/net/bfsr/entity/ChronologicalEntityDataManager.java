package net.bfsr.entity;

import java.util.LinkedList;

public class ChronologicalEntityDataManager<T extends ChronologicalEntityData> {
    protected final LinkedList<T> dataList = new LinkedList<>();
    protected final double historyLengthMillis;

    public ChronologicalEntityDataManager(double historyLengthMillis) {
        this.historyLengthMillis = historyLengthMillis;
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

    public T get(double serverTimeToUse) {
        if (dataList.size() == 0) return null;

        if (dataList.getFirst().getTime() < serverTimeToUse) {
            return null;
        } else if (dataList.getLast().getTime() > serverTimeToUse) {
            return dataList.getLast();
        }

        T firstEPD = null;
        for (int i = 0, size = dataList.size(); i < size; i++) {
            T secondEPD = dataList.get(i);
            if (firstEPD != null) {
                if (firstEPD.getTime() >= serverTimeToUse && secondEPD.getTime() <= serverTimeToUse) {
                    return secondEPD;
                }
            }

            firstEPD = secondEPD;
        }

        return null;
    }

    public T getMostRecent() {
        return dataList.getFirst();
    }
}