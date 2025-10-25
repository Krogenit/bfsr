package net.bfsr.editor.gui.input;

import net.bfsr.client.Client;
import net.bfsr.client.input.InputController;
import net.bfsr.config.entity.ship.ShipData;
import net.bfsr.config.entity.ship.ShipRegistry;
import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.GuiManager;
import net.bfsr.engine.gui.component.Button;
import net.bfsr.engine.gui.component.ScrollPane;
import net.bfsr.engine.input.AbstractMouse;
import net.bfsr.engine.renderer.font.string.StringOffsetType;
import net.bfsr.network.packet.client.PacketCommand;
import org.joml.Vector2f;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;

import static net.bfsr.editor.gui.EditorTheme.CONTEXT_MENU_BUTTON_HEIGHT;
import static net.bfsr.editor.gui.EditorTheme.CONTEXT_MENU_STRING_OFFSET_X;
import static net.bfsr.editor.gui.EditorTheme.FONT;
import static net.bfsr.editor.gui.EditorTheme.FONT_SIZE;
import static net.bfsr.editor.gui.EditorTheme.SCROLL_WIDTH;
import static net.bfsr.editor.gui.EditorTheme.setupContextMenuButton;
import static net.bfsr.editor.gui.EditorTheme.setupScrollPane;

public class EditorInputController extends InputController {
    private final Client client = Client.get();
    private final GuiManager guiManager = Engine.getGuiManager();
    private final AbstractMouse mouse = Engine.getMouse();

    @Override
    public boolean mouseRightRelease() {
        Vector2i mousePosition = mouse.getGuiPosition();
        showDefaultContextMenu(mousePosition.x, mousePosition.y);
        return true;
    }

    private void showDefaultContextMenu(int mouseX, int mouseY) {
        String title = "Spawn ship";
        Button spawnShipButton = new Button(FONT.getWidth(title, FONT_SIZE) + CONTEXT_MENU_STRING_OFFSET_X, CONTEXT_MENU_BUTTON_HEIGHT,
                title, FONT, FONT_SIZE, CONTEXT_MENU_STRING_OFFSET_X / 2, 0, StringOffsetType.DEFAULT, (mouseX1, mouseY1) -> {
            ShipRegistry shipRegistry = client.getConfigConverterManager().getConverter(ShipRegistry.class);
            List<ShipData> dataList = shipRegistry.getAll();
            List<Button> buttons = new ArrayList<>(dataList.size());
            for (int i = 0; i < dataList.size(); i++) {
                ShipData shipData = dataList.get(i);
                String shipName = shipData.getFileName();
                buttons.add(setupContextMenuButton(new Button(FONT.getWidth(shipName, FONT_SIZE) + CONTEXT_MENU_STRING_OFFSET_X,
                        CONTEXT_MENU_BUTTON_HEIGHT, shipName, FONT, FONT_SIZE, CONTEXT_MENU_STRING_OFFSET_X / 2, 0,
                        StringOffsetType.DEFAULT, (mouseX2, mouseY2) -> {
                    Vector2f worldVector = client.getCamera().getWorldVector(mouseX, mouseY);
                    client.sendTCPPacket(PacketCommand.spawnShip(shipData.getId(), worldVector.x, worldVector.y));
                })));
            }

            int maxWidth = 0;
            for (int i = 0; i < buttons.size(); i++) {
                int width = buttons.get(i).getWidth();
                if (width > maxWidth) {
                    maxWidth = width;
                }
            }

            maxWidth += SCROLL_WIDTH;

            int maxVisibleElements = 10;
            ScrollPane scrollPane = new ScrollPane(maxWidth, Math.min(buttons.size() * CONTEXT_MENU_BUTTON_HEIGHT,
                    CONTEXT_MENU_BUTTON_HEIGHT * maxVisibleElements), SCROLL_WIDTH);
            scrollPane.atBottomLeft(mouseX, mouseY - scrollPane.getHeight());
            int y = 0;
            for (int i = 0; i < buttons.size(); i++) {
                scrollPane.add(buttons.get(i).setWidth(maxWidth).atTopLeft(0, y));
                y -= CONTEXT_MENU_BUTTON_HEIGHT;
            }
            guiManager.openContextMenu(setupScrollPane(scrollPane));
        });
        spawnShipButton.atBottomLeft(mouseX, mouseY - CONTEXT_MENU_BUTTON_HEIGHT);
        guiManager.openContextMenu(setupContextMenuButton(spawnShipButton));
    }
}
