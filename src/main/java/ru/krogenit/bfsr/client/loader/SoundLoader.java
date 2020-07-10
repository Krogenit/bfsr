package ru.krogenit.bfsr.client.loader;

import java.util.HashMap;
import java.util.Map;

import ru.krogenit.bfsr.client.sound.SoundBuffer;
import ru.krogenit.bfsr.client.sound.SoundManager;
import ru.krogenit.bfsr.client.sound.SoundRegistry;

public class SoundLoader {
	
	private static final Map<SoundRegistry, SoundBuffer> LOADED_SOUNDS = new HashMap<>();

	public static void loadSound(SoundRegistry sound) {
//		if(loadedSounds.containsKey(sound)) return;
		
		try {
			SoundManager manager = SoundManager.getInstance();
			SoundBuffer buffBack = new SoundBuffer(sound.getPath());
			manager.addSoundBuffer(buffBack);
			LOADED_SOUNDS.put(sound, buffBack);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static int getBufferId(SoundRegistry soundName) {
		if(!LOADED_SOUNDS.containsKey(soundName)) loadSound(soundName);
		return LOADED_SOUNDS.get(soundName).getBufferId();
	}

//	public static Sound loadSound(String fileName)
//	{
//		stackPush();
//		IntBuffer channelsBuffer = stackMallocInt(1);
//		stackPush();
//		IntBuffer sampleRateBuffer = stackMallocInt(1);
//
//		String path = PathHelper.sound.toString()+File.separator+fileName+".ogg";
//		ShortBuffer rawAudioBuffer = STBVorbis.stb_vorbis_decode_filename(path, channelsBuffer, sampleRateBuffer);
//
//		//Retreive the extra information that was stored in the buffers by the function
//		int channels = channelsBuffer.get();
//		int sampleRate = sampleRateBuffer.get();
//		//Free the space we allocated earlier
//		stackPop();
//		stackPop();
//		
//		//Find the correct OpenAL format
//		int format = -1;
//		if(channels == 1) {
//		    format = AL_FORMAT_MONO16;
//		} else if(channels == 2) {
//		    format = AL_FORMAT_STEREO16;
//		}
//
//		//Request space for the buffer
//		int bufferPointer = alGenBuffers();
//
//		//Send the data to OpenAL
//		alBufferData(bufferPointer, format, rawAudioBuffer, sampleRate);
//
//		//Free the memory allocated by STB
//		free(rawAudioBuffer);
//		
//		//Request a source
//		int sourcePointer = alGenSources();
//
//		//Assign the sound we just loaded to the source
//		alSourcei(sourcePointer, AL_BUFFER, bufferPointer);
//		alSource3f(sourcePointer, AL_POSITION, 0, 0, 0);
//		alSource3f(sourcePointer, AL_VELOCITY, 0, 0, 0);
//		alListener3f(AL_VELOCITY, 0, 0, 0);
//		alListener3f(AL_ORIENTATION, 0, 0, -1f);
//		alListener3f(AL_POSITION, 0, 0, 0);
//		
//		alSourcef(sourcePointer, AL_GAIN, 1);
//		alSourcei(sourcePointer, AL_SOURCE_RELATIVE, AL_FALSE);
//		alSourcei(sourcePointer, AL_LOOPING, AL_TRUE);
//		alSourcef(sourcePointer, AL_PITCH, 1);
//
//		//Play the sound
//		Sound sound = new Sound(bufferPointer, sourcePointer);
//		sound.play();
//		
//		return sound;
//	}

//	public static void dispose()
//	{
//		alDeleteSources(buttonClick.getSourcePointer());
//		alDeleteBuffers(buttonClick.getBufferPointer());
//		alcDestroyContext(context);
//		alcCloseDevice(device);
//	}
}
