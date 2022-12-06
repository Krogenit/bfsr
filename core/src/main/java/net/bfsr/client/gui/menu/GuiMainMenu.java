package net.bfsr.client.gui.menu;

import net.bfsr.client.font_new.FontType;
import net.bfsr.client.gui.Gui;
import net.bfsr.client.gui.GuiSettings;
import net.bfsr.client.gui.GuiTextureObject;
import net.bfsr.client.gui.button.Button;
import net.bfsr.client.gui.button.ButtonBase;
import net.bfsr.client.gui.multiplayer.GuiConnect;
import net.bfsr.client.loader.TextureLoader;
import net.bfsr.client.shader.BaseShader;
import net.bfsr.client.texture.TextureRegister;
import net.bfsr.core.Core;
import net.bfsr.entity.TextureObject;
import org.joml.Vector2f;

public class GuiMainMenu extends Gui {
    private final TextureObject bfsrText, bfsrLogo;

    public GuiMainMenu() {
        bfsrText = new GuiTextureObject(TextureLoader.getTexture(TextureRegister.guiBfsrText2));
        bfsrLogo = new GuiTextureObject(TextureLoader.getTexture(TextureRegister.guiLogoBFSR));

        createString(FontType.Xolonium, "Привет как дела что делаеш ахфзах зфза фщыа" +
                "щзфшыазщ фщзыа зфщлыазщ флыщзал фщзлыа зфщзыа", 0, 8, 12, 1.0f, 1.0f, 1.0f, 1.0f);
        createString(FontType.Xolonium, "Норм че делаеш", 0, 20, 12, 1.0f, 1.0f, 1.0f, 1.0f);
    }

    @Override
    public void init() {
        super.init();

        bfsrLogo.setPosition(center.x, center.y - 150);
        bfsrLogo.setScale(180f, 180f);

        bfsrText.setPosition(center.x, center.y - 150);
        bfsrText.setScale(1553f / 2.25f, 158f / 2f);

        buttons.add(new ButtonBase(0, new Vector2f(center.x, center.y - 45), new Vector2f(260, 40), "gui.mainmenu.singleplayer", new Vector2f(0.9f, 0.8f)));
        buttons.add(new ButtonBase(1, new Vector2f(center.x, center.y), new Vector2f(260, 40), "gui.mainmenu.multiplayer", new Vector2f(0.9f, 0.8f)));
        buttons.add(new ButtonBase(2, new Vector2f(center.x, center.y + 45), new Vector2f(260, 40), "gui.mainmenu.options", new Vector2f(0.9f, 0.8f)));
        buttons.add(new ButtonBase(3, new Vector2f(center.x, center.y + 90), new Vector2f(260, 40), "gui.mainmenu.quit", new Vector2f(0.9f, 0.8f)));
    }

    @Override
    protected void onButtonLeftClick(Button b) {
        switch (b.getId()) {
            case 0:
                Core.getCore().startSingleplayer();
                Core.getCore().setCurrentGui(null);
                return;
            case 1:
//			Core.getCore().connectToServer("localhost", 25565);
                Core.getCore().setCurrentGui(new GuiConnect(this));
                return;
            case 2:
                Core.getCore().setCurrentGui(new GuiSettings(this));
                return;
            case 3:
                Core.getCore().stop();
        }
    }

    @Override
    public void update(double delta) {
        super.update(delta);
    }

    @Override
    public void render(BaseShader shader) {
//        long nano = System.nanoTime();
//        FontRenderer.getInstance().render(staticStrings.get(0));
//        System.out.println("static: " + (System.nanoTime() - nano) / 1_000_000.0f + "ms");
//        nano = System.nanoTime();
//        FontRenderer.getInstance().renderString(FontType.Xolonium, "Привет как дела что делаеш ахфзах зфза фщыа" +
//                "щзфшыазщ фщзыа зфщлыазщ флыщзал фщзлыа зфщзыа", 0, 50, 16, 1.0f, 1.0f, 1.0f, 1.0f, EnumZoomFactor.Gui);
//        System.out.println("dynamic: " + (System.nanoTime() - nano) / 1_000_000.0f + "ms");
        shader.enable();
        bfsrLogo.render(shader);
        bfsrText.render(shader);
        super.render(shader);
        drawStaticStrings();
    }
}
