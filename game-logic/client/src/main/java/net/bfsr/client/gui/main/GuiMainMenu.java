package net.bfsr.client.gui.main;

import net.bfsr.client.Core;
import net.bfsr.client.gui.connect.GuiConnect;
import net.bfsr.client.gui.settings.GuiSettings;
import net.bfsr.client.language.Lang;
import net.bfsr.engine.gui.Gui;
import net.bfsr.engine.gui.component.Button;
import net.bfsr.engine.gui.component.Label;
import net.bfsr.engine.gui.component.TexturedRectangle;
import net.bfsr.engine.renderer.font.Font;
import net.bfsr.engine.renderer.texture.TextureRegister;

public class GuiMainMenu extends Gui {
    public GuiMainMenu() {
        add(new TexturedRectangle(TextureRegister.guiLogoBFSR, 180, 180).atCenter(-90, -240));
        add(new TexturedRectangle(TextureRegister.guiBfsrText2, 690, 79).atCenter(-345, -189));

        int buttonWidth = 260;
        int buttonHeight = 40;
        int x = -width / 2;
        Font font = Font.XOLONIUM_LEGACY;
        int fontSze = 13;
        add(new Button(buttonWidth, buttonHeight, Lang.getString("gui.mainmenu.singleplayer"), font, fontSze,
                () -> Core.get().startSinglePlayer()).atCenter(x, -45));
        add(new Button(buttonWidth, buttonHeight, Lang.getString("gui.mainmenu.multiplayer"), font, fontSze,
                () -> Core.get().openGui(new GuiConnect(this))).atCenter(x, 0));
        add(new Button(buttonWidth, buttonHeight, Lang.getString("gui.mainmenu.options"), font, fontSze,
                () -> Core.get().openGui(new GuiSettings(this))).atCenter(x, 45));
        add(new Button(buttonWidth, buttonHeight, Lang.getString("gui.mainmenu.quit"), font, fontSze,
                () -> Core.get().stop())
                .atCenter(x, 90));

        x += buttonWidth;
        font = Font.XOLONIUM;
        fontSze = 13;
        add(new Button(buttonWidth, buttonHeight, Lang.getString("gui.mainmenu.singleplayer"), font, fontSze,
                () -> Core.get().startSinglePlayer()).atCenter(x, -45));
        add(new Button(buttonWidth, buttonHeight, Lang.getString("gui.mainmenu.multiplayer"), font, fontSze,
                () -> Core.get().openGui(new GuiConnect(this))).atCenter(x, 0));
        add(new Button(buttonWidth, buttonHeight, Lang.getString("gui.mainmenu.options"), font, fontSze,
                () -> Core.get().openGui(new GuiSettings(this))).atCenter(x, 45));
        add(new Button(buttonWidth, buttonHeight, Lang.getString("gui.mainmenu.quit"), font, fontSze,
                () -> Core.get().stop())
                .atCenter(x, 90));

        x += buttonWidth;
        font = Font.SANS_SERIF_LEGACY;
        fontSze = 30;
        add(new Button(buttonWidth, buttonHeight, Lang.getString("gui.mainmenu.singleplayer"), font, fontSze,
                () -> Core.get().startSinglePlayer()).atCenter(x, -45));
        add(new Button(buttonWidth, buttonHeight, Lang.getString("gui.mainmenu.multiplayer"), font, fontSze,
                () -> Core.get().openGui(new GuiConnect(this))).atCenter(x, 0));
        add(new Button(buttonWidth, buttonHeight, Lang.getString("gui.mainmenu.options"), font, fontSze,
                () -> Core.get().openGui(new GuiSettings(this))).atCenter(x, 45));
        add(new Button(buttonWidth, buttonHeight, Lang.getString("gui.mainmenu.quit"), font, fontSze,
                () -> Core.get().stop())
                .atCenter(x, 90));

        x += buttonWidth;
        font = Font.Segoe_UI;
        fontSze = 36;
        add(new Button(buttonWidth, buttonHeight, Lang.getString("gui.mainmenu.singleplayer"), font, fontSze,
                () -> Core.get().startSinglePlayer()).atCenter(x, -45));
        add(new Button(buttonWidth, buttonHeight, Lang.getString("gui.mainmenu.multiplayer"), font, fontSze,
                () -> Core.get().openGui(new GuiConnect(this))).atCenter(x, 0));
        add(new Button(buttonWidth, buttonHeight, Lang.getString("gui.mainmenu.options"), font, fontSze,
                () -> Core.get().openGui(new GuiSettings(this))).atCenter(x, 45));
        add(new Button(buttonWidth, buttonHeight, Lang.getString("gui.mainmenu.quit"), font, fontSze,
                () -> Core.get().stop())
                .atCenter(x, 90));

        fontSze = 24;
        add(new Label(Font.XOLONIUM_LEGACY, "BFSR Client " + Core.GAME_VERSION, 0, 0, fontSze).atTopLeft(0, 0));
        add(new Label(Font.XOLONIUM, "BFSR Client " + Core.GAME_VERSION, 0, 0, fontSze).atTopLeft(300, 0));
        add(new Label(Font.CONSOLA, "BFSR Client " + Core.GAME_VERSION, 0, 0, fontSze).atTopLeft(600, 0));
        add(new Label(Font.Segoe_UI, "BFSR Client " + Core.GAME_VERSION, 0, 0, fontSze).atTopLeft(850, 0));
    }
}