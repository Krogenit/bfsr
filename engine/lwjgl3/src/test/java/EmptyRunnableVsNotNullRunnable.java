import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * run empty runnable ~2 time faster
 */
public final class EmptyRunnableVsNotNullRunnable {
    @Getter
    @Setter
    private static final class TestObject {
        private Runnable runnable;

        public void run() {
            runnable.run();
        }

        public void check() {
            if (runnable != null) {
                runnable.run();
            }
        }
    }

    private static final Runnable EMPTY_RUNNABLE = () -> {};

    public static void main(String[] args) {
        List<TestObject> emptyRunnableObjects = new ArrayList<>();
        List<TestObject> checkRunnableObjects = new ArrayList<>();
        int count = 1_000_000;
        for (int i = 0; i < count; i++) {
            TestObject testObject = new TestObject();
            testObject.setRunnable(EMPTY_RUNNABLE);
            emptyRunnableObjects.add(testObject);
            testObject = new TestObject();
            checkRunnableObjects.add(testObject);
        }

        for (int k = 0; k < 100; k++) {
            PerformanceTest.beginTest();
            for (int i = 0; i < emptyRunnableObjects.size(); i++) {
                emptyRunnableObjects.get(i).run();
            }
            PerformanceTest.finishTest("emptyRunnable");

            PerformanceTest.beginTest();
            for (int i = 0; i < checkRunnableObjects.size(); i++) {
                checkRunnableObjects.get(i).check();
            }
            PerformanceTest.finishTest("checkRunnable");
        }
    }
}