package net.bfsr.engine.network.sync;

public class IntegerSync {
    private final DataHistory<IntegerData> localDataHistory;
    private final DataHistory<IntegerData> remoteDataHistory;

    public IntegerSync(int historyLengthFrames) {
        localDataHistory = new DataHistory<>(historyLengthFrames, new IntegerData(0, 0));
        remoteDataHistory = new DataHistory<>(historyLengthFrames, new IntegerData(0, 0));
    }

    public void addLocalData(int value, int frame) {
        localDataHistory.addData(new IntegerData(value, frame));
    }

    public void addRemoteData(int value, int frame) {
        remoteDataHistory.addData(new IntegerData(value, frame));
    }

    public int correction() {
        IntegerData remoteData = remoteDataHistory.getAndRemoveFirst();
        if (remoteData == null) {
            return 0;
        }

        IntegerData localData = localDataHistory.getInterpolated(remoteData.getFrame());
        if (localData == null) {
            return 0;
        }

        int localValue = localData.getValue();
        int remoteValue = remoteData.getValue();
        if (localValue == remoteValue) {
            return 0;
        }

        int deltaFrame = remoteData.getFrame() - localData.getFrame();
        if (deltaFrame != 0) {
            return 0;
        }

        int deltaId = remoteValue - localValue;
        localDataHistory.forEach(worldEntityDataId -> worldEntityDataId.offset(deltaId));
        return deltaId;
    }

    public void clear() {
        localDataHistory.clear();
        remoteDataHistory.clear();
    }
}
