package net.bfsr.engine.renderer.buffer;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.engine.collection.UnorderedArrayList;
import net.bfsr.engine.renderer.AbstractSpriteRenderer;
import net.bfsr.engine.renderer.primitive.VAO;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import static net.bfsr.engine.renderer.AbstractSpriteRenderer.BASE_INSTANCE_OFFSET;
import static net.bfsr.engine.renderer.AbstractSpriteRenderer.BASE_VERTEX_OFFSET;
import static net.bfsr.engine.renderer.AbstractSpriteRenderer.COMMAND_SIZE;
import static net.bfsr.engine.renderer.AbstractSpriteRenderer.FIRST_INDEX_OFFSET;
import static net.bfsr.engine.renderer.AbstractSpriteRenderer.FOUR_BYTES_ELEMENT_SHIFT;
import static net.bfsr.engine.renderer.AbstractSpriteRenderer.INSTANCE_COUNT_OFFSET;
import static net.bfsr.engine.renderer.AbstractSpriteRenderer.LAST_UPDATE_MATERIAL_DATA_SIZE_IN_BYTES;
import static net.bfsr.engine.renderer.AbstractSpriteRenderer.MATERIAL_DATA_SIZE_IN_BYTES;
import static net.bfsr.engine.renderer.AbstractSpriteRenderer.MODEL_DATA_SIZE;
import static net.bfsr.engine.renderer.AbstractSpriteRenderer.QUAD_INDEX_COUNT;
import static org.lwjgl.opengl.GL44C.GL_DYNAMIC_STORAGE_BIT;

@Getter
public class BuffersHolder extends AbstractBuffersHolder {
    private final VAO vao;

    @Setter
    private Future<?> future = CompletableFuture.completedFuture(null);

    private FloatBuffer modelBuffer;
    private long modelBufferAddress;
    private final int modelDataBufferResizeCapacity;
    @Setter
    private boolean modelBufferDirty;

    private FloatBuffer lastUpdateModelBuffer;
    private long lastUpdateModelBufferAddress;
    @Setter
    private boolean lastUpdateModelBufferDirty;

    private ByteBuffer materialBuffer;
    private long materialBufferAddress;
    private final int materialBufferResizeCapacity;
    @Setter
    private boolean materialBufferDirty;

    private ByteBuffer lastUpdateMaterialBuffer;
    private long lastUpdateMaterialBufferAddress;
    private final int lastUpdateMaterialBufferResizeCapacity;
    @Setter
    private boolean lastUpdateMaterialBufferDirty;

    private IntBuffer commandBuffer;
    private final int commandBufferResizeCapacity;
    private long commandBufferAddress;

    @Setter
    private int renderObjects;
    @Getter
    protected int bufferUsage;
    private int baseInstance;
    private int maxBufferCapacity;
    private final UnorderedArrayList<Integer> freeIndices = new UnorderedArrayList<>();

    public BuffersHolder(VAO vao, int initialObjectCount) {
        this.vao = vao;

        modelDataBufferResizeCapacity = initialObjectCount * AbstractSpriteRenderer.MODEL_DATA_SIZE;
        materialBufferResizeCapacity = initialObjectCount * MATERIAL_DATA_SIZE_IN_BYTES;
        lastUpdateMaterialBufferResizeCapacity = initialObjectCount * LAST_UPDATE_MATERIAL_DATA_SIZE_IN_BYTES;

        modelBuffer = MemoryUtil.memAllocFloat(modelDataBufferResizeCapacity);
        modelBufferAddress = MemoryUtil.memAddress(modelBuffer);

        lastUpdateModelBuffer = MemoryUtil.memAllocFloat(modelDataBufferResizeCapacity);
        lastUpdateModelBufferAddress = MemoryUtil.memAddress(lastUpdateModelBuffer);

        materialBuffer = MemoryUtil.memAlloc(materialBufferResizeCapacity);
        materialBufferAddress = MemoryUtil.memAddress(materialBuffer);

        lastUpdateMaterialBuffer = MemoryUtil.memAlloc(lastUpdateMaterialBufferResizeCapacity);
        lastUpdateMaterialBufferAddress = MemoryUtil.memAddress(lastUpdateMaterialBuffer);

        commandBufferResizeCapacity = initialObjectCount * COMMAND_SIZE;
        commandBuffer = MemoryUtil.memAllocInt(commandBufferResizeCapacity);
        commandBufferAddress = MemoryUtil.memAddress(commandBuffer);
        fillCommandBuffer(commandBuffer, 0);

        maxBufferCapacity = initialObjectCount;
    }

    private void fillCommandBuffer(IntBuffer commandBuffer, int offset) {
        for (int i = offset; i < commandBuffer.capacity(); i += COMMAND_SIZE) {
            putCommandData(i, QUAD_INDEX_COUNT);
            putCommandData(i + INSTANCE_COUNT_OFFSET, 1);
            putCommandData(i + FIRST_INDEX_OFFSET, 0);
            putCommandData(i + BASE_VERTEX_OFFSET, 0);
            putCommandData(i + BASE_INSTANCE_OFFSET, 0);
        }
    }

    @Override
    public void checkBuffersSize(int objectCount) {
        int remainingBufferCapacity = modelBuffer.capacity() - bufferUsage * MODEL_DATA_SIZE;
        int requiredBufferCapacity = objectCount * MODEL_DATA_SIZE;
        if (remainingBufferCapacity < requiredBufferCapacity) {
            int resizeAmount = Math.max(modelDataBufferResizeCapacity, requiredBufferCapacity);
            modelBuffer = MemoryUtil.memRealloc(modelBuffer, modelBuffer.capacity() + resizeAmount);
            modelBufferAddress = MemoryUtil.memAddress(modelBuffer);
            lastUpdateModelBuffer = MemoryUtil.memRealloc(lastUpdateModelBuffer, lastUpdateModelBuffer.capacity() + resizeAmount);
            lastUpdateModelBufferAddress = MemoryUtil.memAddress(lastUpdateModelBuffer);

            maxBufferCapacity = modelBuffer.capacity() / MODEL_DATA_SIZE;
        }

        remainingBufferCapacity = materialBuffer.capacity() - bufferUsage * MATERIAL_DATA_SIZE_IN_BYTES;
        requiredBufferCapacity = objectCount * MATERIAL_DATA_SIZE_IN_BYTES;
        if (remainingBufferCapacity < requiredBufferCapacity) {
            int resizeAmount = Math.max(materialBufferResizeCapacity, requiredBufferCapacity);
            materialBuffer = MemoryUtil.memRealloc(materialBuffer, materialBuffer.capacity() + resizeAmount);
            materialBufferAddress = MemoryUtil.memAddress(materialBuffer);

            lastUpdateMaterialBuffer = MemoryUtil.memRealloc(lastUpdateMaterialBuffer,
                    lastUpdateMaterialBuffer.capacity() + resizeAmount / (MATERIAL_DATA_SIZE_IN_BYTES /
                            LAST_UPDATE_MATERIAL_DATA_SIZE_IN_BYTES));
            lastUpdateMaterialBufferAddress = MemoryUtil.memAddress(lastUpdateMaterialBuffer);
        }

        int capacity = commandBuffer.capacity();
        remainingBufferCapacity = capacity - bufferUsage * COMMAND_SIZE;
        requiredBufferCapacity = objectCount * COMMAND_SIZE;
        if (remainingBufferCapacity < requiredBufferCapacity) {
            int resizeAmount = Math.max(commandBufferResizeCapacity, requiredBufferCapacity);
            commandBuffer = MemoryUtil.memRealloc(commandBuffer, capacity + resizeAmount);
            commandBufferAddress = MemoryUtil.memAddress(commandBuffer);
            fillCommandBuffer(commandBuffer, capacity);
        }
    }

    @Override
    public void updateBuffers(int modelBufferIndex, int materialBufferIndex, int lastUpdateModelBufferIndex,
                              int lastUpdateMaterialBufferIndex) {
        if (modelBufferDirty) {
            vao.updateBuffer(modelBufferIndex, modelBuffer, GL_DYNAMIC_STORAGE_BIT);
            modelBufferDirty = false;
        }

        if (materialBufferDirty) {
            vao.updateBuffer(materialBufferIndex, materialBuffer, GL_DYNAMIC_STORAGE_BIT);
            materialBufferDirty = false;
        }

        if (lastUpdateModelBufferDirty) {
            vao.updateBuffer(lastUpdateModelBufferIndex, lastUpdateModelBuffer, GL_DYNAMIC_STORAGE_BIT);
            lastUpdateModelBufferDirty = false;
        }

        if (lastUpdateMaterialBufferDirty) {
            vao.updateBuffer(lastUpdateMaterialBufferIndex, lastUpdateMaterialBuffer, GL_DYNAMIC_STORAGE_BIT);
            lastUpdateMaterialBufferDirty = false;
        }
    }

    @Override
    public void markAllBuffersDirty() {
        modelBufferDirty = true;
        materialBufferDirty = true;
        lastUpdateModelBufferDirty = true;
        lastUpdateMaterialBufferDirty = true;
    }

    @Override
    public void putModelData(int offset, float value) {
        MemoryUtil.memPutFloat(modelBufferAddress + ((offset & 0xFFFF_FFFFL) << FOUR_BYTES_ELEMENT_SHIFT), value);
    }

    @Override
    public void putLastUpdateModelData(int offset, float value) {
        MemoryUtil.memPutFloat(lastUpdateModelBufferAddress + ((offset & 0xFFFF_FFFFL) << FOUR_BYTES_ELEMENT_SHIFT), value);
    }

    @Override
    public void putMaterialData(int offset, float value) {
        MemoryUtil.memPutFloat(materialBufferAddress + (offset & 0xFFFF_FFFFL), value);
    }

    @Override
    public void putMaterialData(int offset, int value) {
        MemoryUtil.memPutInt(materialBufferAddress + (offset & 0xFFFF_FFFFL), value);
    }

    @Override
    public void putMaterialData(int offset, long value) {
        MemoryUtil.memPutLong(materialBufferAddress + (offset & 0xFFFF_FFFFL), value);
    }

    @Override
    public void putLastUpdateMaterialData(int offset, float value) {
        MemoryUtil.memPutFloat(lastUpdateMaterialBufferAddress + (offset & 0xFFFF_FFFFL), value);
    }

    @Override
    public void putLastUpdateMaterialData(int offset, int value) {
        MemoryUtil.memPutInt(lastUpdateMaterialBufferAddress + (offset & 0xFFFF_FFFFL), value);
    }

    @Override
    public void putCommandData(int offset, int value) {
        MemoryUtil.memPutInt(commandBufferAddress + ((offset & 0xFFFF_FFFFL) << FOUR_BYTES_ELEMENT_SHIFT), value);
    }

    @Override
    public void incrementRenderObjects() {
        renderObjects++;
    }

    @Override
    public void addRenderObjectsCount(int count) {
        renderObjects += count;
    }

    public void incrementBufferUsage() {
        bufferUsage++;
    }

    public void decrementBufferUsage() {
        bufferUsage--;
    }

    private int getAndIncrementBaseInstance() {
        return baseInstance++;
    }

    @Override
    public int getNextBaseInstanceId() {
        if (freeIndices.size() > 0) {
            incrementBufferUsage();
            return freeIndices.remove(0);
        }

        checkBuffersSize(1);
        incrementBufferUsage();

        return getAndIncrementBaseInstance();
    }

    public void removeObject(int id) {
        freeIndices.add(id);
        decrementBufferUsage();
    }

    @Override
    public void clear() {
        renderObjects = 0;
        bufferUsage = 0;
        baseInstance = 0;
        MemoryUtil.memFree(modelBuffer);
        MemoryUtil.memFree(materialBuffer);
        MemoryUtil.memFree(lastUpdateModelBuffer);
        MemoryUtil.memFree(lastUpdateMaterialBuffer);
        MemoryUtil.memFree(commandBuffer);
        vao.clear();
    }
}