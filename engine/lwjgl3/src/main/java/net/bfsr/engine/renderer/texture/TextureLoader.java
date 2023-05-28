package net.bfsr.engine.renderer.texture;

import gnu.trove.map.TMap;
import gnu.trove.map.hash.THashMap;
import lombok.extern.log4j.Log4j2;
import net.bfsr.engine.renderer.texture.dds.DDSFile;
import org.lwjgl.opengl.*;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;

@Log4j2
public final class TextureLoader extends AbstractTextureLoader {
    private static final TMap<String, Texture> LOADED_TEXTURES = new THashMap<>();
    private static final int DEFAULT_WRAP = GL12C.GL_CLAMP_TO_EDGE;
    private static final int DEFAULT_FILTER = GL11.GL_NEAREST;

    @Override
    public void init() {
        createDummyTexture();
    }

    private void createDummyTexture() {
        int width = 4;
        int height = 4;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer byteBuffer = stack.malloc(width * height * 4);
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    byteBuffer.put((byte) 32);
                    byteBuffer.put((byte) 32);
                    byteBuffer.put((byte) 32);
                }
            }

            dummyTexture = createTexture(width, height, byteBuffer.flip(), 3, false);
        }
    }

    @Override
    public Texture getTexture(TextureRegister texture) {
        return getTexture(texture.getPath(), DEFAULT_WRAP, DEFAULT_FILTER);
    }

    @Override
    public Texture getTexture(TextureRegister texture, int wrap, int filter) {
        return getTexture(texture.getPath(), wrap, filter);
    }

    @Override
    public Texture getTexture(Path path) {
        return getTexture(path, true, DEFAULT_WRAP, DEFAULT_FILTER);
    }

    public Texture getTexture(Path path, int wrap, int filter) {
        return getTexture(path, true, wrap, filter);
    }

    public Texture getTexture(TextureRegister texture, boolean createMips, int wrap, int filter) {
        return getTexture(texture.getPath(), createMips, wrap, filter);
    }

    public Texture getTexture(Path path, boolean createMips, int wrap, int filter) {
        return LOADED_TEXTURES.computeIfAbsent(path.toString(), s -> loadPngTexture(path, createMips, wrap, filter));
    }

    private void loadDDSTexture(String path) {
        if (LOADED_TEXTURES.containsKey(path)) {
            LOADED_TEXTURES.get(path);
            return;
        }

        DDSFile dds = null;

        try {
            InputStream in = new FileInputStream(path);
            dds = new DDSFile(in);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (dds == null) throw new RuntimeException("Failed to load a texture file " + path);

        Texture texture = generateTexture(dds);
        LOADED_TEXTURES.put(path, texture);
    }

    private Texture loadPngTexture(Path path, boolean createMips, int wrap, int filter) {
        ByteBuffer image;
        int width, height, channels;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer comp = stack.mallocInt(1);

            STBImage.stbi_set_flip_vertically_on_load(false);
            image = STBImage.stbi_load(path.toString(), w, h, comp, 0);
            if (image == null) {
                log.error("Failed to load a texture file {}!{}{}", path, System.lineSeparator(), STBImage.stbi_failure_reason());
                return null;
            }

            channels = comp.get();
            width = w.get();
            height = h.get();
        }

        return createTexture(width, height, image, channels, createMips, wrap, filter);
    }

    private Texture generateTexture(DDSFile dds) {
        Texture texture = new Texture(dds.getWidth(), dds.getHeight()).create();

        GL45C.glTextureStorage2D(texture.getId(), dds.getMipMapCount() + 1, dds.getDXTFormat(), dds.getWidth(), dds.getHeight());
        GL45C.glCompressedTextureSubImage2D(texture.getId(), 0, 0, 0, dds.getWidth(), dds.getHeight(), dds.getDXTFormat(), dds.getBuffer());
        int mipMapCount = dds.getMipMapCount();
        int textureMagFilter = GL11C.GL_LINEAR;
        int textureMinFilter = (mipMapCount > 0) ? GL11C.GL_LINEAR_MIPMAP_LINEAR : GL11C.GL_LINEAR;
        if (mipMapCount > 0) {
            GL45C.glTextureParameteri(texture.getId(), GL12C.GL_TEXTURE_MAX_LEVEL, mipMapCount);
            GL45C.glTextureParameterf(texture.getId(), GL12C.GL_TEXTURE_MIN_LOD, 0.0f);
            GL45C.glTextureParameterf(texture.getId(), GL12C.GL_TEXTURE_MAX_LOD, mipMapCount);
            GL45C.glTextureParameterf(texture.getId(), GL14C.GL_TEXTURE_LOD_BIAS, 0.0f);

            int width = dds.getWidth();
            int height = dds.getHeight();
            for (int i = 0; i < mipMapCount; i++) {
                width /= 2;
                height /= 2;
                ByteBuffer mipmapBuffer = dds.getMipMapLevel(i);
                GL45C.glCompressedTextureSubImage2D(texture.getId(), i + 1, 0, 0, width, height, dds.getDXTFormat(), mipmapBuffer);
            }
        }

        GL45C.glTextureParameteri(texture.getId(), GL11C.GL_TEXTURE_WRAP_S, GL11C.GL_REPEAT);
        GL45C.glTextureParameteri(texture.getId(), GL11C.GL_TEXTURE_WRAP_T, GL11C.GL_REPEAT);
        GL45C.glTextureParameteri(texture.getId(), GL11C.GL_TEXTURE_MAG_FILTER, textureMagFilter);
        GL45C.glTextureParameteri(texture.getId(), GL11C.GL_TEXTURE_MIN_FILTER, textureMinFilter);
        GL45C.glTextureParameterf(texture.getId(), EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT, GL11C.glGetFloat(EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT));

        return texture;
    }

    private Texture createTexture(int width, int height, ByteBuffer image, int channels, boolean createMips) {
        return createTexture(width, height, image, channels, createMips, DEFAULT_WRAP, DEFAULT_FILTER);
    }

    private Texture createTexture(int width, int height, ByteBuffer image, int channels, boolean createMips, int wrap, int filter) {
        Texture texture = new Texture(width, height).create();

        int internalFormat;
        int format;
        if (channels == 1) {
            internalFormat = GL30.GL_R8;
            format = GL11.GL_R;
        } else if (channels == 2) {
            internalFormat = GL30.GL_RG8;
            format = GL30.GL_RG;
        } else if (channels == 3) {
            internalFormat = GL11.GL_RGB8;
            format = GL11.GL_RGB;
        } else if (channels == 4) {
            internalFormat = GL11.GL_RGBA8;
            format = GL11.GL_RGBA;
        } else {
            throw new RuntimeException("Unsupported image channels " + channels);
        }

        GL45C.glTextureStorage2D(texture.getId(), 1, internalFormat, width, height);
        GL45C.glTextureSubImage2D(texture.getId(), 0, 0, 0, width, height, format, GL11.GL_UNSIGNED_BYTE, image);

        if (createMips) GL45C.glGenerateTextureMipmap(texture.getId());

        GL45C.glTextureParameteri(texture.getId(), GL11.GL_TEXTURE_WRAP_S, wrap);
        GL45C.glTextureParameteri(texture.getId(), GL11.GL_TEXTURE_WRAP_T, wrap);
        GL45C.glTextureParameteri(texture.getId(), GL11.GL_TEXTURE_MIN_FILTER, createMips ? (filter == GL11.GL_NEAREST ? GL11.GL_NEAREST_MIPMAP_NEAREST :
                GL11.GL_LINEAR_MIPMAP_LINEAR) : filter);
        GL45C.glTextureParameteri(texture.getId(), GL11.GL_TEXTURE_MAG_FILTER, filter);
        GL45C.glTextureParameterf(texture.getId(), EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT,
                GL11.glGetFloat(EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT));
        long textureHandle = ARBBindlessTexture.glGetTextureHandleARB(texture.getId());
        ARBBindlessTexture.glMakeTextureHandleResidentARB(textureHandle);
        texture.setTextureHandle(textureHandle);

        return texture;
    }

    @Override
    public Texture createTexture(int width, int height) {
        return newTexture(width, height).create();
    }

    @Override
    public Texture newTexture(int width, int height) {
        return new Texture(width, height);
    }
}