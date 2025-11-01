package net.bfsr.client.network;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import net.bfsr.client.Client;
import net.bfsr.engine.Engine;

import java.util.concurrent.ConcurrentLinkedQueue;

@Log4j2
@RequiredArgsConstructor
public class TimeSyncManager {
    private final Client client;
    private final ConcurrentLinkedQueue<TimeData> dataList = new ConcurrentLinkedQueue<>();

    private final FramesErrorCounter framesErrorCounter = new FramesErrorCounter();

    @Getter
    @AllArgsConstructor
    public static class TimeData {
        private int frame;
        private double serverTime;
    }

    @Getter
    @Setter
    private static class FramesErrorCounter {
        private int errorCount;
        private int lastErrorFrame;
    }

    public void addData(TimeData timeData) {
        dataList.add(timeData);
    }

    public void update() {
        while (!dataList.isEmpty()) {
            int frame = client.getFrame();

            if (framesErrorCounter.errorCount > 0 && frame - framesErrorCounter.lastErrorFrame > Engine.convertSecondsToFrames(5)) {
                framesErrorCounter.errorCount--;
            }

            TimeData timeData = dataList.poll();
            int serverFrame = timeData.getFrame();
            double serverTime = timeData.getServerTime();
            int clientPrediction = client.getNetworkSystem().getAveragePingInFrames();
            if (frame < serverFrame || frame > serverFrame + clientPrediction) {
                framesErrorCounter.errorCount += 1;
                framesErrorCounter.lastErrorFrame = frame;

                if (framesErrorCounter.errorCount > 60) {
                    client.setTime(serverTime);
                    client.setFrame(serverFrame);
                    log.info("Adjust client time and frame, frame diff: {}", serverFrame - frame);
                }
            }
        }
    }
}
