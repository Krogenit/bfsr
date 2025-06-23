package net.bfsr.engine.world.entity;

import net.bfsr.engine.network.sync.DataTickHistory;
import net.bfsr.engine.util.ObjectPool;
import org.jetbrains.annotations.Nullable;

public class EntityPositionHistory extends DataTickHistory<TransformData> {
    private final ObjectPool<TransformData> cache = new ObjectPool<>(TransformData::new);
    private final TransformData cachedTransformData = new TransformData();

    public EntityPositionHistory(double historyLengthMillis) {
        super(historyLengthMillis);
    }

    public void addPositionData(float x, float y, float sin, float cos, int tick) {
        TransformData positionData = cache.get();
        positionData.setPosition(x, y);
        positionData.setSin(sin);
        positionData.setCos(cos);
        positionData.setTick(tick);
        addData(positionData);
    }

    @Override
    public TransformData get(int tick) {
        if (dataList.isEmpty()) {
            return null;
        }

        if (dataList.getFirst().getTick() < tick) {
            return null;// Extrapolation
        } else if (dataList.getLast().getTick() > tick) {
            return dataList.getLast();
        }

        TransformData firstEPD = null;
        for (int i = 0, positionDataSize = dataList.size(); i < positionDataSize; i++) {
            TransformData secondEPD = dataList.get(i);
            if (firstEPD != null) {
                if (firstEPD.getTick() >= tick && secondEPD.getTick() < tick) {
                    firstEPD.getInterpol(secondEPD, tick, cachedTransformData);
                    return cachedTransformData;
                }
            }

            firstEPD = secondEPD;
        }

        return null;
    }

    public TransformData getNonInterpolated(int tick) {
        if (dataList.isEmpty()) {
            return null;
        }

        if (dataList.getFirst().getTick() < tick) {
            return dataList.getFirst();
        }

        for (int i = 1, positionDataSize = dataList.size(); i < positionDataSize; i++) {
            TransformData older = dataList.get(i);
            if (older.getTick() <= tick) {
                TransformData newer = dataList.get(i - 1);
                double olderTime = Math.abs(tick - older.getTick());
                double newerTime = Math.abs(tick - newer.getTick());
                if (newerTime <= olderTime) {
                    return older;
                } else {
                    return newer;
                }
            }
        }

        return null;
    }

    @Override
    public @Nullable TransformData getFirst() {
        return dataList.isEmpty() ? null : dataList.get(0);
    }

    @Override
    protected void removeOld(int tickOfEntryAdded) {
        double thresh = tickOfEntryAdded - historyLengthTicks;
        while (dataList.size() > 100) {
            TransformData epd = dataList.getLast();
            if (epd.getTick() < thresh) {
                cache.returnBack(dataList.removeLast());
            } else {
                break;
            }
        }
    }

    @Override
    public void clear() {
        dataList.clear();
        cache.clear();
    }
}