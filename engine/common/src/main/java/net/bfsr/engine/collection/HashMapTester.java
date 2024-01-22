package net.bfsr.engine.collection;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class HashMapTester {
    private long lastInfoTime = System.currentTimeMillis();
    private final List<Double> addTimes = new ArrayList<>();
    private final List<Double> getTimes = new ArrayList<>();
    private final List<Double> removeTimes = new ArrayList<>();

    public void addAddTime(long time) {
        addTime(addTimes, time);
    }

    public void addGetTime(long time) {
        addTime(getTimes, time);
    }

    public void addRemoveTime(long time) {
        addTime(removeTimes, time);
    }

    private void addTime(List<Double> times, long time) {
        if (times.size() > 1000) {
            times.remove(0);
        }

        times.add(time / 1_000_000.0);
    }

    public void update() {
        long now = System.currentTimeMillis();
        if (now - lastInfoTime >= 500) {
            lastInfoTime = now;

            float averageAddTime = 0;
            float averageGetTime = 0;
            float averageRemoveTime = 0;

            for (int i = 0; i < addTimes.size(); i++) {
                averageAddTime += addTimes.get(i);
            }

            for (int i = 0; i < getTimes.size(); i++) {
                averageGetTime += getTimes.get(i);
            }

            for (int i = 0; i < removeTimes.size(); i++) {
                averageRemoveTime += removeTimes.get(i);
            }

            averageAddTime /= addTimes.size();
            averageGetTime /= getTimes.size();
            averageRemoveTime /= removeTimes.size();

            DecimalFormat decimalFormat = new DecimalFormat("0.000000");
            System.out.println(Thread.currentThread().getName() + " " +
                    "averageAddTime: " + decimalFormat.format(averageAddTime) + "ms averageGetTime: " +
                    decimalFormat.format(averageGetTime) + "ms averageRemoveTime: " +
                    decimalFormat.format(averageRemoveTime) + "ms");
        }
    }
}