package net.bfsr.engine.sound;

import it.unimi.dsi.util.XoRoShiRo128PlusRandom;
import net.bfsr.engine.util.RandomHelper;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class SoundManager extends AbstractSoundManager {
    private final long device;
    private final long context;
    private final AbstractSoundListener listener;

    private final List<SoundSource> playingSounds = new ArrayList<>();

    private float lastSoundVolume;

    private final XoRoShiRo128PlusRandom random = new XoRoShiRo128PlusRandom();

    public SoundManager() {
        this.device = ALC10.alcOpenDevice((ByteBuffer) null);
        if (device == MemoryUtil.NULL) {
            throw new IllegalStateException("Failed to open the default OpenAL device.");
        }
        ALCCapabilities deviceCaps = ALC.createCapabilities(device);
        this.context = ALC10.alcCreateContext(device, (IntBuffer) null);
        if (context == MemoryUtil.NULL) {
            throw new IllegalStateException("Failed to create OpenAL context.");
        }
        ALC10.alcMakeContextCurrent(context);
        AL.createCapabilities(deviceCaps);

        SoundListener soundListener = new SoundListener(new Vector3f(0, 0, 0));
        soundListener.setExponentClampedDistanceModel();
        Vector3f at = new Vector3f();
        Matrix4f cameraMatrix = new Matrix4f();
        cameraMatrix.positiveZ(at).negate();
        Vector3f up = new Vector3f();
        cameraMatrix.positiveY(up);
        soundListener.setOrientation(at, up);
        listener = soundListener;
    }

    @Override
    public int createSoundSource(int bufferId, boolean loop, boolean relative) {
        int source = AL10.alGenSources();

        if (loop) {
            AL10.alSourcei(source, AL10.AL_LOOPING, AL10.AL_TRUE);
        }

        if (relative) {
            AL10.alSourcei(source, AL10.AL_SOURCE_RELATIVE, AL10.AL_TRUE);
        }

        AL10.alSourcei(source, AL10.AL_BUFFER, bufferId);

        return source;
    }

    @Override
    public SoundSource play(int soundBuffer, float volume) {
        Vector3f position = listener.getPosition();
        SoundSource soundSource = new SoundSource(soundBuffer, volume, position.x, position.y, position.z, 1.0f);
        play(soundSource);
        return soundSource;
    }

    @Override
    public SoundSource play(int soundBuffer, float volume, float x, float y) {
        return play(soundBuffer, volume, x, y, 1.0f);
    }

    @Override
    public SoundSource play(int soundBuffer, float volume, float x, float y, float pitch) {
        SoundSource soundSource = new SoundSource(soundBuffer, volume, x, y, 0.0f, pitch);
        play(soundSource);
        return soundSource;
    }

    private void play(Sound sound, float x, float y) {
        float minRandomPitch = sound.minPitch();
        float maxRandomPitch = sound.maxPitch();
        if (minRandomPitch != maxRandomPitch) {
            float pitch = RandomHelper.randomFloat(random, minRandomPitch, maxRandomPitch);
            play(sound.soundBuffer(), sound.volume(), x, y, pitch);
        } else {
            play(sound.soundBuffer(), sound.volume(), x, y, maxRandomPitch);
        }
    }

    @Override
    public void play(SoundEffect soundEffect, float x, float y) {
        List<Sound> soundEffectsList = soundEffect.getSounds();
        if (soundEffect.isRandomFromList()) {
            play(soundEffectsList.get(random.nextInt(soundEffectsList.size())), x, y);
        } else {
            for (int i = 0; i < soundEffectsList.size(); i++) {
                play(soundEffectsList.get(i), x, y);
            }
        }
    }

    private void play(SoundSource source) {
        AL10.alSourcePlay(source.getSource());
        playingSounds.add(source);
    }

    @Override
    public void setGain(float value) {
        listener.setGain(value);
    }

    private void checkSoundsToClear() {
        for (int i = 0; i < playingSounds.size(); i++) {
            SoundSource source = playingSounds.get(i);
            if (!source.isPlaying()) {
                source.clear();
                playingSounds.remove(i);
                i--;
            }
        }
    }

    @Override
    public void updateListenerPosition(float x, float y, float z) {
        checkSoundsToClear();
        listener.setPosition(x, y, z);
    }

    @Override
    public void updateGain(float value) {
        if (lastSoundVolume != value) {
            listener.setGain(value);
            lastSoundVolume = value;
        }
    }

    @Override
    public void pause(int source) {
        AL10.alSourcePause(source);
    }

    @Override
    public void stop(int source) {
        AL10.alSourceStop(source);
    }

    @Override
    public void delete(int source) {
        AL10.alDeleteSources(source);
    }

    @Override
    public void setPosition(int source, float x, float y, float z) {
        AL10.alSource3f(source, AL10.AL_POSITION, x, y, z);
    }

    @Override
    public void setGain(int source, float gain) {
        AL10.alSourcef(source, AL10.AL_GAIN, gain);
    }

    @Override
    public void setRollOffFactor(int source, float value) {
        AL10.alSourcef(source, AL10.AL_ROLLOFF_FACTOR, value);
    }

    @Override
    public void setReferenceDistance(int source, float value) {
        AL10.alSourcef(source, AL10.AL_REFERENCE_DISTANCE, value);
    }

    @Override
    public void setMaxDistance(int source, float value) {
        AL10.alSourcef(source, AL10.AL_MAX_DISTANCE, value);
    }

    @Override
    public void setPitch(int source, float value) {
        AL10.alSourcef(source, AL10.AL_PITCH, value);
    }

    @Override
    public boolean isPlaying(int source) {
        return AL10.alGetSourcei(source, AL10.AL_SOURCE_STATE) == AL10.AL_PLAYING;
    }

    @Override
    public void clear() {
        for (int i = 0, size = playingSounds.size(); i < size; i++) {
            SoundSource soundSource = playingSounds.get(i);
            soundSource.clear();
        }

        playingSounds.clear();

        if (context != MemoryUtil.NULL) {
            ALC10.alcDestroyContext(context);
        }

        if (device != MemoryUtil.NULL) {
            ALC10.alcCloseDevice(device);
        }
    }
}