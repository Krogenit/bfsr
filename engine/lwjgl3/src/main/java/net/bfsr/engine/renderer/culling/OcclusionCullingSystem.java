package net.bfsr.engine.renderer.culling;

import net.bfsr.engine.Engine;
import net.bfsr.engine.renderer.AbstractSpriteRenderer;
import net.bfsr.engine.renderer.buffer.AbstractBuffersHolder;
import net.bfsr.engine.renderer.primitive.AbstractVAO;
import net.bfsr.engine.renderer.primitive.AbstractVBO;
import net.bfsr.engine.renderer.shader.AbstractShaderProgram;
import net.bfsr.engine.renderer.shader.culling.OcclusionCullingShader;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

import static net.bfsr.engine.renderer.AbstractSpriteRenderer.COMMAND_SIZE_IN_BYTES;
import static net.bfsr.engine.renderer.SpriteRenderer.COMMAND_BUFFER_INDEX;
import static net.bfsr.engine.renderer.SpriteRenderer.MATERIAL_BUFFER_INDEX;
import static net.bfsr.engine.renderer.SpriteRenderer.MODEL_BUFFER_INDEX;
import static org.lwjgl.opengl.ARBBufferStorage.GL_DYNAMIC_STORAGE_BIT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL42.glMemoryBarrier;
import static org.lwjgl.opengl.GL43.GL_SHADER_STORAGE_BARRIER_BIT;
import static org.lwjgl.opengl.GL43.GL_SHADER_STORAGE_BUFFER;
import static org.lwjgl.opengl.GL43.glDispatchCompute;

public class OcclusionCullingSystem implements AbstractOcclusionCullingSystem {
    public static final ByteBuffer BYTE_BUFFER = MemoryUtil.memCalloc(4);

    private static final int OCC_CULL_SSBO_MODEL_DATA = 0;
    private static final int OCC_CULL_SSBO_MATERIAL_DATA = 1;
    private static final int OCC_CULL_SSBO_DRAW_COMMANDS = 2;

    private final OcclusionCullingShader occlusionCullingShader = new OcclusionCullingShader();
    private AbstractShaderProgram shader;
    private AbstractSpriteRenderer spriteRenderer;

    @Override
    public void init(AbstractShaderProgram shaderProgram) {
        shader = shaderProgram;
        spriteRenderer = Engine.renderer.spriteRenderer;

        occlusionCullingShader.load();
        occlusionCullingShader.init();
    }

    @Override
    public void renderOcclusionCulled(int count, AbstractBuffersHolder buffersHolder) {
        AbstractVAO vao = buffersHolder.getVao();
        AbstractVBO drawCommandsVBO = vao.getBuffer(COMMAND_BUFFER_INDEX);
        drawCommandsVBO.storeData(buffersHolder.getCommandBufferAddress(), (long) buffersHolder.getCommandBuffer().capacity() << 2, 0L,
                (long) count * COMMAND_SIZE_IN_BYTES, GL_DYNAMIC_STORAGE_BIT);

        testAABB(count, vao.getBuffer(MODEL_BUFFER_INDEX), vao.getBuffer(MATERIAL_BUFFER_INDEX), drawCommandsVBO);

        shader.enable();
        spriteRenderer.render(GL_TRIANGLES, count, buffersHolder);
    }

    private void testAABB(int renderObjects, AbstractVBO modelData, AbstractVBO materialData, AbstractVBO drawCommandsData) {
        occlusionCullingShader.enable();

        modelData.bindBufferBase(GL_SHADER_STORAGE_BUFFER, OCC_CULL_SSBO_MODEL_DATA);
        materialData.bindBufferBase(GL_SHADER_STORAGE_BUFFER, OCC_CULL_SSBO_MATERIAL_DATA);
        drawCommandsData.bindBufferBase(GL_SHADER_STORAGE_BUFFER, OCC_CULL_SSBO_DRAW_COMMANDS);

        /*
         * https://www.khronos.org/opengl/wiki/Memory_Model#Incoherent_memory_access
         * Writes (atomic or otherwise) via Shader Storage Buffer Objects
         */
        glMemoryBarrier(GL_SHADER_STORAGE_BARRIER_BIT);

        glDispatchCompute(renderObjects, 1, 1);
        Engine.renderer.increaseDrawCalls();
    }

    @Override
    public void reloadShaders() {
        occlusionCullingShader.delete();
        occlusionCullingShader.load();
        occlusionCullingShader.init();
    }

    @Override
    public void clear() {
        occlusionCullingShader.delete();
        MemoryUtil.memFree(BYTE_BUFFER);
    }
}
