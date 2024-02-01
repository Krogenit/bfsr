package net.bfsr.collection;

import com.carrotsearch.hppc.IntObjectHashMap;
import com.carrotsearch.hppc.IntObjectMap;
import com.carrotsearch.hppc.IntObjectWormMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.agrona.collections.Int2ObjectHashMap;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.openjdk.jmh.annotations.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class RemoveMapBenchmarks {
    @Param({"100000"})
    int setSize;

    int[] addedKeys;
    Entity[] addedValues;
    int[] removeKeys;

    Map<Integer, Entity> map;
    TIntObjectMap<Entity> troveMap;
    Int2ObjectMap<Entity> fastUtilMap;
    io.netty.util.collection.IntObjectMap<Entity> nettyMap;
    IntObjectMap<Entity> hppcMap;
    IntObjectMap<Entity> hppcWormMap;
    MutableIntObjectMap<Entity> eclipseMap;
    Int2ObjectHashMap<Entity> agronaMap;

    @Setup(Level.Iteration)
    public void init() {
        addedKeys = new int[setSize];
        addedValues = new Entity[setSize];
        Random random = new Random(3487162487124L);
        for (int i = 0; i < setSize; i++) {
            addedKeys[i] = random.nextInt();
            addedValues[i] = new Entity();
        }

        map = new HashMap<>(setSize);
        troveMap = new TIntObjectHashMap<>(setSize);
        fastUtilMap = new Int2ObjectOpenHashMap<>(setSize);
        nettyMap = new io.netty.util.collection.IntObjectHashMap<>(setSize);
        hppcMap = new IntObjectHashMap<>(setSize);
        hppcWormMap = new IntObjectWormMap<>(setSize);
        eclipseMap = new org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap<>(setSize);
        agronaMap = new Int2ObjectHashMap<>(setSize, 0.65f);

        for (int i = 0; i < setSize; i++) {
            map.put(addedKeys[i], addedValues[i]);
            troveMap.put(addedKeys[i], addedValues[i]);
            fastUtilMap.put(addedKeys[i], addedValues[i]);
            nettyMap.put(addedKeys[i], addedValues[i]);
            hppcMap.put(addedKeys[i], addedValues[i]);
            hppcWormMap.put(addedKeys[i], addedValues[i]);
            eclipseMap.put(addedKeys[i], addedValues[i]);
            agronaMap.put(addedKeys[i], addedValues[i]);
        }

        removeKeys = new int[setSize];
        System.arraycopy(addedKeys, 0, removeKeys, 0, setSize);
    }

    @Benchmark
    public void removeJavaMap() {
        for (int i = 0; i < removeKeys.length; i++) {
            map.remove(removeKeys[i]);
        }
    }

    @Benchmark
    public void removeTroveMap() {
        for (int i = 0; i < removeKeys.length; i++) {
            troveMap.remove(removeKeys[i]);
        }
    }

    @Benchmark
    public void removeFastUtilMap() {
        for (int i = 0; i < removeKeys.length; i++) {
            fastUtilMap.remove(removeKeys[i]);
        }
    }

    @Benchmark
    public void removeNettyMap() {
        for (int i = 0; i < removeKeys.length; i++) {
            nettyMap.remove(removeKeys[i]);
        }
    }

    @Benchmark
    public void removeHPPCMap() {
        for (int i = 0; i < removeKeys.length; i++) {
            hppcMap.remove(removeKeys[i]);
        }
    }

    @Benchmark
    public void removeHPPCWormMap() {
        for (int i = 0; i < removeKeys.length; i++) {
            hppcWormMap.remove(removeKeys[i]);
        }
    }

    @Benchmark
    public void removeEclipseMap() {
        for (int i = 0; i < removeKeys.length; i++) {
            eclipseMap.remove(removeKeys[i]);
        }
    }

    @Benchmark
    public void removeAgronaMap() {
        for (int i = 0; i < removeKeys.length; i++) {
            agronaMap.remove(removeKeys[i]);
        }
    }
}