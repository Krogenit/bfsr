package net.bfsr.collection;

import com.carrotsearch.hppc.IntObjectHashMap;
import com.carrotsearch.hppc.IntObjectMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.agrona.collections.Int2ObjectHashMap;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class PutMapBenchmarks {
    @Param({"100000"})
    int setSize;

    int[] addedKeys;
    Entity[] addedValues;

    @Setup(Level.Iteration)
    public void init() {
        addedKeys = new int[setSize];
        addedValues = new Entity[setSize];
        Random random = new Random(3487162487124L);

        for (int i = 0; i < setSize; i++) {
            addedKeys[i] = random.nextInt();
            addedValues[i] = new Entity();
        }
    }

    @Benchmark
    public Map<Integer, Entity> putJavaHashMap() {
        Map<Integer, Entity> map = new HashMap<>(setSize);
        for (int i = 0; i < setSize; i++) {
            map.put(addedKeys[i], addedValues[i]);
        }
        return map;
    }

    @Benchmark
    public TIntObjectMap<Entity> putTroveHashMap() {
        TIntObjectMap<Entity> map = new TIntObjectHashMap<>(setSize);
        for (int i = 0; i < setSize; i++) {
            map.put(addedKeys[i], addedValues[i]);
        }
        return map;
    }

    @Benchmark
    public Int2ObjectMap<Entity> putFastUtilHashMap() {
        Int2ObjectMap<Entity> map = new Int2ObjectOpenHashMap<>(setSize);
        for (int i = 0; i < setSize; i++) {
            map.put(addedKeys[i], addedValues[i]);
        }
        return map;
    }

    @Benchmark
    public io.netty.util.collection.IntObjectMap<Entity> putNettyMap() {
        io.netty.util.collection.IntObjectMap<Entity> map = new io.netty.util.collection.IntObjectHashMap<>(setSize);
        for (int i = 0; i < setSize; i++) {
            map.put(addedKeys[i], addedValues[i]);
        }
        return map;
    }

    @Benchmark
    public IntObjectMap<Entity> putHPPCHashMap() {
        IntObjectMap<Entity> map = new IntObjectHashMap<>(setSize);
        for (int i = 0; i < setSize; i++) {
            map.put(addedKeys[i], addedValues[i]);
        }
        return map;
    }

    @Benchmark
    public MutableIntObjectMap<Entity> putEclipseHashMap() {
        MutableIntObjectMap<Entity> map = new org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap<>(setSize);
        for (int i = 0; i < setSize; i++) {
            map.put(addedKeys[i], addedValues[i]);
        }
        return map;
    }

    @Benchmark
    public Int2ObjectHashMap<Entity> putAgronaHashMap() {
        Int2ObjectHashMap<Entity> map = new Int2ObjectHashMap<>(setSize, 0.65f);
        for (int i = 0; i < setSize; i++) {
            map.put(addedKeys[i], addedValues[i]);
        }
        return map;
    }
}