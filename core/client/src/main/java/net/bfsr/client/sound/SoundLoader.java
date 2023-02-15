package net.bfsr.client.sound;

import lombok.extern.log4j.Log4j2;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.EnumMap;

@Log4j2
final class SoundLoader {
    private static final EnumMap<SoundRegistry, SoundBuffer> LOADED_SOUNDS = new EnumMap<>(SoundRegistry.class);

    @Nullable
    private static SoundBuffer loadSound(SoundRegistry sound) {
        try {
            SoundManager manager = SoundManager.getInstance();
            SoundBuffer soundBuffer = new SoundBuffer(sound.getPath());
            manager.addSoundBuffer(soundBuffer);
            return soundBuffer;
        } catch (IOException e) {
            log.error("Can't load sound {}", sound.getPath(), e);
            return null;
        }
    }

    static int getBufferId(SoundRegistry soundName) {
        return LOADED_SOUNDS.computeIfAbsent(soundName, soundRegistry -> loadSound(soundName)).getBufferId();
    }
}
