package net.bfsr.engine.sound;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.openal.*;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class SoundManager extends AbstractSoundManager {
    private long device;
    private long context;
    private AbstractSoundListener listener;

    private final List<AbstractSoundBuffer> soundBufferList = new ArrayList<>();
    private final Matrix4f cameraMatrix = new Matrix4f();
    private final List<SoundSource> playingSounds = new ArrayList<>();

    private float lastSoundVolume;

    @Override
    public void init() {
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

        setAttenuationModel(AL11.AL_EXPONENT_DISTANCE);
        setListener(new SoundListener(new Vector3f(0, 0, 0)));
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
    public SoundSource play(AbstractSoundBuffer soundBuffer, float volume, float x, float y) {
        SoundSource soundSource = new SoundSource(soundBuffer, volume, x, y);
        play(soundSource);
        return soundSource;
    }

    @Override
    public SoundSource play(SoundRegistry sound) {
        SoundSource soundSource = new SoundSource(sound);
        play(soundSource);
        return soundSource;
    }

    private void play(SoundSource source) {
        AL10.alSourcePlay(source.getSource());
        playingSounds.add(source);
    }

    @Override
    public void addSoundBuffer(AbstractSoundBuffer soundBuffer) {
        this.soundBufferList.add(soundBuffer);
    }

    @Override
    public void setListener(AbstractSoundListener listener) {
        this.listener = listener;
        this.listener.setExponentClampedDistanceModel();

        Vector3f at = new Vector3f();
        cameraMatrix.positiveZ(at).negate();
        Vector3f up = new Vector3f();
        cameraMatrix.positiveY(up);
        this.listener.setOrientation(at, up);
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
    public void updateListenerPosition(Vector2f position) {
        checkSoundsToClear();
        listener.setPosition(position);
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
    public void setPosition(int source, float x, float y) {
        AL10.alSource3f(source, AL10.AL_POSITION, x, y, 0);
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
    public void setAttenuationModel(int model) {
        AL10.alDistanceModel(model);
    }

    @Override
    public boolean isPlaying(int source) {
        return AL10.alGetSourcei(source, AL10.AL_SOURCE_STATE) == AL10.AL_PLAYING;
    }

    @Override
    public void cleanup() {
        int size = playingSounds.size();
        for (int i = 0; i < size; i++) {
            SoundSource soundSource = playingSounds.get(i);
            soundSource.clear();
        }
        playingSounds.clear();
        size = soundBufferList.size();
        for (int i = 0; i < size; i++) {
            AbstractSoundBuffer soundBuffer = soundBufferList.get(i);
            soundBuffer.cleanup();
        }
        soundBufferList.clear();
        if (context != MemoryUtil.NULL) {
            ALC10.alcDestroyContext(context);
        }
        if (device != MemoryUtil.NULL) {
            ALC10.alcCloseDevice(device);
        }
    }
}