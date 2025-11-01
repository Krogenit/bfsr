package net.bfsr.engine.world.entity;

import net.bfsr.engine.network.sync.DataHistory;
import net.bfsr.engine.util.ObjectPool;
import org.joml.Vector2f;

public class EntityVelocityHistory extends DataHistory<VelocityData> {
    private final ObjectPool<VelocityData> cache = new ObjectPool<>(VelocityData::new);

    public EntityVelocityHistory(int historyLengthFrames) {
        super(historyLengthFrames, new VelocityData());
    }

    public void addData(Vector2f velocity, float angularVelocity, int frame) {
        VelocityData velocityData = cache.get();
        velocityData.setVelocity(velocity.x, velocity.y);
        velocityData.setAngularVelocity(angularVelocity);
        velocityData.setFrame(frame);
        addData(velocityData);
    }

    @Override
    protected void removeOld(int frameOfEntryAdded) {
        int removeThreshold = frameOfEntryAdded - historyLengthFrames;
        while (dataList.size() > 100) {
            VelocityData velocityData = dataList.getLast();
            if (velocityData.getFrame() < removeThreshold) {
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
