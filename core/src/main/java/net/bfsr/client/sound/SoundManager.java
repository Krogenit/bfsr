package net.bfsr.client.sound;

import net.bfsr.client.camera.Camera;
import net.bfsr.core.Core;
import net.bfsr.math.Transformation;
import net.bfsr.settings.ClientSettings;
import net.bfsr.settings.EnumOption;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.openal.*;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class SoundManager {

    private static SoundManager instance;

    private long device;
    private long context;
    private SoundListener listener;

    private final List<SoundBuffer> soundBufferList = new ArrayList<>();
    private final Matrix4f cameraMatrix = new Matrix4f();
    private final List<SoundSource> playingSounds = new ArrayList<>();

    private float lastSoundVolume;
    private final ClientSettings settings = Core.getCore().getSettings();

    public SoundManager() {
        instance = this;
    }

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
    }

    public void play(SoundRegistry sound, boolean loop, boolean relative) {
        SoundSource source = new SoundSource(sound, loop, relative);
        play(source);
    }

    public void play(SoundSource source) {
        source.play();
        this.playingSounds.add(source);
    }

    public void addSoundBuffer(SoundBuffer soundBuffer) {
        this.soundBufferList.add(soundBuffer);
    }

    public SoundListener getListener() {
        return this.listener;
    }

    public void setListener(SoundListener listener) {
        this.listener = listener;
        this.listener.setGain((float) settings.getOptionValue(EnumOption.soundVolume));
        this.listener.setExponentClampedDistanceModel();
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

    public void updateListenerPosition(Camera camera) {
        checkSoundsToClear();
        // Update camera matrix with camera data
        Transformation.updateGenericViewMatrix(camera.getPosition(), camera.getRotation(), cameraMatrix);

        listener.setPosition(camera.getPosition());
        Vector3f at = new Vector3f();
        cameraMatrix.positiveZ(at).negate();
        Vector3f up = new Vector3f();
        cameraMatrix.positiveY(up);
        listener.setOrientation(at, up);

        float soundVolume = (float) settings.getOptionValue(EnumOption.soundVolume);
        if (lastSoundVolume != soundVolume) {
            listener.setGain(soundVolume);
            lastSoundVolume = soundVolume;
        }
    }

    public void setAttenuationModel(int model) {
        AL10.alDistanceModel(model);
    }

    public void cleanup() {
        for (SoundSource soundSource : playingSounds) {
            soundSource.clear();
        }
        playingSounds.clear();
        for (SoundBuffer soundBuffer : soundBufferList) {
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

    public static SoundManager getInstance() {
        return instance;
    }
}
