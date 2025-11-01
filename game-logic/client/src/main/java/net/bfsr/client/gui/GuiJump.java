package net.bfsr.client.gui;

import lombok.extern.log4j.Log4j2;
import net.bfsr.client.Client;
import net.bfsr.client.font.FontType;
import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.Gui;
import net.bfsr.engine.gui.component.Button;
import net.bfsr.engine.gui.component.InputBox;
import net.bfsr.engine.gui.component.Label;
import net.bfsr.engine.gui.component.Rectangle;
import net.bfsr.engine.gui.renderer.RectangleOutlinedRenderer;
import net.bfsr.engine.gui.renderer.inputbox.InputBoxOutlinedRenderer;
import net.bfsr.engine.renderer.font.glyph.Font;
import net.bfsr.network.packet.client.PacketShipJump;

import static net.bfsr.engine.input.Keys.KEY_ESCAPE;

@Log4j2
public class GuiJump extends Gui {
    public GuiJump() {
        Rectangle rectangle = new Rectangle(400, 200);
        rectangle.setColor(0.1f, 0.1f, 0.1f, 0.9f);
        rectangle.setHoverColor(0.125f, 0.125f, 0.125f, 0.9f);
        rectangle.setOutlineColor(0.3f, 0.3f, 0.3f, 0.9f);
        rectangle.setOutlineHoverColor(0.3f, 0.3f, 0.3f, 0.9f);
        rectangle.setRenderer(new RectangleOutlinedRenderer(rectangle));

        Font font = Engine.getFontManager().getFont(FontType.CONSOLA.getFontName());
        rectangle.add(new Label(font, "Select dest", 14).atTop(0, -10));

        rectangle.add(new Label(font, "X: ", 14).atCenter(-60, 10));
        rectangle.add(new Label(font, "Y: ", 14).atCenter(-60, -10));

        InputBox xInputBox = new InputBox(100, 20, "", font, 14, 3, 0);
        InputBox yInputBox = new InputBox(100, 20, "", font, 14, 3, 0);
        xInputBox.setColor(0.1f, 0.1f, 0.1f, 0.9f);
        xInputBox.setHoverColor(0.125f, 0.125f, 0.125f, 0.9f);
        xInputBox.setOutlineColor(0.3f, 0.3f, 0.3f, 0.9f);
        xInputBox.setOutlineHoverColor(0.3f, 0.3f, 0.3f, 0.9f);
        yInputBox.setColor(0.1f, 0.1f, 0.1f, 0.9f);
        yInputBox.setHoverColor(0.125f, 0.125f, 0.125f, 0.9f);
        yInputBox.setOutlineColor(0.3f, 0.3f, 0.3f, 0.9f);
        yInputBox.setOutlineHoverColor(0.3f, 0.3f, 0.3f, 0.9f);

        xInputBox.setRenderer(new InputBoxOutlinedRenderer(xInputBox));
        yInputBox.setRenderer(new InputBoxOutlinedRenderer(yInputBox));
        rectangle.add(xInputBox.atCenter(10, 10));
        rectangle.add(yInputBox.atCenter(10, -10));

        Button button = new Button(200, 20, "Jump", font, 14, (mouseX, mouseY) -> {
            try {
                float x = Float.parseFloat(xInputBox.getString());
                float y = Float.parseFloat(yInputBox.getString());

                Client.get().sendTCPPacket(new PacketShipJump(x, y));
                closeGui();
            } catch (NumberFormatException e) {
                log.error("Can't parse jump coordinates", e);
            }
        });
        button.setColor(0.1f, 0.1f, 0.1f, 0.9f);
        button.setHoverColor(0.125f, 0.125f, 0.125f, 0.9f);
        button.setOutlineColor(0.3f, 0.3f, 0.3f, 0.9f);
        button.setOutlineHoverColor(0.3f, 0.3f, 0.3f, 0.9f);
        button.setRenderer(new RectangleOutlinedRenderer(button));
        rectangle.add(button.atBottom(0, 10));

        add(rectangle.atCenter(0, 0));
    }

    @Override
    public boolean input(int key) {
        boolean input = super.input(key);

        if (key == KEY_ESCAPE) {
            guiManager.closeGui();
        }

        return input;
    }
}
