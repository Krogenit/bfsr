package net.bfsr.engine.network.sync;

import net.bfsr.engine.Engine;

import java.util.ArrayList;
import java.util.List;

public class IntegerTimeSync {
    private final DataTimeHistory<IntegerTimeData> localDataHistory;
    private final DataTimeHistory<IntegerTimeData> remoteDataHistory;
    private final boolean debug;

    public IntegerTimeSync(double historyLengthMillis, boolean debug) {
        localDataHistory = new DataTimeHistory<>(historyLengthMillis);
        remoteDataHistory = new DataTimeHistory<>(historyLengthMillis);
        this.debug = debug;
    }

    public IntegerTimeSync(double historyLengthMillis) {
        this(historyLengthMillis, false);
    }

    public void addLocalData(int value, double time) {
        IntegerTimeData data = new IntegerTimeData(value, time);
        localDataHistory.addData(data);
    }

    public void addRemoteData(int value, double time) {
        IntegerTimeData data = new IntegerTimeData(value, time);
        remoteDataHistory.addData(data);
    }

    public int correction() {
        ArrayList<IntegerTimeData> remoteDataList = new ArrayList<>(remoteDataHistory.dataList);
        IntegerTimeData remoteData = remoteDataHistory.getAndRemoveFirst();
        if (remoteData == null) {
            if (debug) {
                System.out.println("Remote data history empty");
            }
            return 0;
        }

        IntegerTimeData localData = localDataHistory.get(remoteData.getTime());
        if (localData == null) {
            if (debug) {
                System.out.println("Can't find local data for " + remoteData);
            }
            return 0;
        }

        int localValue = localData.getValue();
        int remoteValue = remoteData.getValue();
        if (localValue == remoteValue) {
            return 0;
        }

        double timeDiffMillis = (remoteData.getTime() - localData.getTime()) / 1_000_000.0;
        int ticks = Engine.convertMillisecondsToTicks(timeDiffMillis);
        if (ticks > 0) {
//            System.out.println("Skip correction for time diff: " + ticks + "ticks, diff: " + (remoteValue - localValue));
            return 0;
        }

        if (debug) {
            System.out.println("Time diff " + timeDiffMillis + "ms, " + ticks + " ticks");
//            System.out.println("Removed remote data " + remoteData);
//            System.out.println("Find local data " + localData);

            for (int i = 0; i < remoteDataList.size(); i++) {
                IntegerTimeData integerTickData = remoteDataList.get(i);
//                System.out.println("Remote data " + integerTickData);
            }

            List<IntegerTimeData> dataList = localDataHistory.dataList;
            for (int i = 0; i < dataList.size(); i++) {
                IntegerTimeData integerTickData = dataList.get(i);
//                System.out.println("Local data " + integerTickData);
            }
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