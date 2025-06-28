package net.bfsr.engine.world.entity;

import net.bfsr.engine.network.sync.DataHistory;
import net.bfsr.engine.util.ObjectPool;
import org.jetbrains.annotations.Nullable;

public class EntityPositionHistory extends DataHistory<TransformData> {
    private final ObjectPool<TransformData> cache = new ObjectPool<>(TransformData::new);

    public EntityPositionHistory(double historyLengthMillis) {
        super(historyLengthMillis, new TransformData());
    }

    public void addPositionData(float x, float y, float sin, float cos, int frame) {
        TransformData positionData = cache.get();
        positionData.setPosition(x, y);
        positionData.setSin(sin);
        positionData.setCos(cos);
        positionData.setFrame(frame);
        addData(positionData);
    }

    @Override
    public TransformData getInterpolated(int frame) {
        if (dataList.isEmpty()) {
            return null;
        }

        if (dataList.getFirst().getFrame() < frame) {
            return null;// Extrapolation
        } else if (dataList.getLast().getFrame() > frame) {
            return dataList.getLast();
        }

        return findInterpolated(frame);
    }

    @Override
    public TransformData get(int frame) {
        if (dataList.isEmpty()) {
            return null;
        }

        if (dataList.getFirst().getFrame() < frame) {
            return dataList.getFirst();
        }

        for (int i = 0, positionDataSize = dataList.size(); i < positionDataSize; i++) {
            TransformData transformData = dataList.get(i);
            if (transformData.getFrame() <= frame) {
                return transformData;
            }
        }

        return null;
    }

    @Override
    public @Nullable TransformData getFirst() {
        return dataList.isEmpty() ? null : dataList.get(0);
    }

    @Override
    protected void removeOld(int frameOfEntryAdded) {
        double thresh = frameOfEntryAdded - historyLengthFrames;
        while (dataList.size() > 100) {
            TransformData epd = dataList.getLast();
            if (epd.getFrame() < thresh) {
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