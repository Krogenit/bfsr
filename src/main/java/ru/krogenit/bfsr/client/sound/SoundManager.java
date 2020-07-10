package ru.krogenit.bfsr.client.sound;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
import ru.krogenit.bfsr.client.camera.Camera;
import ru.krogenit.bfsr.core.Core;
import ru.krogenit.bfsr.math.Transformation;
import ru.krogenit.bfsr.settings.ClientSettings;
import ru.krogenit.bfsr.settings.EnumOption;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.openal.AL10.alDistanceModel;
import static org.lwjgl.openal.ALC10.*;
import static org.lwjgl.system.MemoryUtil.NULL;

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
		this.device = alcOpenDevice((ByteBuffer) null);
		if (device == NULL) {
			throw new IllegalStateException("Failed to open the default OpenAL device.");
		}
		ALCCapabilities deviceCaps = ALC.createCapabilities(device);
		this.context = alcCreateContext(device, (IntBuffer) null);
		if (context == NULL) {
			throw new IllegalStateException("Failed to create OpenAL context.");
		}
		alcMakeContextCurrent(context);
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
		for(int i = 0; i < playingSounds.size();i++) {
			SoundSource source = playingSounds.get(i);
			if(!source.isPlaying()) {
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
		if(lastSoundVolume != soundVolume) {
			listener.setGain(soundVolume);
			lastSoundVolume = soundVolume;
		}
	}

	public void setAttenuationModel(int model) {
		alDistanceModel(model);
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
		if (context != NULL) {
			alcDestroyContext(context);
		}
		if (device != NULL) {
			alcCloseDevice(device);
		}
	}
	
	public static SoundManager getInstance() {
		return instance;
	}
}
