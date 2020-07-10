package ru.krogenit.bfsr.client.sound;

import org.joml.Vector2f;

public class SoundSourceEffect extends SoundSource {

	public SoundSourceEffect(SoundRegistry soundName, Vector2f pos) {
		super(soundName, false, false);
		this.setPosition(pos);
	}

}
