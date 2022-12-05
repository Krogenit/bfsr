package net.bfsr.client.sound;

import net.bfsr.util.IOUtil;
import net.bfsr.util.PathHelper;
import org.lwjgl.openal.AL10;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.stb.STBVorbisInfo;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

public class SoundBuffer {

    private final int bufferId;
    private ShortBuffer pcm;
    private ByteBuffer vorbis;

    public SoundBuffer(String file) throws Exception {
        this.bufferId = AL10.alGenBuffers();
        try (STBVorbisInfo info = STBVorbisInfo.malloc()) {
            ShortBuffer pcm = readVorbis(file, 32 * 1024, info);

            // Copy to buffer
            AL10.alBufferData(bufferId, info.channels() == 1 ? AL10.AL_FORMAT_MONO16 : AL10.AL_FORMAT_STEREO16, pcm, info.sample_rate());
        }
    }

    public int getBufferId() {
        return this.bufferId;
    }

    public void cleanup() {
        AL10.alDeleteBuffers(this.bufferId);
        if (pcm != null) {
            MemoryUtil.memFree(pcm);
        }
    }

    private ShortBuffer readVorbis(String fileName, int bufferSize, STBVorbisInfo info) throws Exception {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            vorbis = IOUtil.ioResourceToByteBuffer(PathHelper.sound + File.separator + fileName + ".ogg", bufferSize);
            IntBuffer error = stack.mallocInt(1);
            long decoder = STBVorbis.stb_vorbis_open_memory(vorbis, error, null);
            if (decoder == MemoryUtil.NULL) {
                throw new RuntimeException("Failed to open Ogg Vorbis file. Error: " + error.get(0));
            }

            STBVorbis.stb_vorbis_get_info(decoder, info);

            int channels = info.channels();

            int lengthSamples = STBVorbis.stb_vorbis_stream_length_in_samples(decoder);

            pcm = MemoryUtil.memAllocShort(lengthSamples);

            pcm.limit(STBVorbis.stb_vorbis_get_samples_short_interleaved(decoder, channels, pcm) * channels);
            STBVorbis.stb_vorbis_close(decoder);

            return pcm;
        }
    }
}
