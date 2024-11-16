package net.bfsr.entity;

import net.bfsr.engine.util.ObjectPool;

public class PositionHistory extends EntityDataHistory<TransformData> {
    private final ObjectPool<TransformData> cache = new ObjectPool<>(TransformData::new);
    private final TransformData cachedTransformData = new TransformData();

    public PositionHistory(double historyLengthMillis) {
        super(historyLengthMillis);
    }

    public void addPositionData(float x, float y, float sin, float cos, double time) {
        TransformData positionData = cache.get();
        positionData.setPosition(x, y);
        positionData.setSin(sin);
        positionData.setCos(cos);
        positionData.setTime(time);
        addData(positionData);
    }

    @Override
    public TransformData get(double time) {
        if (dataList.size() == 0) return null;

        if (dataList.getFirst().getTime() < time) {
            return dataList.getFirst();
        } else if (dataList.getLast().getTime() > time) {
            return dataList.getLast();
        }

        TransformData firstEPD = null;
        for (int i = 0, positionDataSize = dataList.size(); i < positionDataSize; i++) {
            TransformData secondEPD = dataList.get(i);
            if (firstEPD != null) {
                if (firstEPD.getTime() >= time && secondEPD.getTime() <= time) {
                    firstEPD.getInterpol(secondEPD, time, cachedTransformData);
                    return cachedTransformData;
                }
            }

            firstEPD = secondEPD;
        }

        return null;
    }

    @Override
    protected void removeOld(double timeOfEntryAdded) {
        double thresh = timeOfEntryAdded - historyLengthMillis;
        while (dataList.size() > 100) {
            TransformData epd = dataList.getLast();
            if (epd.getTime() < thresh) {
                cache.returnBack(dataList.removeLast());
            } else {
                break;
            }
        }
    }

    public void clear() {
        dataList.clear();
        cache.clear();
    }

    public void correction(float dx, float dy) {
        for (int i = 0; i < dataList.size(); i++) {
            TransformData transformData = dataList.get(i);
            transformData.getPosition().add(dx, dy);
        }
    }
}