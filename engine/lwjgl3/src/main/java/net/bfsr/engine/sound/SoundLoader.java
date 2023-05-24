package net.bfsr.engine.sound;

import gnu.trove.map.TMap;
import gnu.trove.map.hash.THashMap;
import lombok.extern.log4j.Log4j2;
import net.bfsr.engine.Engine;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.stb.STBVorbisInfo;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;

@Log4j2
public final class SoundLoader extends AbstractSoundLoader {
    private final TMap<String, AbstractSoundBuffer> loadedSounds = new THashMap<>();

    private SoundBuffer loadSound(Path path) {
        try (STBVorbisInfo info = STBVorbisInfo.malloc()) {
            ShortBuffer pcm = readVorbis(path, info);

            int bufferId = AL10.alGenBuffers();
            AL10.alBufferData(bufferId, info.channels() == 1 ? AL10.AL_FORMAT_MONO16 : AL10.AL_FORMAT_STEREO16, pcm, info.sample_rate());
            MemoryUtil.memFree(pcm);
            SoundBuffer soundBuffer = new SoundBuffer(bufferId);
            Engine.soundManager.addSoundBuffer(soundBuffer);
            return soundBuffer;
        } catch (IOException e) {
            log.error("Can't load sound {}", path, e);
            throw new RuntimeException(e);
        }
    }

    private ShortBuffer readVorbis(Path path, STBVorbisInfo info) throws IOException {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer vorbis = resourceToByteBuffer(path);
            IntBuffer error = stack.mallocInt(1);
            long decoder = STBVorbis.stb_vorbis_open_memory(vorbis, error, null);
            if (decoder == MemoryUtil.NULL) {
                throw new IOException("Failed to open Ogg Vorbis file. Error: " + error.get(0));
            }

            STBVorbis.stb_vorbis_get_info(decoder, info);
            int channels = info.channels();
            int lengthSamples = STBVorbis.stb_vorbis_stream_length_in_samples(decoder);
            ShortBuffer pcm = MemoryUtil.memAllocShort(lengthSamples);
            pcm.limit(STBVorbis.stb_vorbis_get_samples_short_interleaved(decoder, channels, pcm) * channels);
            STBVorbis.stb_vorbis_close(decoder);
            return pcm;
        }
    }

    private ByteBuffer resourceToByteBuffer(Path path) throws IOException {
        try (SeekableByteChannel fc = Files.newByteChannel(path)) {
            ByteBuffer buffer = BufferUtils.createByteBuffer((int) fc.size() + 1);
            while (true) {
                if (fc.read(buffer) == -1) break;
            }

            return buffer.flip();
        }
    }

    @Override
    public AbstractSoundBuffer getBuffer(SoundRegistry sound) {
        return getBuffer(sound.getPath());
    }

    @Override
    public AbstractSoundBuffer getBuffer(Path path) {
        return loadedSounds.computeIfAbsent(path.toString(), soundRegistry -> loadSound(path));
    }
}