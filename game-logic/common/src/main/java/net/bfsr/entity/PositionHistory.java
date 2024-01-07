package net.bfsr.entity;

import net.bfsr.engine.util.ObjectPool;
import org.joml.Vector2f;

public final class PositionHistory extends ChronologicalEntityDataManager<TransformData> {
    private final ObjectPool<TransformData> cache = new ObjectPool<>(TransformData::new);

    public PositionHistory(double historyLengthMillis) {
        super(historyLengthMillis);
    }

    public void addPositionData(Vector2f pos, float sin, float cos, double time) {
        TransformData positionData = cache.get();
        positionData.setPosition(pos);
        positionData.setSin(sin);
        positionData.setCos(cos);
        positionData.setTime(time);
        addData(positionData);
    }

    @Override
    public TransformData get(double serverTimeToUse) {
        if (dataList.size() == 0) return null;

        if (dataList.getFirst().getTime() < serverTimeToUse) {
            return null;
        } else if (dataList.getLast().getTime() > serverTimeToUse) {
            return null;
        }

        TransformData firstEPD = null;
        for (int i = 0, positionDataSize = dataList.size(); i < positionDataSize; i++) {
            TransformData secondEPD = dataList.get(i);
            if (firstEPD != null) {
                if (firstEPD.getTime() >= serverTimeToUse && secondEPD.getTime() <= serverTimeToUse) {
                    return firstEPD.getInterpol(secondEPD, serverTimeToUse);
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
}