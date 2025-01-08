package net.bfsr.engine.renderer;

import net.bfsr.engine.Engine;
import net.bfsr.engine.math.LUT;
import net.bfsr.engine.renderer.buffer.AbstractBuffersHolder;
import net.bfsr.engine.renderer.buffer.BufferType;
import net.bfsr.engine.renderer.gui.AbstractGUIRenderer;
import net.bfsr.engine.renderer.primitive.Primitive;
import net.bfsr.engine.renderer.texture.AbstractTexture;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11C;

import java.nio.IntBuffer;

import static net.bfsr.engine.renderer.AbstractSpriteRenderer.LAST_UPDATE_MATERIAL_DATA_SIZE_IN_BYTES;
import static net.bfsr.engine.renderer.SpriteRenderer.COLOR_A_OFFSET;
import static net.bfsr.engine.renderer.SpriteRenderer.COLOR_B_OFFSET;
import static net.bfsr.engine.renderer.SpriteRenderer.COLOR_G_OFFSET;
import static net.bfsr.engine.renderer.SpriteRenderer.COS_OFFSET;
import static net.bfsr.engine.renderer.SpriteRenderer.HEIGHT_OFFSET;
import static net.bfsr.engine.renderer.SpriteRenderer.MATERIAL_DATA_SIZE_IN_BYTES;
import static net.bfsr.engine.renderer.SpriteRenderer.MODEL_DATA_SIZE;
import static net.bfsr.engine.renderer.SpriteRenderer.SIN_OFFSET;
import static net.bfsr.engine.renderer.SpriteRenderer.WIDTH_OFFSET;

public class GuiRenderer extends AbstractGUIRenderer {
    private AbstractSpriteRenderer spriteRenderer;
    private AbstractBuffersHolder buffersHolder;

    @Override
    public void init() {
        spriteRenderer = Engine.renderer.spriteRenderer;
        buffersHolder = spriteRenderer.getBuffersHolder(BufferType.GUI);
    }

    @Override
    public void addPrimitive(Primitive primitive) {
        spriteRenderer.addPrimitive(primitive);
    }

    @Override
    public void render() {
        render(GL11C.GL_TRIANGLES);
    }

    @Override
    public void render(int mode) {
        if (buffersHolder.getRenderObjects() > 0) {
            spriteRenderer.render(mode, buffersHolder.getRenderObjects(), buffersHolder);
            buffersHolder.setRenderObjects(0);
        }
    }

    @Override
    public void addDrawCommand(IntBuffer commandBuffer, int count) {
        spriteRenderer.addDrawCommand(commandBuffer, count, buffersHolder);
    }

    @Override
    public void addDrawCommand(int id) {
        addDrawCommand(id, AbstractSpriteRenderer.SIMPLE_QUAD_BASE_VERTEX);
    }

    @Override
    public void addDrawCommand(int id, int baseVertex) {
        spriteRenderer.addDrawCommand(id, baseVertex, buffersHolder);
    }

    @Override
    public void setIndexCount(int id, int count) {
        spriteRenderer.setIndexCount(id, count, buffersHolder);
    }

    @Override
    public int add(int x, int y, int width, int height, Vector4f color) {
        return add(x, y, width, height, color.x, color.y, color.z, color.w, 0);
    }

    @Override
    public int add(int x, int y, int width, int height, float r, float g, float b, float a) {
        return add(x, y, width, height, r, g, b, a, 0);
    }

    @Override
    public int add(int x, int y, float sin, float cos, int width, int height, float r, float g, float b, float a) {
        return add(x, y, sin, cos, width, height, r, g, b, a, 0, 0);
    }

    @Override
    public void add(int x, int y, int width, int height, float r, float g, float b, float a, AbstractTexture texture) {
        add(x, y, width, height, r, g, b, a, texture.getTextureHandle());
    }

    @Override
    public int add(int x, int y, int width, int height, Vector4f color, AbstractTexture texture) {
        return add(x, y, width, height, color.x, color.y, color.z, color.w, texture.getTextureHandle());
    }

    @Override
    public int add(int x, int y, int width, int height, float r, float g, float b, float a, long textureHandle) {
        return add(x, y, 0, 1, width, height, r, g, b, a, textureHandle, 0);
    }

    @Override
    public int add(int x, int y, float sin, float cos, int width, int height, float r, float g, float b, float a,
                   AbstractTexture texture) {
        return add(x, y, sin, cos, width, height, r, g, b, a, texture.getTextureHandle(), 0);
    }

    @Override
    public int add(int x, int y, float sin, float cos, int width, int height, float r, float g, float b, float a,
                   long textureHandle, int font) {
        return spriteRenderer.add(x, y, sin, cos, width, height, r, g, b, a, textureHandle, font, buffersHolder);
    }

    @Override
    public int addCentered(int x, int y, int width, int height, Vector4f color) {
        return addCentered(x, y, width, height, color.x, color.y, color.z, color.w);
    }

    @Override
    public int addCentered(int x, int y, int width, int height, float r, float g, float b, float a) {
        return addCentered(x, y, 0, 1, width, height, r, g, b, a, 0);
    }

    public int addCentered(int x, int y, float rotation, int width, int height, Vector4f color, AbstractTexture texture) {
        return addCentered(x, y, LUT.sin(rotation), LUT.cos(rotation), width, height, color, texture);
    }

    @Override
    public int addCentered(int x, int y, float sin, float cos, int width, int height, Vector4f color, AbstractTexture texture) {
        return addCentered(x, y, sin, cos, width, height, color.x, color.y, color.z, color.w, texture.getTextureHandle());
    }

    @Override
    public int addCentered(int x, int y, float sin, float cos, int width, int height, float r, float g, float b, float a,
                           long textureHandle) {
        return spriteRenderer.add(x + width * 0.5f, y + height * 0.5f, sin, cos, width, height, r, g, b, a, textureHandle, 0,
                buffersHolder);
    }

    @Override
    public void setPosition(int id, int x, int y) {
        setPosition(id, (float) x, (float) y);
    }

    @Override
    public void setPosition(int id, float x, float y) {
        int offset = id * MODEL_DATA_SIZE;
        buffersHolder.putModelData(offset, x);
        buffersHolder.putModelData(offset + SpriteRenderer.Y_OFFSET, y);
        buffersHolder.setModelBufferDirty(true);
    }

    @Override
    public void setX(int id, int x) {
        buffersHolder.putModelData(id * MODEL_DATA_SIZE, x);
        buffersHolder.setModelBufferDirty(true);
    }

    @Override
    public void setY(int id, int y) {
        buffersHolder.putModelData(id * MODEL_DATA_SIZE + SpriteRenderer.Y_OFFSET, y);
        buffersHolder.setModelBufferDirty(true);
    }

    @Override
    public void setRotation(int id, float rotation) {
        setRotation(id, LUT.sin(rotation), LUT.cos(rotation));
    }

    @Override
    public void setRotation(int id, float sin, float cos) {
        buffersHolder.putModelData(id * MODEL_DATA_SIZE + SpriteRenderer.SIN_OFFSET, sin);
        buffersHolder.putModelData(id * MODEL_DATA_SIZE + SpriteRenderer.COS_OFFSET, cos);
        buffersHolder.setModelBufferDirty(true);
    }

    @Override
    public void setSize(int id, int width, int height) {
        int offset = id * MODEL_DATA_SIZE;
        buffersHolder.putModelData(offset + SpriteRenderer.WIDTH_OFFSET, width);
        buffersHolder.putModelData(offset + SpriteRenderer.HEIGHT_OFFSET, height);
        buffersHolder.setModelBufferDirty(true);
    }

    @Override
    public void setWidth(int id, int width) {
        buffersHolder.putModelData(id * MODEL_DATA_SIZE + SpriteRenderer.WIDTH_OFFSET, width);
        buffersHolder.setModelBufferDirty(true);
    }

    @Override
    public void setHeight(int id, int height) {
        buffersHolder.putModelData(id * MODEL_DATA_SIZE + SpriteRenderer.HEIGHT_OFFSET, height);
        buffersHolder.setModelBufferDirty(true);
    }

    @Override
    public void setColor(int id, Vector4f color) {
        setColor(id, color.x, color.y, color.z, color.w);
    }

    @Override
    public void setColor(int id, float r, float g, float b, float a) {
        int offset = id * MATERIAL_DATA_SIZE_IN_BYTES;
        buffersHolder.putMaterialData(offset, r);
        buffersHolder.putMaterialData(offset + SpriteRenderer.COLOR_G_OFFSET, g);
        buffersHolder.putMaterialData(offset + SpriteRenderer.COLOR_B_OFFSET, b);
        buffersHolder.putMaterialData(offset + SpriteRenderer.COLOR_A_OFFSET, a);
        buffersHolder.setMaterialBufferDirty(true);
    }

    @Override
    public void setTexture(int id, long textureHandle) {
        buffersHolder.putMaterialData(id * MATERIAL_DATA_SIZE_IN_BYTES + SpriteRenderer.TEXTURE_HANDLE_OFFSET, textureHandle);
        buffersHolder.setMaterialBufferDirty(true);
    }

    @Override
    public void setLastPosition(int id, int x, int y) {
        setLastPosition(id, (float) x, (float) y);
    }

    @Override
    public void setLastPosition(int id, float x, float y) {
        int offset = id * MODEL_DATA_SIZE;
        buffersHolder.putLastUpdateModelData(offset, x);
        buffersHolder.putLastUpdateModelData(offset + SpriteRenderer.Y_OFFSET, y);
        buffersHolder.setLastUpdateModelBufferDirty(true);
    }

    @Override
    public void setLastRotation(int id, float rotation) {
        setLastRotation(id, LUT.sin(rotation), LUT.cos(rotation));
    }

    @Override
    public void setLastRotation(int id, float sin, float cos) {
        int offset = id * MODEL_DATA_SIZE;
        buffersHolder.putLastUpdateModelData(offset + SIN_OFFSET, sin);
        buffersHolder.putLastUpdateModelData(offset + COS_OFFSET, cos);
        buffersHolder.setLastUpdateModelBufferDirty(true);
    }

    @Override
    public void setLastSize(int id, int width, int height) {
        int offset = id * MODEL_DATA_SIZE;
        buffersHolder.putLastUpdateModelData(offset + WIDTH_OFFSET, width);
        buffersHolder.putLastUpdateModelData(offset + HEIGHT_OFFSET, height);
        buffersHolder.setLastUpdateModelBufferDirty(true);
    }

    @Override
    public void setLastColor(int id, Vector4f color) {
        setLastColor(id, color.x, color.y, color.z, color.w);
    }

    @Override
    public void setLastColor(int id, float r, float g, float b, float a) {
        int offset = id * LAST_UPDATE_MATERIAL_DATA_SIZE_IN_BYTES;
        buffersHolder.putLastUpdateMaterialData(offset, r);
        buffersHolder.putLastUpdateMaterialData(offset + COLOR_G_OFFSET, g);
        buffersHolder.putLastUpdateMaterialData(offset + COLOR_B_OFFSET, b);
        buffersHolder.putLastUpdateMaterialData(offset + COLOR_A_OFFSET, a);
        buffersHolder.setLastUpdateMaterialBufferDirty(true);
    }

    @Override
    public int getRenderObjectsCount() {
        return buffersHolder.getRenderObjects();
    }

    @Override
    public void removeObject(int id) {
        removeObject(id, BufferType.GUI);
    }

    @Override
    public void removeObject(int id, BufferType bufferType) {
        spriteRenderer.removeObject(id, bufferType);
    }
}
