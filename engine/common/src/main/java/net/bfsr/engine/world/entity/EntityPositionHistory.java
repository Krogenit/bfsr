package net.bfsr.engine.world.entity;

import net.bfsr.engine.network.sync.DataHistory;
import net.bfsr.engine.util.ObjectPool;

public class EntityPositionHistory extends DataHistory<TransformData> {
    private final ObjectPool<TransformData> cache = new ObjectPool<>(TransformData::new);

    public EntityPositionHistory(int historyLengthFrames) {
        super(historyLengthFrames, new TransformData());
    }

    public void addData(float x, float y, float sin, float cos, int frame) {
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
        } else if (dataList.getLast().getFrame() > frame) {
            return dataList.getLast();
        }

        return find(frame);
    }

    @Override
    protected void onOldDataRemoved(TransformData data) {
        cache.returnBack(data);
    }

    @Override
    public void clear() {
        super.clear();
        cache.clear();
    }
}