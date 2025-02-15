package net.bfsr.editor.gui.property;

import lombok.Setter;
import net.bfsr.client.Client;
import net.bfsr.editor.gui.component.MinimizableHolder;
import net.bfsr.editor.property.PropertiesBuilder;
import net.bfsr.editor.property.holder.PropertiesHolder;
import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.GuiManager;
import net.bfsr.engine.gui.component.Button;
import net.bfsr.engine.gui.component.GuiObject;
import net.bfsr.engine.gui.component.Label;
import net.bfsr.engine.gui.component.MinimizableGuiObject;
import net.bfsr.engine.gui.component.Rectangle;
import net.bfsr.engine.gui.component.ScrollPane;
import net.bfsr.engine.renderer.font.AbstractFontManager;
import net.bfsr.engine.renderer.font.glyph.Font;
import net.bfsr.engine.renderer.font.string.StringOffsetType;
import net.bfsr.engine.util.RunnableUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import static net.bfsr.editor.gui.EditorTheme.SCROLL_WIDTH;
import static net.bfsr.editor.gui.EditorTheme.TEXT_COLOR;
import static net.bfsr.editor.gui.EditorTheme.setup;
import static net.bfsr.editor.gui.EditorTheme.setupButton;
import static net.bfsr.editor.gui.EditorTheme.setupContextMenuButton;
import static net.bfsr.editor.gui.EditorTheme.setupScrollPane;

public class PropertiesPanel extends Rectangle {
    private final Client client = Client.get();
    private final GuiManager guiManager = client.getGuiManager();
    private final AbstractFontManager fontManager = Engine.getFontManager();
    private final Font font;
    private final int fontSize;
    private final int elementHeight = 20;
    private final int contextMenuStringXOffset;
    private final ScrollPane scrollPane;
    private final List<MinimizableHolder<PropertiesHolder>> minimizableProperties = new ArrayList<>();
    private final Button saveButton;
    private final Button removeButton;
    @Setter
    private Runnable changeValueListener = RunnableUtils.EMPTY_RUNNABLE;

    private PropertiesHolder clipboard;

    public PropertiesPanel(int width, int height, Font font, int fontSize, int contextMenuStringXOffset) {
        super(width, height);
        this.width = width;
        this.font = font;
        this.fontSize = fontSize;
        this.scrollPane = setupScrollPane(new ScrollPane(width, height - (elementHeight << 1), SCROLL_WIDTH));
        this.contextMenuStringXOffset = contextMenuStringXOffset;

        Label label = new Label(font, "Properties", fontSize, TEXT_COLOR.x, TEXT_COLOR.y, TEXT_COLOR.z, TEXT_COLOR.w);
        add(label.atBottomLeft(() -> 0, () -> this.height - elementHeight + label.getCenteredOffsetY(elementHeight)));

        add(scrollPane.atBottomRight(0, elementHeight).setHeightFunction((screenWidth, screenHeight) -> screenHeight -
                (elementHeight << 1)));

        int buttonWidth = width / 2;
        int x = 0;

        add(saveButton = new Button(buttonWidth, elementHeight, "Save", font, fontSize));
        setupButton(saveButton).atBottomLeft(x, 0);
        add(removeButton = new Button(buttonWidth, elementHeight, "Remove", font, fontSize));
        setupButton(removeButton).atBottomLeft(x + buttonWidth, 0);
    }

    public MinimizableHolder<PropertiesHolder> add(PropertiesHolder propertiesHolder, String name) {
        MinimizableHolder<PropertiesHolder> minimizableHolder = createMinimizable(propertiesHolder, name,
                width - scrollPane.getScrollWidth(), elementHeight, 150);
        updatePositionAndSize();
        return minimizableHolder;
    }

    private MinimizableHolder<PropertiesHolder> createMinimizable(PropertiesHolder propertiesHolder, String name, int width, int height,
                                                                  int propertyOffsetX) {
        propertiesHolder.clearListeners();
        MinimizableHolder<PropertiesHolder> minimizableHolder = new MinimizableHolder<>(width, height, name, font, fontSize,
                propertiesHolder);
        minimizableHolder.setRightReleaseConsumer((mouseX, mouseY) -> {
            int x1 = mouseX;
            int y1 = mouseY - elementHeight;
            String buttonName = "Copy";
            Button copyButton = new Button(font.getWidth(buttonName, fontSize) +
                    contextMenuStringXOffset, elementHeight, buttonName, font, fontSize, contextMenuStringXOffset / 2, 0,
                    StringOffsetType.DEFAULT, EMPTY_BI_CONSUMER);
            copyButton.atBottomLeft(x1, y1);
            copyButton.setLeftReleaseConsumer((mouseX1, mouseY1) -> clipboard = propertiesHolder.copy());
            y1 -= elementHeight;
            buttonName = "Paste";
            Button pasteButton = new Button(font.getWidth(buttonName, fontSize) +
                    contextMenuStringXOffset, elementHeight, buttonName, font, fontSize, contextMenuStringXOffset / 2, 0,
                    StringOffsetType.DEFAULT, EMPTY_BI_CONSUMER);
            pasteButton.atBottomLeft(x1, y1);
            pasteButton.setLeftReleaseConsumer((mouseX1, mouseY1) -> {
                if (clipboard != null && clipboard.getClass() == propertiesHolder.getClass()) {
                    propertiesHolder.paste(clipboard);
                    minimizableHolder.removeAllChild();
                    PropertiesBuilder.createGuiProperties(propertiesHolder, width - MinimizableGuiObject.MINIMIZABLE_STRING_X_OFFSET,
                            height, font, fontSize, propertyOffsetX, 0, minimizableHolder::add, changeValueListener);
                    updatePositionAndSize();
                }
            });
            guiManager.openContextMenu(setupContextMenuButton(copyButton), setupContextMenuButton(pasteButton));
        });

        propertiesHolder.addChangeNameEventListener(minimizableHolder::setName);

        PropertiesBuilder.createGuiProperties(propertiesHolder, width - MinimizableGuiObject.MINIMIZABLE_STRING_X_OFFSET, height, font,
                fontSize, propertyOffsetX, 0, minimizableHolder::add, changeValueListener);

        scrollPane.add(setup(minimizableHolder));
        minimizableProperties.add(minimizableHolder);
        minimizableHolder.tryMaximize();
        return minimizableHolder;
    }

    @Override
    protected void onChildSizeChanged(GuiObject guiObject, int width, int height) {
        super.onChildSizeChanged(guiObject, width, height);
        updatePositionAndSize();
    }

    @Override
    public void updatePositionAndSize() {
        updatePositions();
        super.updatePositionAndSize();
    }

    private void updatePositions() {
        for (int i = 0, y = 0; i < minimizableProperties.size(); i++) {
            MinimizableHolder<PropertiesHolder> minimizable = minimizableProperties.get(i);
            updatePropertiesOffsetAndWidth(minimizable,
                    width - scrollPane.getScrollWidth() - MinimizableGuiObject.MINIMIZABLE_STRING_X_OFFSET);
            minimizable.atTopLeft(0, y);
            y -= minimizable.getHeight();
        }
    }

    private void updatePropertiesOffsetAndWidth(MinimizableHolder<PropertiesHolder> guiObject, int width) {
        List<GuiObject> guiObjects = guiObject.getGuiObjects();
        if (guiObjects.isEmpty()) return;

        int maxStringWidth = ((MinimizableGuiObject) guiObjects.get(0)).getLabel().getWidth();
        for (int i = 1; i < guiObjects.size(); i++) {
            PropertyComponent propertyComponent = ((PropertyComponent) guiObjects.get(i));
            maxStringWidth = Math.max(maxStringWidth, propertyComponent.getLabel().getWidth());
        }

        int propertyOffsetX = maxStringWidth;
        for (int i = 0; i < guiObjects.size(); i++) {
            PropertyComponent propertyComponent = (PropertyComponent) guiObjects.get(i);
            propertyComponent.setPropertyOffsetX(propertyOffsetX);
            propertyComponent.setWidthFunction((width1, height1) -> width);
        }
    }

    public void applyProperties() {
        try {
            for (int i = 0; i < minimizableProperties.size(); i++) {
                MinimizableHolder<PropertiesHolder> minimizable = minimizableProperties.get(i);
                List<GuiObject> guiObjects = minimizable.getGuiObjects();
                for (int j = 0; j < guiObjects.size(); j++) {
                    ((PropertyComponent) guiObjects.get(j)).setSetting();
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isIntersectsWithMouse(int mouseX, int mouseY) {
        return scrollPane.isIntersectsWithMouse(mouseX, mouseY);
    }

    public void open(BiConsumer<Integer, Integer> saveConsumer, BiConsumer<Integer, Integer> removeConsumer) {
        minimizableProperties.clear();
        scrollPane.clear();
        saveButton.setLeftReleaseConsumer(saveConsumer);
        removeButton.setLeftReleaseConsumer(removeConsumer);
    }
}