package net.bfsr.engine.network.sync;

import net.bfsr.engine.Engine;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class DataHistory<T extends ChronologicalData<T>> {
    protected final List<T> dataList = new ArrayList<>();
    protected final int historyLengthFrames;
    private final T cachedData;

    public DataHistory(double historyLengthMillis, T cachedData) {
        this.historyLengthFrames = Engine.convertMillisecondsToFrames(historyLengthMillis);
        this.cachedData = cachedData;
    }

    public void addData(T data) {
        boolean added = false;
        for (int i = 0; i < dataList.size(); i++) {
            T epd = dataList.get(i);
            if (data.getFrame() > epd.getFrame()) {
                dataList.add(i, data);
                added = true;
                break;
            }
        }

        if (!added) {
            dataList.add(data);
        }

        removeOld(data.getFrame());
    }

    public void forEach(Consumer<T> consumer) {
        for (int i = 0; i < dataList.size(); i++) {
            consumer.accept(dataList.get(i));
        }
    }

    protected void removeOld(int frameOfEntryAdded) {
        double thresh = frameOfEntryAdded - historyLengthFrames;
        while (dataList.size() > 0) {
            T epd = dataList.getLast();
            if (epd.getFrame() < thresh) {
                dataList.removeLast();
            } else {
                break;
            }
        }
    }

    public T get(int frame) {
        if (dataList.isEmpty()) {
            return null;
        }

        if (dataList.getFirst().getFrame() < frame) {
            return dataList.getFirst();
        } else if (dataList.getLast().getFrame() > frame) {
            return dataList.getLast();
        }

        return find(frame);
    }

    protected T find(int frame) {
        for (int i = 0, positionDataSize = dataList.size(); i < positionDataSize; i++) {
            T data = dataList.get(i);
            if (data.getFrame() <= frame) {
                return data;
            }
        }

        return null;
    }

    public T getInterpolated(int frame) {
        if (dataList.isEmpty()) {
            return null;
        }

        if (dataList.getFirst().getFrame() < frame) {
            return dataList.getFirst();
        } else if (dataList.getLast().getFrame() > frame) {
            return dataList.getLast();
        }

        return findInterpolated(frame);
    }

    protected T findInterpolated(int frame) {
        T firstData = null;
        for (int i = 0, positionDataSize = dataList.size(); i < positionDataSize; i++) {
            T secondData = dataList.get(i);
            if (secondData.getFrame() == frame) {
                return secondData;
            } else if (firstData != null) {
                if (firstData.getFrame() >= frame && secondData.getFrame() < frame) {
                    float interpolation = (firstData.getFrame() - frame) / (float) (firstData.getFrame() - secondData.getFrame());
                    firstData.getInterpolated(secondData, frame, interpolation, cachedData);
                    return cachedData;
                }
            }

            firstData = secondData;
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