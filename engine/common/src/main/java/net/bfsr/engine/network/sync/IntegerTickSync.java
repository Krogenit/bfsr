package net.bfsr.engine.network.sync;

public class IntegerTickSync {
    private final DataTickHistory<IntegerTickData> localDataHistory;
    private final DataTickHistory<IntegerTickData> remoteDataHistory;

    public IntegerTickSync(double historyLengthMillis) {
        localDataHistory = new DataTickHistory<>(historyLengthMillis);
        remoteDataHistory = new DataTickHistory<>(historyLengthMillis);
    }

    public void addLocalData(int value, int tick) {
        localDataHistory.addData(new IntegerTickData(value, tick));
    }

    public void addRemoteData(int value, int tick) {
        remoteDataHistory.addData(new IntegerTickData(value, tick));
    }

    public int correction() {
        return correction(false);
    }

    public int correction(boolean debug) {
        IntegerTickData remoteData = remoteDataHistory.getAndRemoveFirst();
        if (remoteData == null) {
            return 0;
        }

        IntegerTickData localData = localDataHistory.get(remoteData.getTick());
        if (localData == null) {
            return 0;
        }

        int localValue = localData.getValue();
        int remoteValue = remoteData.getValue();
        if (localValue == remoteValue) {
            return 0;
        }

        int deltaTick = remoteData.getTick() - localData.getTick();
        if (deltaTick > 0) {
            System.out.println("Skip correction for delta tick: " + deltaTick + "ticks, diff: " + (remoteValue - localValue));
            return 0;
        }

        if (debug) {
//            System.out.println("Removed remote data " + remoteData);
//            System.out.println("Find local data " + localData);
//
//            List<IntegerTickData> dataList = remoteDataHistory.dataList;
//            for (int i = 0; i < dataList.size(); i++) {
//                IntegerTickData integerTickData = dataList.get(i);
//                System.out.println("Remote data " + integerTickData);
//            }
//
//            dataList = localDataHistory.dataList;
//            for (int i = 0; i < dataList.size(); i++) {
//                IntegerTickData integerTickData = dataList.get(i);
//                System.out.println("Local data " + integerTickData);
//            }
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
