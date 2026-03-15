package net.bfsr.engine.sound;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class SoundEffect {
    private List<Sound> sounds;
    private boolean randomFromList;
}
