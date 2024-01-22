package collection;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

public final class HashMapBenchmarks {
    /**
     * getHPPCWormMap the fastest GET and REMOVE
     * putHPPCHashMap the fastest PUT
     * <p>
     * getHPPCWormMap           100000  avgt    2  0.434          ms/op
     * getFastUtilMap           100000  avgt    2  0.436          ms/op
     * getEclipseMap            100000  avgt    2  0.457          ms/op
     * getNettyMap              100000  avgt    2  0.462          ms/op
     * <p>
     * putHPPCHashMap           100000  avgt    2  0.806          ms/op
     * putHPPCWormHashMap       100000  avgt    2  0.851          ms/op
     * putFastUtilHashMap       100000  avgt    2  0.862          ms/op
     * putEclipseHashMap        100000  avgt    2  0.905          ms/op
     * <p>
     * removeHPPCWormMap     100000  avgt    2  0.107          ms/op
     * removeNettyMap        100000  avgt    2  0.109          ms/op
     * removeFastUtilMap     100000  avgt    2  0.126          ms/op
     * removeHPPCMap         100000  avgt    2  0.131          ms/op
     *
     * @param args
     * @throws RunnerException
     */
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(".*" + PutMapBenchmarks.class.getSimpleName() + ".*")
                .include(".*" + GetMapBenchmarks.class.getSimpleName() + ".*")
                .include(".*" + RemoveMapBenchmarks.class.getSimpleName() + ".*")
                .warmupIterations(0)
                .measurementIterations(2)
                .measurementTime(TimeValue.seconds(1))
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}