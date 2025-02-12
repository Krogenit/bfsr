package net.bfsr.engine.renderer;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import lombok.Getter;
import net.bfsr.engine.math.LUT;
import net.bfsr.engine.renderer.buffer.AbstractBuffersHolder;
import net.bfsr.engine.renderer.buffer.BufferType;
import net.bfsr.engine.renderer.buffer.BuffersHolder;
import net.bfsr.engine.renderer.primitive.AbstractVAO;
import net.bfsr.engine.renderer.primitive.Primitive;
import net.bfsr.engine.renderer.primitive.VAO;
import net.bfsr.engine.renderer.primitive.VBO;
import net.bfsr.engine.util.MultithreadingUtils;
import net.bfsr.engine.util.MutableInt;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11C.GL_TRIANGLES;
import static org.lwjgl.opengl.GL43.glMultiDrawElementsIndirect;
import static org.lwjgl.opengl.GL43C.GL_SHADER_STORAGE_BUFFER;

public class SpriteRenderer implements AbstractSpriteRenderer {
    private static final int VERTEX_STRIDE = 16;

    public static final int MODEL_BUFFER_INDEX = 0;
    public static final int MATERIAL_BUFFER_INDEX = 1;
    public static final int LAST_UPDATE_MODEL_BUFFER_INDEX = 2;
    public static final int LAST_UPDATE_MATERIAL_BUFFER_INDEX = 3;

    private static final int SSBO_MODEL_DATA = 0;
    private static final int SSBO_LAST_UPDATE_MODEL_DATA = 1;
    private static final int SSBO_MATERIAL_DATA = 2;
    private static final int SSBO_LAST_UPDATE_MATERIAL_DATA = 3;

    public static final int Y_OFFSET = 1;
    public static final int SIN_OFFSET = 2;
    public static final int COS_OFFSET = 3;
    public static final int WIDTH_OFFSET = 4;
    public static final int HEIGHT_OFFSET = 5;

    public static final int COLOR_G_OFFSET = 4;
    public static final int COLOR_B_OFFSET = 8;
    public static final int COLOR_A_OFFSET = 12;
    public static final int TEXTURE_HANDLE_OFFSET = 16;
    private static final int MASK_TEXTURE_HANDLE_OFFSET = 24;
    private static final int MATERIAL_TYPE_OFFSET = 32;
    private static final int FIRE_AMOUNT_OFFSET = 36;
    private static final int FIRE_UV_ANIMATION_OFFSET = 40;
    private static final int ZOOM_FACTOR_OFFSET = 44;

    private static final int LAST_FIRE_AMOUNT_OFFSET = 16;
    private static final int LAST_FIRE_UV_ANIMATION_OFFSET = 20;
    private static final int LAST_PADDING_1_OFFSET = 24;
    private static final int LAST_PADDING_2_OFFSET = 28;

    private FloatBuffer vertexBuffer;
    private final MutableInt vertexBufferIndex = new MutableInt();
    private long vertexBufferAddress;
    private final VBO vertexVBO;

    private final IntBuffer indexBuffer;
    private final VBO indexVBO;

    private final ObjectSet<Primitive> primitiveMap = new ObjectOpenHashSet<>();

    protected AbstractRenderer renderer;
    private ExecutorService executorService;
    @Getter
    final BuffersHolder[] buffersHolders = createBuffersHolderArray(BufferType.values().length);
    private final BiConsumer<Runnable, BufferType> addTaskConsumer;

    private boolean persistentMappedBuffers = true;

    public SpriteRenderer() {
        float[] positionsUvs = {
                // Centered quad
                -0.5f, -0.5f, 0.0f, 0.0f,
                0.5f, -0.5f, 1.0f, 0.0f,
                0.5f, 0.5f, 1.0f, 1.0f,
                -0.5f, 0.5f, 0.0f, 1.0f,
                // Simple quad
                0.0f, 0.0f, 0.0f, 0.0f,
                1.0f, 0.0f, 1.0f, 0.0f,
                1.0f, 1.0f, 1.0f, 1.0f,
                0.0f, 1.0f, 0.0f, 1.0f,
        };

        vertexBuffer = MemoryUtil.memAllocFloat(positionsUvs.length);
        vertexBufferAddress = MemoryUtil.memAddress(vertexBuffer);
        vertexBuffer.put(vertexBufferIndex.getAndAdd(positionsUvs.length), positionsUvs);
        vertexVBO = VBO.create();
        vertexVBO.storeData(vertexBuffer, 0);

        int[] indices = {0, 1, 2, 2, 3, 0};

        indexBuffer = MemoryUtil.memAllocInt(indices.length);
        indexBuffer.put(0, indices);
        indexVBO = VBO.create();
        indexVBO.storeData(indexBuffer, 0);

        buffersHolders[BufferType.BACKGROUND.ordinal()] = createBuffersHolder(1, true);
        buffersHolders[BufferType.ENTITIES_BACKGROUND_ADDITIVE.ordinal()] = createBuffersHolder(512, true);
        buffersHolders[BufferType.ENTITIES_ALPHA.ordinal()] = createBuffersHolder(512, true);
        buffersHolders[BufferType.ENTITIES_ADDITIVE.ordinal()] = createBuffersHolder(512, true);
        buffersHolders[BufferType.GUI.ordinal()] = createBuffersHolder(512, false);

        if (MultithreadingUtils.MULTITHREADING_SUPPORTED) {
            executorService = Executors.newFixedThreadPool(MultithreadingUtils.PARALLELISM);
            addTaskConsumer = (runnable, bufferType) -> buffersHolders[bufferType.ordinal()].setFuture(
                    executorService.submit(runnable));
        } else {
            addTaskConsumer = (runnable, bufferType) -> runnable.run();
        }
    }

    @Override
    public void init(AbstractRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public VAO createVAO() {
        VAO vao = VAO.create(4);
        vao.createVertexBuffers();
        vao.vertexArrayVertexBufferInternal(0, vertexVBO.getId(), VERTEX_STRIDE);
        vao.vertexArrayElementBufferInternal(indexVBO.getId());
        vao.attributeBindingAndFormat(0, 4, 0, 0);
        vao.enableAttributes(1);
        return vao;
    }

    @Override
    public void updateBuffers() {
        for (int i = 0; i < buffersHolders.length; i++) {
            BuffersHolder buffersHolder = buffersHolders[i];
            updateBuffers(buffersHolder);
        }
    }

    @Override
    public void updateBuffers(AbstractBuffersHolder[] buffersHolderArray) {
        for (int i = 0; i < buffersHolderArray.length; i++) {
            AbstractBuffersHolder buffersHolder = buffersHolderArray[i];
            updateBuffers(buffersHolder);
        }
    }

    public void updateBuffers(AbstractBuffersHolder buffersHolder) {
        buffersHolder.updateBuffers(MODEL_BUFFER_INDEX, MATERIAL_BUFFER_INDEX, LAST_UPDATE_MODEL_BUFFER_INDEX,
                LAST_UPDATE_MATERIAL_BUFFER_INDEX);
    }

    @Override
    public void waitForLockedRange() {
        for (int i = 0; i < buffersHolders.length; i++) {
            BuffersHolder buffersHolder = buffersHolders[i];
            if (i != BufferType.GUI.ordinal()) {
                buffersHolder.waitForLockedRange();
            }
        }
    }

    @Override
    public void waitForLockedRange(AbstractBuffersHolder[] buffersHolderArray) {
        for (int i = 0; i < buffersHolderArray.length; i++) {
            buffersHolderArray[i].waitForLockedRange();
        }
    }

    @Override
    public boolean addPrimitive(Primitive primitive) {
        if (primitiveMap.contains(primitive)) {
            return false;
        }

        int vertexIndex = vertexBufferIndex.get();
        int primitiveSize = 16;
        if (vertexBuffer.capacity() - vertexIndex < primitiveSize) {
            vertexBuffer = MemoryUtil.memRealloc(vertexBuffer, vertexBuffer.capacity() + primitiveSize);
            vertexBufferAddress = MemoryUtil.memAddress(vertexBuffer);
        }

        MemoryUtil.memPutFloat(vertexBufferAddress + ((vertexBufferIndex.getAndIncrement() & 0xFFFF_FFFFL) << FOUR_BYTES_ELEMENT_SHIFT),
                primitive.getX1());
        MemoryUtil.memPutFloat(vertexBufferAddress + ((vertexBufferIndex.getAndIncrement() & 0xFFFF_FFFFL) << FOUR_BYTES_ELEMENT_SHIFT),
                primitive.getY1());
        MemoryUtil.memPutFloat(vertexBufferAddress + ((vertexBufferIndex.getAndIncrement() & 0xFFFF_FFFFL) << FOUR_BYTES_ELEMENT_SHIFT),
                primitive.getU1());
        MemoryUtil.memPutFloat(vertexBufferAddress + ((vertexBufferIndex.getAndIncrement() & 0xFFFF_FFFFL) << FOUR_BYTES_ELEMENT_SHIFT),
                primitive.getV1());
        MemoryUtil.memPutFloat(vertexBufferAddress + ((vertexBufferIndex.getAndIncrement() & 0xFFFF_FFFFL) << FOUR_BYTES_ELEMENT_SHIFT),
                primitive.getX2());
        MemoryUtil.memPutFloat(vertexBufferAddress + ((vertexBufferIndex.getAndIncrement() & 0xFFFF_FFFFL) << FOUR_BYTES_ELEMENT_SHIFT),
                primitive.getY2());
        MemoryUtil.memPutFloat(vertexBufferAddress + ((vertexBufferIndex.getAndIncrement() & 0xFFFF_FFFFL) << FOUR_BYTES_ELEMENT_SHIFT),
                primitive.getU2());
        MemoryUtil.memPutFloat(vertexBufferAddress + ((vertexBufferIndex.getAndIncrement() & 0xFFFF_FFFFL) << FOUR_BYTES_ELEMENT_SHIFT),
                primitive.getV2());
        MemoryUtil.memPutFloat(vertexBufferAddress + ((vertexBufferIndex.getAndIncrement() & 0xFFFF_FFFFL) << FOUR_BYTES_ELEMENT_SHIFT),
                primitive.getX3());
        MemoryUtil.memPutFloat(vertexBufferAddress + ((vertexBufferIndex.getAndIncrement() & 0xFFFF_FFFFL) << FOUR_BYTES_ELEMENT_SHIFT),
                primitive.getY3());
        MemoryUtil.memPutFloat(vertexBufferAddress + ((vertexBufferIndex.getAndIncrement() & 0xFFFF_FFFFL) << FOUR_BYTES_ELEMENT_SHIFT),
                primitive.getU3());
        MemoryUtil.memPutFloat(vertexBufferAddress + ((vertexBufferIndex.getAndIncrement() & 0xFFFF_FFFFL) << FOUR_BYTES_ELEMENT_SHIFT),
                primitive.getV3());
        MemoryUtil.memPutFloat(vertexBufferAddress + ((vertexBufferIndex.getAndIncrement() & 0xFFFF_FFFFL) << FOUR_BYTES_ELEMENT_SHIFT),
                primitive.getX4());
        MemoryUtil.memPutFloat(vertexBufferAddress + ((vertexBufferIndex.getAndIncrement() & 0xFFFF_FFFFL) << FOUR_BYTES_ELEMENT_SHIFT),
                primitive.getY4());
        MemoryUtil.memPutFloat(vertexBufferAddress + ((vertexBufferIndex.getAndIncrement() & 0xFFFF_FFFFL) << FOUR_BYTES_ELEMENT_SHIFT),
                primitive.getU4());
        MemoryUtil.memPutFloat(vertexBufferAddress + ((vertexBufferIndex.getAndIncrement() & 0xFFFF_FFFFL) << FOUR_BYTES_ELEMENT_SHIFT),
                primitive.getV4());

        vertexVBO.storeData(vertexBuffer, 0);
        for (int i = 0; i < buffersHolders.length; i++) {
            buffersHolders[i].getVao().vertexArrayVertexBufferInternal(0, vertexVBO.getId(), VERTEX_STRIDE);
        }

        primitive.setBaseVertex(vertexIndex / VERTEX_DATA_SIZE);
        primitiveMap.add(primitive);
        return true;
    }

    @Override
    public void addTask(Runnable runnable, BufferType bufferType) {
        addTaskConsumer.accept(runnable, bufferType);
    }

    @Override
    public Future<?> addTask(Runnable runnable) {
        return executorService.submit(runnable);
    }

    @Override
    public void addDrawCommand(long commandBufferAddress, int count, BufferType bufferType) {
        addDrawCommand(commandBufferAddress, count, buffersHolders[bufferType.ordinal()]);
    }

    @Override
    public void addDrawCommand(long commandBufferAddress, int count, AbstractBuffersHolder buffersHolder) {
        int offset = buffersHolder.getAndIncrementRenderObjects(count) * COMMAND_SIZE_IN_BYTES;
        MemoryUtil.memCopy(commandBufferAddress, buffersHolder.getCommandBufferAddress() +
                (offset & 0xFFFF_FFFFL), (long) count * COMMAND_SIZE_IN_BYTES);
    }

    @Override
    public void addDrawCommand(int id, BufferType bufferType) {
        addDrawCommand(id, buffersHolders[bufferType.ordinal()]);
    }

    public void addDrawCommand(int id, BuffersHolder buffersHolder) {
        addDrawCommand(id, SIMPLE_QUAD_BASE_VERTEX, buffersHolder);
    }

    public void addDrawCommandCentered(int id, BufferType bufferType) {
        addDrawCommandCentered(id, buffersHolders[bufferType.ordinal()]);
    }

    public void addDrawCommandCentered(int id, BuffersHolder buffersHolder) {
        addDrawCommand(id, 0, buffersHolder);
    }

    @Override
    public void addDrawCommand(int id, int baseVertex, BufferType bufferType) {
        addDrawCommand(id, baseVertex, buffersHolders[bufferType.ordinal()]);
    }

    @Override
    public void addDrawCommand(int id, int baseVertex, AbstractBuffersHolder buffersHolder) {
        int offset = buffersHolder.getAndIncrementRenderObjects() * COMMAND_SIZE_IN_BYTES;
        buffersHolder.putCommandData(offset + BASE_VERTEX_OFFSET, baseVertex);
        buffersHolder.putCommandData(offset + BASE_INSTANCE_OFFSET, id);
    }

    public void setIndexCount(int id, int count, BufferType bufferType) {
        setIndexCount(id, count, buffersHolders[bufferType.ordinal()]);
    }

    @Override
    public void setIndexCount(int id, int count, AbstractBuffersHolder buffersHolder) {
        int offset = id * COMMAND_SIZE_IN_BYTES;
        buffersHolder.putCommandData(offset, count);
    }

    @Override
    public void syncAndRender(BufferType bufferType) {
        syncAndRender(GL_TRIANGLES, bufferType);
    }

    void syncAndRender(int type, BufferType bufferType) {
        BuffersHolder buffersHolder = buffersHolders[bufferType.ordinal()];

        try {
            buffersHolder.getFuture().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        if (buffersHolder.getRenderObjects() > 0) {
            render(type, buffersHolder.getRenderObjects(), buffersHolder);
            buffersHolder.setRenderObjects(0);
        }
    }

    @Override
    public void render(BufferType bufferType) {
        render(GL_TRIANGLES, bufferType);
    }

    private void render(int mode, BufferType bufferType) {
        BuffersHolder buffersHolder = buffersHolders[bufferType.ordinal()];
        if (buffersHolder.getRenderObjects() > 0) {
            if (persistentMappedBuffers) {
                render(mode, buffersHolder.getRenderObjects(), buffersHolder);
            } else {
                updateCommandBufferAndRender(mode, buffersHolder.getRenderObjects(), buffersHolder);
            }
            buffersHolder.setRenderObjects(0);
        }
    }

    @Override
    public void render(int objectCount, AbstractBuffersHolder buffersHolder) {
        if (persistentMappedBuffers) {
            render(GL_TRIANGLES, objectCount, buffersHolder);
        } else {
            updateCommandBufferAndRender(GL_TRIANGLES, objectCount, buffersHolder);
        }
    }

    @Override
    public void updateCommandBufferAndRender(int mode, int objectCount, AbstractBuffersHolder buffersHolder) {
        buffersHolder.updateCommandBuffer(objectCount);
        render(mode, objectCount, buffersHolder);
    }

    @Override
    public void render(int mode, int objectCount, AbstractBuffersHolder buffersHolder) {
        AbstractVAO vao = buffersHolder.getVao();
        vao.bind();

        vao.bindBufferBase(GL_SHADER_STORAGE_BUFFER, SSBO_MODEL_DATA, MODEL_BUFFER_INDEX);
        vao.bindBufferBase(GL_SHADER_STORAGE_BUFFER, SSBO_LAST_UPDATE_MODEL_DATA, LAST_UPDATE_MODEL_BUFFER_INDEX);
        vao.bindBufferBase(GL_SHADER_STORAGE_BUFFER, SSBO_MATERIAL_DATA, MATERIAL_BUFFER_INDEX);
        vao.bindBufferBase(GL_SHADER_STORAGE_BUFFER, SSBO_LAST_UPDATE_MATERIAL_DATA, LAST_UPDATE_MATERIAL_BUFFER_INDEX);
        buffersHolder.bindCommandBuffer();

        glMultiDrawElementsIndirect(mode, GL_UNSIGNED_INT, 0, objectCount, 0);
        renderer.increaseDrawCalls();
        buffersHolder.lockRange();
        buffersHolder.switchRenderingIndex();
    }

    @Override
    public int add(float x, float y, float width, float height, float r, float g, float b, float a, BufferType bufferType) {
        return add(x, y, 0, 1, width, height, r, g, b, a, 0, MaterialType.NOT_TEXTURED, bufferType);
    }

    @Override
    public int add(float x, float y, float width, float height, float r, float g, float b, float a, long textureHandle,
                   BufferType bufferType) {
        return add(x, y, 0, 1, width, height, r, g, b, a, textureHandle, bufferType);
    }

    @Override
    public int add(float x, float y, float width, float height, float r, float g, float b, float a, long textureHandle, float zoomFactor,
                   BufferType bufferType) {
        return add(x, y, 0, 1, width, height, r, g, b, a, textureHandle, zoomFactor, bufferType);
    }

    public int add(float x, float y, float sin, float cos, float width, float height, float r, float g, float b, float a,
                   long textureHandle, float zoomFactor, BufferType bufferType) {
        return add(x, y, sin, cos, width, height, r, g, b, a, textureHandle, MaterialType.TEXTURED, zoomFactor, bufferType);
    }

    private int add(float x, float y, float sin, float cos, float width, float height, float r, float g, float b, float a,
                    long textureHandle, MaterialType materialType, float zoomFactor, BufferType bufferType) {
        return add(x, y, sin, cos, width, height, r, g, b, a, textureHandle, 0, materialType, zoomFactor,
                buffersHolders[bufferType.ordinal()]);
    }

    @Override
    public int add(float x, float y, float sin, float cos, float width, float height, float r, float g, float b, float a,
                   long textureHandle, BufferType bufferType) {
        return add(x, y, sin, cos, width, height, r, g, b, a, textureHandle, 0, MaterialType.TEXTURED,
                buffersHolders[bufferType.ordinal()]);
    }

    @Override
    public int add(float x, float y, float sin, float cos, float width, float height, float r, float g, float b, float a,
                   long textureHandle, MaterialType materialType, BufferType bufferType) {
        return add(x, y, sin, cos, width, height, r, g, b, a, textureHandle, 0, materialType, buffersHolders[bufferType.ordinal()]);
    }

    @Override
    public int add(float x, float y, float sin, float cos, float width, float height, float r, float g, float b, float a,
                   long textureHandle, long maskTextureHandle, BufferType bufferType) {
        return add(x, y, sin, cos, width, height, r, g, b, a, textureHandle, maskTextureHandle, MaterialType.MASKED,
                buffersHolders[bufferType.ordinal()]);
    }

    @Override
    public int add(float x, float y, float sin, float cos, float width, float height, float r, float g, float b, float a,
                   long textureHandle, long maskTextureHandle, MaterialType materialType, BufferType bufferType) {
        return add(x, y, sin, cos, width, height, r, g, b, a, textureHandle, maskTextureHandle, materialType,
                buffersHolders[bufferType.ordinal()]);
    }

    @Override
    public int add(float x, float y, float sin, float cos, float width, float height, float r, float g, float b, float a,
                   long textureHandle, AbstractBuffersHolder buffersHolder) {
        return add(x, y, sin, cos, width, height, r, g, b, a, textureHandle, MaterialType.TEXTURED, buffersHolder);
    }

    @Override
    public int add(float x, float y, float width, float height, float r, float g, float b, float a, long textureHandle,
                   MaterialType materialType, AbstractBuffersHolder buffersHolder) {
        return add(x, y, 0, 1, width, height, r, g, b, a, textureHandle, materialType, buffersHolder);
    }

    public int add(float x, float y, float width, float height, float r, float g, float b, float a, long textureHandle,
                   MaterialType materialType, float zoomFactor, BuffersHolder buffersHolder) {
        return add(x, y, 0, 1, width, height, r, g, b, a, textureHandle, materialType, zoomFactor, buffersHolder);
    }

    private int add(float x, float y, int sin, int cos, float width, float height, float r, float g, float b, float a, long textureHandle,
                    MaterialType materialType, float zoomFactor, BuffersHolder buffersHolder) {
        return add(x, y, sin, cos, width, height, r, g, b, a, textureHandle, 0, materialType, zoomFactor, buffersHolder);
    }

    @Override
    public int add(float x, float y, float sin, float cos, float width, float height, float r, float g, float b, float a,
                   long textureHandle, MaterialType materialType, AbstractBuffersHolder buffersHolder) {
        return add(x, y, sin, cos, width, height, r, g, b, a, textureHandle, 0, materialType, buffersHolder);
    }

    private int add(float x, float y, float sin, float cos, float width, float height, float r, float g, float b, float a,
                    long textureHandle, long maskTextureHandle, MaterialType materialType, AbstractBuffersHolder buffersHolder) {
        return add(x, y, sin, cos, width, height, r, g, b, a, textureHandle, maskTextureHandle, materialType, 1.0f, buffersHolder);
    }

    public int add(float x, float y, float sin, float cos, float width, float height, float r, float g, float b, float a,
                   long textureHandle, long maskTextureHandle, MaterialType materialType, float zoomFactor,
                   AbstractBuffersHolder buffersHolder) {
        int id = buffersHolder.getNextBaseInstanceId();
        int modelDataOffset = id * MODEL_DATA_SIZE;
        int materialOffset = id * MATERIAL_DATA_SIZE_IN_BYTES;
        int lastMaterialOffset = id * LAST_UPDATE_MATERIAL_DATA_SIZE_IN_BYTES;

        addLastUpdateModelData(x, y, sin, cos, width, height, modelDataOffset, buffersHolder);
        addModelData(x, y, sin, cos, width, height, modelDataOffset, buffersHolder);
        addMaterialData(r, g, b, a, textureHandle, maskTextureHandle, materialType, zoomFactor, materialOffset, buffersHolder);
        addLastUpdateMaterialData(r, g, b, a, lastMaterialOffset, buffersHolder);
        buffersHolder.markAllBuffersDirty();

        return id;
    }

    @Override
    public void addModelData(float x, float y, float sin, float cos, float width, float height, int offset,
                             AbstractBuffersHolder buffersHolder) {
        buffersHolder.putModelData(offset, x);
        buffersHolder.putModelData(offset + Y_OFFSET, y);
        buffersHolder.putModelData(offset + SIN_OFFSET, sin);
        buffersHolder.putModelData(offset + COS_OFFSET, cos);
        buffersHolder.putModelData(offset + WIDTH_OFFSET, width);
        buffersHolder.putModelData(offset + HEIGHT_OFFSET, height);
    }

    public void addLastUpdateModelData(float x, float y, float sin, float cos, float width, float height, int offset,
                                       AbstractBuffersHolder buffersHolder) {
        buffersHolder.putLastUpdateModelData(offset, x);
        buffersHolder.putLastUpdateModelData(offset + Y_OFFSET, y);
        buffersHolder.putLastUpdateModelData(offset + SIN_OFFSET, sin);
        buffersHolder.putLastUpdateModelData(offset + COS_OFFSET, cos);
        buffersHolder.putLastUpdateModelData(offset + WIDTH_OFFSET, width);
        buffersHolder.putLastUpdateModelData(offset + HEIGHT_OFFSET, height);
    }

    private void addMaterialData(float r, float g, float b, float a, long textureHandle, long maskTextureHandle, MaterialType materialType,
                                 int offset, float zoomFactor, AbstractBuffersHolder buffersHolder) {
        addMaterialData(r, g, b, a, textureHandle, maskTextureHandle, materialType, zoomFactor, offset, buffersHolder);
    }

    @Override
    public void addMaterialData(float r, float g, float b, float a, long textureHandle, MaterialType materialType, int offset,
                                AbstractBuffersHolder buffersHolder) {
        addMaterialData(r, g, b, a, textureHandle, 0, materialType, offset, buffersHolder);
    }

    private void addMaterialData(float r, float g, float b, float a, long textureHandle, long maskTextureHandle, MaterialType materialType,
                                 float zoomFactor, int offset, AbstractBuffersHolder buffersHolder) {
        addMaterialData(r, g, b, a, textureHandle, maskTextureHandle, materialType, 0.0f, 0.0f, zoomFactor, offset, buffersHolder);
    }

    private void addMaterialData(float r, float g, float b, float a, long textureHandle, long maskTextureHandle, MaterialType materialType,
                                 int offset, AbstractBuffersHolder buffersHolder) {
        addMaterialData(r, g, b, a, textureHandle, maskTextureHandle, materialType, 0.0f, 0.0f, offset, buffersHolder);
    }

    public void addMaterialData(float r, float g, float b, float a, long textureHandle, long maskTextureHandle, MaterialType materialType,
                                float fireAmount, float fireUVAnimation, int offset, AbstractBuffersHolder buffersHolder) {
        addMaterialData(r, g, b, a, textureHandle, maskTextureHandle, materialType, fireAmount, fireUVAnimation, 1.0f, offset,
                buffersHolder);
    }

    public void addMaterialData(float r, float g, float b, float a, long textureHandle, long maskTextureHandle, MaterialType materialType,
                                float fireAmount, float fireUVAnimation, float zoomFactor, int offset,
                                AbstractBuffersHolder buffersHolder) {
        buffersHolder.putMaterialData(offset, r);
        buffersHolder.putMaterialData(offset + COLOR_G_OFFSET, g);
        buffersHolder.putMaterialData(offset + COLOR_B_OFFSET, b);
        buffersHolder.putMaterialData(offset + COLOR_A_OFFSET, a);

        buffersHolder.putMaterialData(offset + TEXTURE_HANDLE_OFFSET, textureHandle);
        buffersHolder.putMaterialData(offset + MASK_TEXTURE_HANDLE_OFFSET, maskTextureHandle);
        buffersHolder.putMaterialData(offset + MATERIAL_TYPE_OFFSET, materialType.ordinal());
        buffersHolder.putMaterialData(offset + FIRE_AMOUNT_OFFSET, fireAmount);
        buffersHolder.putMaterialData(offset + FIRE_UV_ANIMATION_OFFSET, fireUVAnimation);
        buffersHolder.putMaterialData(offset + ZOOM_FACTOR_OFFSET, zoomFactor);
    }

    public void addLastUpdateMaterialData(float r, float g, float b, float a, int offset,
                                          AbstractBuffersHolder buffersHolder) {
        addLastUpdateMaterialData(r, g, b, a, 0.0f, 0.0f, offset, buffersHolder);
    }

    public void addLastUpdateMaterialData(float r, float g, float b, float a, float fireAmount, float fireUVAnimation, int offset,
                                          AbstractBuffersHolder buffersHolder) {
        buffersHolder.putLastUpdateMaterialData(offset, r);
        buffersHolder.putLastUpdateMaterialData(offset + COLOR_G_OFFSET, g);
        buffersHolder.putLastUpdateMaterialData(offset + COLOR_B_OFFSET, b);
        buffersHolder.putLastUpdateMaterialData(offset + COLOR_A_OFFSET, a);

        buffersHolder.putLastUpdateMaterialData(offset + LAST_FIRE_AMOUNT_OFFSET, fireAmount);
        buffersHolder.putLastUpdateMaterialData(offset + LAST_FIRE_UV_ANIMATION_OFFSET, fireUVAnimation);

        //padding
        buffersHolder.putLastUpdateMaterialData(offset + LAST_PADDING_1_OFFSET, 1.0f);
        buffersHolder.putLastUpdateMaterialData(offset + LAST_PADDING_2_OFFSET, 0);
    }

    @Override
    public void setPosition(int id, BufferType bufferType, float x, float y) {
        setPosition(id, buffersHolders[bufferType.ordinal()], x, y);
    }

    @Override
    public void setPosition(int id, AbstractBuffersHolder buffersHolder, float x, float y) {
        int offset = id * MODEL_DATA_SIZE;
        buffersHolder.putModelData(offset, x);
        buffersHolder.putModelData(offset + Y_OFFSET, y);
        buffersHolder.setModelBufferDirty(true);
    }

    public void setX(int id, BufferType bufferType, float x) {
        setX(id, buffersHolders[bufferType.ordinal()], x);
    }

    public void setX(int id, BuffersHolder buffersHolder, float x) {
        buffersHolder.putModelData(id * MODEL_DATA_SIZE, x);
        buffersHolder.setModelBufferDirty(true);
    }

    public void setY(int id, BufferType bufferType, float y) {
        setY(id, buffersHolders[bufferType.ordinal()], y);
    }

    public void setY(int id, BuffersHolder buffersHolder, float y) {
        buffersHolder.putModelData(id * MODEL_DATA_SIZE + Y_OFFSET, y);
        buffersHolder.setModelBufferDirty(true);
    }

    public void setRotation(int id, BufferType bufferType, float rotation) {
        setRotation(id, buffersHolders[bufferType.ordinal()], rotation);
    }

    public void setRotation(int id, AbstractBuffersHolder buffersHolder, float rotation) {
        setRotation(id, buffersHolder, LUT.sin(rotation), LUT.cos(rotation));
    }

    @Override
    public void setRotation(int id, BufferType bufferType, float sin, float cos) {
        setRotation(id, buffersHolders[bufferType.ordinal()], sin, cos);
    }

    @Override
    public void setRotation(int id, AbstractBuffersHolder buffersHolder, float sin, float cos) {
        int offset = id * MODEL_DATA_SIZE;
        buffersHolder.putModelData(offset + SIN_OFFSET, sin);
        buffersHolder.putModelData(offset + COS_OFFSET, cos);
        buffersHolder.setModelBufferDirty(true);
    }

    @Override
    public void setSize(int id, BufferType bufferType, float width, float height) {
        setSize(id, buffersHolders[bufferType.ordinal()], width, height);
    }

    @Override
    public void setSize(int id, AbstractBuffersHolder buffersHolder, float width, float height) {
        int offset = id * MODEL_DATA_SIZE;
        buffersHolder.putModelData(offset + WIDTH_OFFSET, width);
        buffersHolder.putModelData(offset + HEIGHT_OFFSET, height);
        buffersHolder.setModelBufferDirty(true);
    }

    public void setWidth(int id, BufferType bufferType, float width) {
        setWidth(id, buffersHolders[bufferType.ordinal()], width);
    }

    public void setWidth(int id, BuffersHolder buffersHolder, float width) {
        buffersHolder.putModelData(id * MODEL_DATA_SIZE + WIDTH_OFFSET, width);
        buffersHolder.setModelBufferDirty(true);
    }

    public void setHeight(int id, BufferType bufferType, float height) {
        setHeight(id, buffersHolders[bufferType.ordinal()], height);
    }

    public void setHeight(int id, BuffersHolder buffersHolder, float height) {
        buffersHolder.putModelData(id * MODEL_DATA_SIZE + HEIGHT_OFFSET, height);
        buffersHolder.setModelBufferDirty(true);
    }

    @Override
    public void setColor(int id, BufferType bufferType, Vector4f color) {
        setColor(id, buffersHolders[bufferType.ordinal()], color.x, color.y, color.z, color.w);
    }

    public void setColor(int id, BufferType bufferType, float r, float g, float b, float a) {
        setColor(id, buffersHolders[bufferType.ordinal()], r, g, b, a);
    }

    @Override
    public void setColor(int id, AbstractBuffersHolder buffersHolder, Vector4f color) {
        setColor(id, buffersHolder, color.x, color.y, color.z, color.w);
    }

    public void setColor(int id, AbstractBuffersHolder buffersHolder, float r, float g, float b, float a) {
        int offset = id * MATERIAL_DATA_SIZE_IN_BYTES;
        buffersHolder.putMaterialData(offset, r);
        buffersHolder.putMaterialData(offset + COLOR_G_OFFSET, g);
        buffersHolder.putMaterialData(offset + COLOR_B_OFFSET, b);
        buffersHolder.putMaterialData(offset + COLOR_A_OFFSET, a);
        buffersHolder.setMaterialBufferDirty(true);
    }

    @Override
    public void setColorAlpha(int id, BufferType bufferType, float a) {
        setColorAlpha(id, buffersHolders[bufferType.ordinal()], a);
    }

    @Override
    public void setColorAlpha(int id, AbstractBuffersHolder buffersHolder, float a) {
        int offset = id * MATERIAL_DATA_SIZE_IN_BYTES;
        buffersHolder.putMaterialData(offset + COLOR_A_OFFSET, a);
        buffersHolder.setMaterialBufferDirty(true);
    }

    @Override
    public void setTexture(int id, BufferType bufferType, long textureHandle) {
        setTexture(id, buffersHolders[bufferType.ordinal()], textureHandle);
    }

    @Override
    public void setTexture(int id, AbstractBuffersHolder buffersHolder, long textureHandle) {
        int offset = id * MATERIAL_DATA_SIZE_IN_BYTES;
        buffersHolder.putMaterialData(offset + TEXTURE_HANDLE_OFFSET, textureHandle);
        buffersHolder.setMaterialBufferDirty(true);
    }

    @Override
    public void setFireAmount(int id, BufferType bufferType, float value) {
        setFireAmount(id, buffersHolders[bufferType.ordinal()], value);
    }

    public void setFireAmount(int id, BuffersHolder buffersHolder, float value) {
        buffersHolder.putMaterialData(id * MATERIAL_DATA_SIZE_IN_BYTES + FIRE_AMOUNT_OFFSET, value);
        buffersHolder.setMaterialBufferDirty(true);
    }

    @Override
    public void setFireUVAnimation(int id, BufferType bufferType, float value) {
        setFireUVAnimation(id, buffersHolders[bufferType.ordinal()], value);
    }

    public void setFireUVAnimation(int id, BuffersHolder buffersHolder, float value) {
        buffersHolder.putMaterialData(id * MATERIAL_DATA_SIZE_IN_BYTES + FIRE_UV_ANIMATION_OFFSET, value);
        buffersHolder.setMaterialBufferDirty(true);
    }

    @Override
    public void setZoomFactor(int id, BufferType bufferType, float value) {
        setZoomFactor(id, buffersHolders[bufferType.ordinal()], value);
    }

    public void setZoomFactor(int id, BuffersHolder buffersHolder, float value) {
        buffersHolder.putMaterialData(id * MATERIAL_DATA_SIZE_IN_BYTES + ZOOM_FACTOR_OFFSET, value);
        buffersHolder.setMaterialBufferDirty(true);
    }

    @Override
    public void setLastPosition(int id, BufferType bufferType, float x, float y) {
        setLastPosition(id, buffersHolders[bufferType.ordinal()], x, y);
    }

    @Override
    public void setLastPosition(int id, AbstractBuffersHolder buffersHolder, float x, float y) {
        int offset = id * MODEL_DATA_SIZE;
        buffersHolder.putLastUpdateModelData(offset, x);
        buffersHolder.putLastUpdateModelData(offset + Y_OFFSET, y);
        buffersHolder.setLastUpdateModelBufferDirty(true);
    }

    @Override
    public void setLastRotation(int id, BufferType bufferType, float sin, float cos) {
        setLastRotation(id, buffersHolders[bufferType.ordinal()], sin, cos);
    }

    @Override
    public void setLastRotation(int id, AbstractBuffersHolder buffersHolder, float sin, float cos) {
        int offset = id * MODEL_DATA_SIZE;
        buffersHolder.putLastUpdateModelData(offset + SIN_OFFSET, sin);
        buffersHolder.putLastUpdateModelData(offset + COS_OFFSET, cos);
        buffersHolder.setLastUpdateModelBufferDirty(true);
    }

    @Override
    public void setLastSize(int id, BufferType bufferType, float width, float height) {
        setLastSize(id, buffersHolders[bufferType.ordinal()], width, height);
    }

    @Override
    public void setLastSize(int id, AbstractBuffersHolder buffersHolder, float width, float height) {
        int offset = id * MODEL_DATA_SIZE;
        buffersHolder.putLastUpdateModelData(offset + WIDTH_OFFSET, width);
        buffersHolder.putLastUpdateModelData(offset + HEIGHT_OFFSET, height);
        buffersHolder.setLastUpdateModelBufferDirty(true);
    }

    @Override
    public void setLastColor(int id, BufferType bufferType, Vector4f color) {
        setLastColor(id, buffersHolders[bufferType.ordinal()], color);
    }

    @Override
    public void setLastColor(int id, AbstractBuffersHolder buffersHolder, Vector4f color) {
        setLastColor(id, buffersHolder, color.x, color.y, color.z, color.w);
    }

    public void setLastColor(int id, AbstractBuffersHolder buffersHolder, float r, float g, float b, float a) {
        int offset = id * LAST_UPDATE_MATERIAL_DATA_SIZE_IN_BYTES;
        buffersHolder.putLastUpdateMaterialData(offset, r);
        buffersHolder.putLastUpdateMaterialData(offset + COLOR_G_OFFSET, g);
        buffersHolder.putLastUpdateMaterialData(offset + COLOR_B_OFFSET, b);
        buffersHolder.putLastUpdateMaterialData(offset + COLOR_A_OFFSET, a);
        buffersHolder.setLastUpdateMaterialBufferDirty(true);
    }

    @Override
    public void setLastColorAlpha(int id, BufferType bufferType, float a) {
        setLastColorAlpha(id, buffersHolders[bufferType.ordinal()], a);
    }

    @Override
    public void setLastColorAlpha(int id, AbstractBuffersHolder buffersHolder, float a) {
        int offset = id * LAST_UPDATE_MATERIAL_DATA_SIZE_IN_BYTES;
        buffersHolder.putLastUpdateMaterialData(offset + COLOR_A_OFFSET, a);
        buffersHolder.setLastUpdateMaterialBufferDirty(true);
    }

    @Override
    public void setLastFireAmount(int id, BufferType bufferType, float value) {
        setLastFireAmount(id, buffersHolders[bufferType.ordinal()], value);
    }

    public void setLastFireAmount(int id, BuffersHolder buffersHolder, float value) {
        buffersHolder.putLastUpdateMaterialData(id * LAST_UPDATE_MATERIAL_DATA_SIZE_IN_BYTES + LAST_FIRE_AMOUNT_OFFSET, value);
        buffersHolder.setLastUpdateMaterialBufferDirty(true);
    }

    @Override
    public void setLastFireUVAnimation(int id, BufferType bufferType, float value) {
        setLastFireUVAnimation(id, buffersHolders[bufferType.ordinal()], value);
    }

    public void setLastFireUVAnimation(int id, BuffersHolder buffersHolder, float value) {
        buffersHolder.putLastUpdateMaterialData(id * LAST_UPDATE_MATERIAL_DATA_SIZE_IN_BYTES + LAST_FIRE_UV_ANIMATION_OFFSET, value);
        buffersHolder.setLastUpdateMaterialBufferDirty(true);
    }

    @Override
    public BuffersHolder createBuffersHolder(int capacity, boolean persistent) {
        return new BuffersHolder(createVAO(), capacity, persistent);
    }

    @Override
    public BuffersHolder[] createBuffersHolderArray(int length) {
        return new BuffersHolder[length];
    }

    @Override
    public void setPersistentMappedBuffers(boolean value) {
        persistentMappedBuffers = value;

        if (value) {
            for (int i = 0; i < buffersHolders.length; i++) {
                buffersHolders[i].enablePersistentMapping();
            }
        } else {
            for (int i = 0; i < buffersHolders.length; i++) {
                buffersHolders[i].disablePersistentMapping();
            }
        }
    }

    @Override
    public void removeObject(int id, BufferType bufferType) {
        buffersHolders[bufferType.ordinal()].removeObject(id);
    }

    @Override
    public BuffersHolder getBuffersHolder(BufferType bufferType) {
        return buffersHolders[bufferType.ordinal()];
    }

    @Override
    public void clear() {
        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
            }
        }

        for (int i = 0; i < buffersHolders.length; i++) {
            buffersHolders[i].clear();
        }
    }
}
