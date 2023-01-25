package net.bfsr.client.sound;

import lombok.extern.log4j.Log4j2;

import java.util.EnumMap;

@Log4j2
final class SoundLoader {
    private static final EnumMap<SoundRegistry, SoundBuffer> LOADED_SOUNDS = new EnumMap<>(SoundRegistry.class);

    private static void loadSound(SoundRegistry sound) {
        try {
            SoundManager manager = SoundManager.getInstance();
            SoundBuffer buffBack = new SoundBuffer(sound.getPath());
            manager.addSoundBuffer(buffBack);
            LOADED_SOUNDS.put(sound, buffBack);
        } catch (Exception e) {
            log.error("Can't load sound {}", sound.getPath(), e);
        }
    }

    static int getBufferId(SoundRegistry soundName) {
        if (!LOADED_SOUNDS.containsKey(soundName)) loadSound(soundName);
        return LOADED_SOUNDS.get(soundName).getBufferId();
    }
}
