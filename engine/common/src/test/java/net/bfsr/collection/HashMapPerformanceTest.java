package net.bfsr.collection;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TMap;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.bfsr.PerformanceTest;
import org.agrona.collections.Int2ObjectHashMap;
import org.agrona.collections.Object2ObjectHashMap;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public final class HashMapPerformanceTest {
    public static void main(String[] args) {
        testPrimitiveHashMap();
    }

    /**
     * hppcWormMap has the fastest GET and REMOVE and fast PUT
     * nettyMap the fastest PUT and fast GET
     * <p>
     * hppcWormMap GET: 0.651ms
     * nettyMap GET: 0.658ms
     * hppcMap GET: 0.664ms
     * fastUtilMap GET: 0.685ms
     * <p>
     * hppcWormMap REMOVE: 0.429ms
     * eclipseMap REMOVE: 0.758ms
     * javaMap REMOVE: 0.908ms
     * hppcMap REMOVE: 1.055ms
     * <p>
     * nettyMap PUT: 1.125ms
     * hppcWormMap PUT: 1.144ms
     * hppcMap PUT: 1.275ms
     * agronaMap PUT: 1.362ms
     **/
    private static void testPrimitiveHashMap() {
        int count = 100_000;
        Map<Integer, Entity> javaMap = new HashMap<>(count);
        TIntObjectMap<Entity> troveMap = new TIntObjectHashMap<>(count);
        Int2ObjectHashMap<Entity> agronaMap = new Int2ObjectHashMap<>(count, 0.65f);
        IntObjectMap<Entity> nettyMap = new IntObjectHashMap<>(count);
        Int2ObjectMap<Entity> fastUtilMap = new Int2ObjectOpenHashMap<>(count);
        com.carrotsearch.hppc.IntObjectMap<Entity> hppcMap = new com.carrotsearch.hppc.IntObjectHashMap<>(count);
        MutableIntObjectMap<Entity> eclipseMap = new org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap<>(count);

        Random random = new Random(3487162487124L);
        int[] keys = new int[count];
        for (int i = 0; i < keys.length; i++) {
            keys[i] = random.nextInt();
        }

        List<Entity> values = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            values.add(new Entity());
        }

        int iterations = 100;
        for (int j = 0; j < iterations; j++) {
            PerformanceTest.beginTest();
            for (int i = 0; i < keys.length; i++) {
                javaMap.put(keys[i], values.get(i));
            }
            PerformanceTest.finishTest("javaMap PUT");
            PerformanceTest.beginTest();
            for (int i = 0; i < keys.length; i++) {
                troveMap.put(keys[i], values.get(i));
            }
            PerformanceTest.finishTest("troveMap PUT");
            PerformanceTest.beginTest();
            for (int i = 0; i < keys.length; i++) {
                agronaMap.put(keys[i], values.get(i));
            }
            PerformanceTest.finishTest("agronaMap PUT");
            PerformanceTest.beginTest();
            for (int i = 0; i < keys.length; i++) {
                nettyMap.put(keys[i], values.get(i));
            }
            PerformanceTest.finishTest("nettyMap PUT");
            PerformanceTest.beginTest();
            for (int i = 0; i < keys.length; i++) {
                fastUtilMap.put(keys[i], values.get(i));
            }
            PerformanceTest.finishTest("fastUtilMap PUT");
            PerformanceTest.beginTest();
            for (int i = 0; i < keys.length; i++) {
                hppcMap.put(keys[i], values.get(i));
            }
            PerformanceTest.finishTest("hppcMap PUT");
            PerformanceTest.beginTest();
            for (int i = 0; i < keys.length; i++) {
                eclipseMap.put(keys[i], values.get(i));
            }
            PerformanceTest.finishTest("eclipseMap PUT");

            //-------------------------------------------------

            PerformanceTest.beginTest();
            Entity entity1 = null;
            for (int i = 0; i < keys.length; i++) {
                entity1 = javaMap.get(keys[i]);
            }
            PerformanceTest.finishTest("javaMap GET");
            PerformanceTest.beginTest();
            for (int i = 0; i < keys.length; i++) {
                entity1 = troveMap.get(keys[i]);
            }
            PerformanceTest.finishTest("troveMap GET");
            PerformanceTest.beginTest();
            for (int i = 0; i < keys.length; i++) {
                entity1 = agronaMap.get(keys[i]);
            }
            PerformanceTest.finishTest("agronaMap GET");
            PerformanceTest.beginTest();
            for (int i = 0; i < keys.length; i++) {
                entity1 = nettyMap.get(keys[i]);
            }
            PerformanceTest.finishTest("nettyMap GET");
            PerformanceTest.beginTest();
            for (int i = 0; i < keys.length; i++) {
                entity1 = fastUtilMap.get(keys[i]);
            }
            PerformanceTest.finishTest("fastUtilMap GET");
            PerformanceTest.beginTest();
            for (int i = 0; i < keys.length; i++) {
                entity1 = hppcMap.get(keys[i]);
            }
            PerformanceTest.finishTest("hppcMap GET");
            PerformanceTest.beginTest();
            for (int i = 0; i < keys.length; i++) {
                entity1 = eclipseMap.get(keys[i]);
            }
            PerformanceTest.finishTest("eclipseMap GET");
            System.out.println(entity1);

            //-------------------------------------------------

            PerformanceTest.beginTest();
            for (int i = 0; i < keys.length; i++) {
                javaMap.remove(keys[i]);
            }
            PerformanceTest.finishTest("javaMap REMOVE");
            PerformanceTest.beginTest();
            for (int i = 0; i < keys.length; i++) {
                troveMap.remove(keys[i]);
            }
            PerformanceTest.finishTest("troveMap REMOVE");
            PerformanceTest.beginTest();
            for (int i = 0; i < keys.length; i++) {
                agronaMap.remove(keys[i]);
            }
            PerformanceTest.finishTest("agronaMap REMOVE");
            PerformanceTest.beginTest();
            for (int i = 0; i < keys.length; i++) {
                nettyMap.remove(keys[i]);
            }
            PerformanceTest.finishTest("nettyMap REMOVE");
            PerformanceTest.beginTest();
            for (int i = 0; i < keys.length; i++) {
                fastUtilMap.remove(keys[i]);
            }
            PerformanceTest.finishTest("fastUtilMap REMOVE");
            PerformanceTest.beginTest();
            for (int i = 0; i < keys.length; i++) {
                hppcMap.remove(keys[i]);
            }
            PerformanceTest.finishTest("hppcMap REMOVE");
            PerformanceTest.beginTest();
            for (int i = 0; i < keys.length; i++) {
                eclipseMap.remove(keys[i]);
            }
            PerformanceTest.finishTest("eclipseMap REMOVE");
        }

        PerformanceTest.outAverageTimeInfo();
    }

    /**
     * hppcWormMap the fastest GET and REMOVE
     * javaMap the fastest PUT
     * <p>
     * PUT
     * javaMap PUT: 0.050ms
     * hppcMap PUT: 0.062ms
     * eclipseMap PUT: 0.065ms
     * agronaMap PUT: 0.066ms
     * hppcWormMap PUT: 0.069ms
     * fastUtilMap PUT: 0.141ms
     * troveMap PUT: 0.175ms
     * <p>
     * GET
     * hppcWormMap GET: 0.037ms
     * javaMap GET: 0.038ms
     * hppcMap GET: 0.038ms
     * eclipseMap GET: 0.043ms
     * agronaMap GET: 0.045ms
     * fastUtilMap GET: 0.052ms
     * troveMap GET: 0.059ms
     * <p>
     * REMOVE
     * hppcWormMap REMOVE: 0.035ms
     * javaMap REMOVE: 0.037ms
     * eclipseMap REMOVE: 0.045ms
     * agronaMap REMOVE: 0.050ms
     * hppcMap REMOVE: 0.051ms
     * fastUtilMap REMOVE: 0.149ms
     * troveMap REMOVE: 0.269ms
     */
    private static void testObjectHashMap() {
        int count = 100_000;
        Map<Class, Entity> javaMap = new HashMap<>(count);
        TMap<Class, Entity> troveMap = new THashMap<>(count);
        Object2ObjectHashMap<Class, Entity> agronaMap = new Object2ObjectHashMap<>(count, 0.65f);
//        IntObjectMap<Entity> nettyMap = new IntObjectHashMap<>(count);
        Object2ObjectMap<Class, Entity> fastUtilMap = new Object2ObjectOpenHashMap();
        com.carrotsearch.hppc.ObjectObjectMap<Class, Entity> hppcMap = new com.carrotsearch.hppc.ObjectObjectHashMap<>(count);
        MutableMap<Class, Entity> eclipseMap = new org.eclipse.collections.impl.map.mutable.UnifiedMap<>(count);

        List<Class> classes = new ArrayList<>();
        Collection<URL> allPackagePrefixes = Arrays.stream(Package.getPackages()).map(p -> p.getName())
                .map(s -> s.split("\\.")[0]).distinct().map(s -> ClasspathHelper.forPackage(s)).reduce((c1, c2) -> {
                    Collection<URL> c3 = new HashSet<>();
                    c3.addAll(c1);
                    c3.addAll(c2);
                    return c3;
                }).get();
        ConfigurationBuilder config = new ConfigurationBuilder().addUrls(allPackagePrefixes)
                .addScanners(Scanners.SubTypes, Scanners.TypesAnnotated);
        Reflections reflections = new Reflections(config);
        Set<Class> classSet = reflections.get(Scanners.SubTypes.of(List.class).as(Class.class));
        classSet.forEach(classes::add);
        classSet = reflections.get(Scanners.SubTypes.of(Map.class).as(Class.class));
        classSet.forEach(classes::add);
        classSet = reflections.get(Scanners.SubTypes.of(Set.class).as(Class.class));
        classSet.forEach(classes::add);

        List<Entity> values = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            values.add(new Entity());
        }

        int iterations = 100;
        for (int j = 0; j < iterations; j++) {
            PerformanceTest.beginTest();
            for (int i = 0; i < classes.size(); i++) {
                javaMap.put(classes.get(i), values.get(i));
            }
            PerformanceTest.finishTest("javaMap PUT");
            PerformanceTest.beginTest();
            for (int i = 0; i < classes.size(); i++) {
                troveMap.put(classes.get(i), values.get(i));
            }
            PerformanceTest.finishTest("troveMap PUT");
            PerformanceTest.beginTest();
            for (int i = 0; i < classes.size(); i++) {
                agronaMap.put(classes.get(i), values.get(i));
            }
            PerformanceTest.finishTest("agronaMap PUT");
            PerformanceTest.beginTest();
            for (int i = 0; i < classes.size(); i++) {
                fastUtilMap.put(classes.get(i), values.get(i));
            }
            PerformanceTest.finishTest("fastUtilMap PUT");
            PerformanceTest.beginTest();
            for (int i = 0; i < classes.size(); i++) {
                hppcMap.put(classes.get(i), values.get(i));
            }
            PerformanceTest.finishTest("hppcMap PUT");
            PerformanceTest.beginTest();
            for (int i = 0; i < classes.size(); i++) {
                eclipseMap.put(classes.get(i), values.get(i));
            }
            PerformanceTest.finishTest("eclipseMap PUT");

            //-------------------------------------------------

            PerformanceTest.beginTest();
            for (int i = 0; i < classes.size(); i++) {
                Entity entity1 = javaMap.get(classes.get(i));
            }
            PerformanceTest.finishTest("javaMap GET");
            PerformanceTest.beginTest();
            for (int i = 0; i < classes.size(); i++) {
                Entity entity1 = troveMap.get(classes.get(i));
            }
            PerformanceTest.finishTest("troveMap GET");
            PerformanceTest.beginTest();
            for (int i = 0; i < classes.size(); i++) {
                Entity entity1 = agronaMap.get(classes.get(i));
            }
            PerformanceTest.finishTest("agronaMap GET");
            PerformanceTest.beginTest();
            for (int i = 0; i < classes.size(); i++) {
                Entity entity1 = fastUtilMap.get(classes.get(i));
            }
            PerformanceTest.finishTest("fastUtilMap GET");
            PerformanceTest.beginTest();
            for (int i = 0; i < classes.size(); i++) {
                Entity entity1 = hppcMap.get(classes.get(i));
            }
            PerformanceTest.finishTest("hppcMap GET");
            PerformanceTest.beginTest();
            for (int i = 0; i < classes.size(); i++) {
                Entity entity1 = eclipseMap.get(classes.get(i));
            }
            PerformanceTest.finishTest("eclipseMap GET");

            //-------------------------------------------------

            PerformanceTest.beginTest();
            for (int i = 0; i < classes.size(); i++) {
                javaMap.remove(classes.get(i));
            }
            PerformanceTest.finishTest("javaMap REMOVE");
            PerformanceTest.beginTest();
            for (int i = 0; i < classes.size(); i++) {
                troveMap.remove(classes.get(i));
            }
            PerformanceTest.finishTest("troveMap REMOVE");
            PerformanceTest.beginTest();
            for (int i = 0; i < classes.size(); i++) {
                agronaMap.remove(classes.get(i));
            }
            PerformanceTest.finishTest("agronaMap REMOVE");
            PerformanceTest.beginTest();
            for (int i = 0; i < classes.size(); i++) {
                fastUtilMap.remove(classes.get(i));
            }
            PerformanceTest.finishTest("fastUtilMap REMOVE");
            PerformanceTest.beginTest();
            for (int i = 0; i < classes.size(); i++) {
                hppcMap.remove(classes.get(i));
            }
            PerformanceTest.finishTest("hppcMap REMOVE");
            PerformanceTest.beginTest();
            for (int i = 0; i < classes.size(); i++) {
                eclipseMap.remove(classes.get(i));
            }
            PerformanceTest.finishTest("eclipseMap REMOVE");
        }

        PerformanceTest.outAverageTimeInfo();
    }

    private static class Entity {

    }
}