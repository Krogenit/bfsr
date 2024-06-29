package net.bfsr.engine.profiler;

import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import it.unimi.dsi.fastutil.objects.Object2FloatLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Profiler {
    @Setter
    private boolean enable;
    private final Object2LongOpenHashMap<String> startTime = new Object2LongOpenHashMap<>();
    private final Object2FloatLinkedOpenHashMap<String> results = new Object2FloatLinkedOpenHashMap<>();
    private String currentCategory;
    private String fullCategory = "root";
    private final Node resultTree = new Node("root");

    public void start(String name) {
        if (enable) {
            currentCategory = name;
            fullCategory += "." + name;
            startTime.put(fullCategory, System.nanoTime());
        }
    }

    public void endStart(String name) {
        if (enable) {
            end();
            start(name);
        }
    }

    public void end() {
        if (enable) {
            long startTime = this.startTime.getLong(fullCategory);
            results.put(fullCategory, (System.nanoTime() - startTime) / 1_000_000.0f);
            fullCategory = fullCategory.substring(0, fullCategory.length() - (currentCategory.length() + 1));
            currentCategory = fullCategory.substring(fullCategory.lastIndexOf('.') + 1);
        }
    }

    public float getResult(String name) {
        return results.getOrDefault(name, 0.0f);
    }

    public void print() {
        StringBuilder stringBuilder = new StringBuilder(16);
        getResults(true).compute(node -> System.out.println(stringBuilder + node.getName() + ", time: " + node.getAverageTime()),
                (node) -> stringBuilder.append(" "), (node) -> stringBuilder.deleteCharAt(stringBuilder.length() - 1));
    }

    public Node getResults(boolean sort) {
        results.object2FloatEntrySet().fastForEach(entry -> {
            resultTree.getOrCreateCategory(entry.getKey()).addTime(entry.getFloatValue());
        });

        if (sort) {
            resultTree.sort();
        }

        return resultTree;
    }

    @Getter
    @RequiredArgsConstructor
    public static class Result {
        private final String category;
        private final float time;
    }

    @Getter
    @RequiredArgsConstructor
    public static class Node {
        private static final int TIMES_COUNT = 100;

        private final String name;
        @Setter
        private FloatList floatList = new FloatArrayList(TIMES_COUNT);
        private float averageTime;
        private final List<Node> childNodes = new ArrayList<>();

        private Node getOrCreateCategory(String category) {
            String nextCategories = category.substring(category.indexOf('.') + 1);
            if (nextCategories.indexOf('.') != -1) {
                String currentCategory = nextCategories.substring(0, nextCategories.indexOf('.'));
                return findOrCreateNode(currentCategory).getOrCreateCategory(nextCategories);
            } else {
                return findOrCreateNode(nextCategories);
            }
        }

        private Node findOrCreateNode(String category) {
            for (int i = 0; i < childNodes.size(); i++) {
                Node node = childNodes.get(i);
                if (node.name.equals(category)) {
                    return node;
                }
            }

            Node node = new Node(category);
            childNodes.add(node);
            return node;
        }

        public void compute(Consumer<Node> nodeConsumer, Consumer<Node> onLevelDown, Consumer<Node> onLevelUp) {
            for (int i = 0; i < childNodes.size(); i++) {
                Node node = childNodes.get(i);
                nodeConsumer.accept(node);
                onLevelDown.accept(node);
                node.compute(nodeConsumer, onLevelDown, onLevelUp);
                onLevelUp.accept(node);
            }
        }

        private void addTime(float time) {
            if (floatList.size() == TIMES_COUNT) {
                floatList.removeFloat(0);
            }

            floatList.add(time);

            int size = floatList.size();
            float totalTime = 0;
            for (int i = 0; i < size; i++) {
                totalTime += floatList.getFloat(i);
            }

            averageTime = totalTime / size;
        }

        private void sort() {
            childNodes.sort((result1, result2) -> result2.averageTime < result1.averageTime ? -1 :
                    (result2.averageTime > result1.averageTime ? 1 : result2.name.compareTo(result1.name)));

            for (int i = 0; i < childNodes.size(); i++) {
                Node node = childNodes.get(i);
                node.sort();
            }
        }
    }
}