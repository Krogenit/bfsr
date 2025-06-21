package net.bfsr.engine.network.sync;

public class IntegerSync {
    private final DataHistory<IntegerData> localDataHistory;
    private final DataHistory<IntegerData> remoteDataHistory;

    public IntegerSync(double historyLengthMillis) {
        localDataHistory = new DataHistory<>(historyLengthMillis);
        remoteDataHistory = new DataHistory<>(historyLengthMillis);
    }

    public void addLocalData(int value, double timestamp) {
        localDataHistory.addData(new IntegerData(value, timestamp));
    }

    public void addRemoteData(int value, double timestamp) {
        remoteDataHistory.addData(new IntegerData(value, timestamp));
    }

    public int correction() {
        IntegerData remoteData = remoteDataHistory.getAndRemoveFirst();
        if (remoteData == null) {
            return 0;
        }

        IntegerData localData = localDataHistory.get(remoteData.getTime());
        if (localData == null) {
            return 0;
        }

        int localValue = localData.getValue();
        int remoteValue = remoteData.getValue();
        if (localValue == remoteValue) {
            return 0;
        }

        int deltaId = remoteValue - localValue;
        localDataHistory.forEach(worldEntityDataId -> worldEntityDataId.offset(deltaId));
        return deltaId;
    }
}
