package net.bfsr.client.gui.objects;

import net.bfsr.client.Client;
import net.bfsr.client.assets.SoundRegistry;
import net.bfsr.client.gui.GuiStyle;
import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.component.Button;
import net.bfsr.engine.gui.component.GuiObject;

import java.util.function.BiConsumer;

public class SimpleButton extends Button {
    public SimpleButton(int width, int height, String string, int fontSize, BiConsumer<Integer, Integer> leftReleaseConsumer) {
        super(width, height, string, fontSize, leftReleaseConsumer);
        GuiStyle.setupTransparentButton(this);
    }

    public SimpleButton(int width, int height, String string, BiConsumer<Integer, Integer> leftReleaseConsumer) {
        super(width, height, string, leftReleaseConsumer);
        GuiStyle.setupTransparentButton(this);
    }

    public SimpleButton(String string, int fontSize, BiConsumer<Integer, Integer> leftReleaseConsumer) {
        super(string, fontSize, leftReleaseConsumer);
        GuiStyle.setupTransparentButton(this);
    }

    public SimpleButton(String string, BiConsumer<Integer, Integer> leftReleaseConsumer) {
        super(string, leftReleaseConsumer);
        GuiStyle.setupTransparentButton(this);
    }

    public SimpleButton(int width, int height, String string) {
        super(width, height, string);
        GuiStyle.setupTransparentButton(this);
    }

    private final int collideSoundBuffer = Engine.getAssetsManager().getSound(SoundRegistry.buttonCollide.getPath());
    private final int clickSoundBuffer = Engine.getAssetsManager().getSound(SoundRegistry.buttonClick.getPath());

    @Override
    public void onMouseHover() {
        super.onMouseHover();
        Client.get().getSoundManager().play(collideSoundBuffer, SoundRegistry.buttonCollide.getVolume());
    }

    @Override
    public GuiObject setLeftReleaseConsumer(BiConsumer<Integer, Integer> consumer) {
        leftReleaseConsumer = (mouseX, mouseY) -> {
            consumer.accept(mouseX, mouseY);
            Client.get().getSoundManager().play(clickSoundBuffer, SoundRegistry.buttonClick.getVolume());
        };
        return this;
    }
}
