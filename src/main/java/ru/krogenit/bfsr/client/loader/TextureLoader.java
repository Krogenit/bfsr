package ru.krogenit.bfsr.client.loader;

import org.lwjgl.opengl.EXTTextureFilterAnisotropic;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import ru.krogenit.bfsr.client.texture.Texture;
import ru.krogenit.bfsr.client.texture.TextureRegister;
import ru.krogenit.bfsr.util.PathHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.stb.STBImage.*;

public class TextureLoader {
	private static final Map<TextureRegister, Texture> LOADED_TEXTURES = new HashMap<>();

	public static Texture getTexture(TextureRegister texture, boolean createMips) {
		if(texture == null) return null;
		if (!LOADED_TEXTURES.containsKey(texture)) loadPngTexture(texture, createMips);
		
		return LOADED_TEXTURES.get(texture);
	}
	
	public static Texture getTexture(TextureRegister texture) {
		return getTexture(texture, true);
	}
	
	public static Texture loadDDSTexture(TextureRegister name) {
		if (LOADED_TEXTURES.containsKey(name)) return LOADED_TEXTURES.get(name);

		DDSFile dds = null;

		try {
			InputStream in = new FileInputStream(new File(PathHelper.texture, name.getPath().replace("/", File.separator) + ".png"));
			dds = new DDSFile(in);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (dds == null) throw new RuntimeException("Failed to load a texture file " + name);

		Texture texture = generateDDSTexture(dds);
		LOADED_TEXTURES.put(name, texture);
		return texture;
	}

	public static void loadPngTexture(TextureRegister name, boolean createMips) {
		ByteBuffer image;
		int width, height, channels;
		try (MemoryStack stack = MemoryStack.stackPush()) {
			IntBuffer w = stack.mallocInt(1);
			IntBuffer h = stack.mallocInt(1);
			IntBuffer comp = stack.mallocInt(1);

			stbi_set_flip_vertically_on_load(false);
			image = stbi_load(new File(PathHelper.texture, name.getPath().replace("/", File.separator) + ".png").toString(), w, h, comp, 0);
			if (image == null) {
				throw new RuntimeException("Failed to load a texture file!" + System.lineSeparator() + stbi_failure_reason());
			}

			channels = comp.get();
			width = w.get();
			height = h.get();
		}

		Texture texture = createOpenGLTexture(width, height, image, channels, createMips);
		if(name.getNumberOfRows() != 0) texture.setNumberOfRows(name.getNumberOfRows());
		
		LOADED_TEXTURES.put(name, texture);
	}

	public static Texture generateDDSTexture(DDSFile dds) {
		Texture texture = new Texture(dds.getWidth(), dds.getHeight());
		glBindTexture(GL_TEXTURE_2D, texture.getId());
		glCompressedTexImage2D(GL_TEXTURE_2D, 0, dds.getDXTFormat(), dds.getWidth(), dds.getHeight(), 0, dds.getBuffer());

		if (dds.getMipMapCount() > 0) {
			int maxMipMapLevel = 5;
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, maxMipMapLevel);
			glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_LOD, 0.0F);
			glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAX_LOD, (float) maxMipMapLevel);
			glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_LOD_BIAS, 0.0F);

			int width = dds.getWidth();
			int height = dds.getHeight();
			for (int i = 0; i < maxMipMapLevel; i++) {
				width /= 2;
				height /= 2;
				ByteBuffer mipmapBuffer = dds.getMipMapLevel(i);
				int mipMapLevel = i + 1;
				glCompressedTexImage2D(GL_TEXTURE_2D, mipMapLevel, dds.getDXTFormat(), width, height, 0, mipmapBuffer);
			}
		} else glGenerateMipmap(GL_TEXTURE_2D);

		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		
		if(GL.getCapabilities().GL_EXT_texture_filter_anisotropic) {
			float amount = glGetFloat(EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT);
			glTexParameterf(GL_TEXTURE_2D, EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT, amount);
		}

		return texture;
	}

	public static Texture createOpenGLTexture(int width, int height, ByteBuffer image, int channels, boolean createMips) {
		Texture texture = new Texture(width, height);

		int internalFormat;
		int format;
		switch (channels) {
			case 1: internalFormat = format = GL_R; break;
			case 2: internalFormat = format = GL_RG; break;
			case 3: internalFormat = format = GL_RGB; break;
			case 4: internalFormat = format = GL_RGBA; break;
			default: throw new RuntimeException("Unsupported image channels " + channels);
		}

		glBindTexture(GL_TEXTURE_2D, texture.getId());
		glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, width, height, 0, format, GL_UNSIGNED_BYTE, image);

		if(createMips) glGenerateMipmap(GL_TEXTURE_2D);

		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, createMips ? GL_LINEAR_MIPMAP_LINEAR : GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		
		if(GL.getCapabilities().GL_EXT_texture_filter_anisotropic) {
			float amount = glGetFloat(EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT);
			glTexParameterf(GL_TEXTURE_2D, EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT, amount);
		}

		return texture;
	}
}
