package net.bfsr.editor.gui.property;

import net.bfsr.client.core.Core;
import net.bfsr.client.gui.AbstractGuiObject;
import net.bfsr.client.gui.Gui;
import net.bfsr.client.gui.GuiObjectsContainer;
import net.bfsr.client.gui.button.Button;
import net.bfsr.client.renderer.font.FontType;
import net.bfsr.client.renderer.font.string.StringObject;
import net.bfsr.client.renderer.instanced.GUIRenderer;
import net.bfsr.editor.gui.component.MinimizableHolder;
import net.bfsr.editor.gui.component.PropertyComponent;
import net.bfsr.editor.property.PropertiesBuilder;
import net.bfsr.property.PropertiesHolder;

import java.util.ArrayList;
import java.util.List;

import static net.bfsr.editor.gui.ColorScheme.*;

public class PropertiesPanel {
    private final Gui gui;
    private final int width;
    private final FontType fontType;
    private final int fontSize;
    private final int elementHeight = 20;
    private final int stringYOffset;

    private final GuiObjectsContainer propertiesContainer;
    private final List<AbstractGuiObject> rightSideGuiObjects = new ArrayList<>();
    private final List<MinimizableHolder<PropertiesHolder>> minimizableProperties = new ArrayList<>();

    private StringObject rightHeader;
    private Button saveButton, removeButton;

    public PropertiesPanel(Gui gui, int width, FontType fontType, int fontSize, int stringYOffset) {
        this.gui = gui;
        this.width = width;
        this.fontType = fontType;
        this.fontSize = fontSize;
        this.propertiesContainer = new GuiObjectsContainer(width, 16);
        this.stringYOffset = stringYOffset;
    }

    public void initElements(Runnable saveRunnable, Runnable removeRunnable) {
        String string = "Properties";
        rightHeader = new StringObject(fontType, string, fontSize, TEXT_COLOR.x, TEXT_COLOR.y, TEXT_COLOR.z, TEXT_COLOR.w).compile();
        gui.registerGuiObject(rightHeader.atTopRightCorner(-width, fontType.getStringCache().getCenteredYOffset(string, elementHeight, fontSize) + stringYOffset));
        rightSideGuiObjects.add(rightHeader);

        gui.registerGuiObject(propertiesContainer.atTopRightCorner(-width, elementHeight).setHeightResizeFunction((width, height) -> Core.get().getScreenHeight() - (elementHeight << 1)));
        rightSideGuiObjects.add(propertiesContainer);

        int buttonWidth = width / 2;
        int x = -width;

        saveButton = new Button(buttonWidth, elementHeight, "Save", fontType, fontSize, stringYOffset, saveRunnable);
        gui.registerGuiObject(setupButtonColors(saveButton).atBottomRightCorner(x, -elementHeight));
        rightSideGuiObjects.add(saveButton);
        removeButton = new Button(buttonWidth, elementHeight, "Remove", fontType, fontSize, stringYOffset, removeRunnable);
        gui.registerGuiObject(setupButtonColors(removeButton).atBottomRightCorner(x + buttonWidth, -elementHeight));
        rightSideGuiObjects.add(removeButton);
    }

    public void add(PropertiesHolder propertiesHolder, String name) {
        createMinimizable(propertiesHolder, name, width - propertiesContainer.getScrollWidth(), elementHeight, 150);
        updatePropertiesPositions();
    }

    private void createMinimizable(PropertiesHolder propertiesHolder, String name, int width, int height, int propertyOffsetX) {
        propertiesHolder.clearListeners();
        MinimizableHolder<PropertiesHolder> minimizableHolder = new MinimizableHolder<>(width, height, name, fontType, fontSize,
                stringYOffset, propertiesHolder);
        minimizableHolder.setOnMaximizeRunnable(this::updatePropertiesPositions);
        minimizableHolder.setOnMinimizeRunnable(this::updatePropertiesPositions);

        propertiesHolder.registerChangeNameEventListener(minimizableHolder::setName);

        PropertiesBuilder.createGuiProperties(propertiesHolder, width - MinimizableHolder.MINIMIZABLE_STRING_X_OFFSET, height,
                fontType, fontSize, propertyOffsetX, stringYOffset, minimizableHolder::addSubObject);

        setupColors(minimizableHolder);
        propertiesContainer.addSubObject(minimizableHolder);
        this.minimizableProperties.add(minimizableHolder);
        minimizableHolder.maximize();
    }

    public void updatePropertiesPositions() {
        int x = -width;
        int y = elementHeight;

        for (int i = 0; i < minimizableProperties.size(); i++) {
            MinimizableHolder<PropertiesHolder> minimizable = minimizableProperties.get(i);
            minimizable.atTopRightCorner(x, y);
            updatePropertiesOffsetAndWidth(minimizable, width - propertiesContainer.getScrollWidth() - MinimizableHolder.MINIMIZABLE_STRING_X_OFFSET);
            y += minimizable.getHeight();
        }

        propertiesContainer.setWidth(width);
        propertiesContainer.atTopRightCorner(-width, elementHeight);
        propertiesContainer.updatePositionAndSize();
        rightHeader.atTopRightCorner(-width, fontType.getStringCache().getCenteredYOffset(rightHeader.getString(), elementHeight, fontSize) + stringYOffset);
        rightHeader.updatePositionAndSize();
        int buttonWidth = width / 2;
        x = -width;
        saveButton.setWidth(buttonWidth).atBottomRightCorner(x, -elementHeight);
        saveButton.updatePositionAndSize();
        removeButton.setWidth(buttonWidth).atBottomRightCorner(x + buttonWidth, -elementHeight);
        removeButton.updatePositionAndSize();
    }

    private void updatePropertiesOffsetAndWidth(MinimizableHolder<PropertiesHolder> guiObject, int width) {
        List<AbstractGuiObject> guiObjects = guiObject.getSubObjects();
        int maxStringWidth = ((PropertyComponent<?>) guiObjects.get(0)).getStringObject().getWidth();
        for (int i = 1; i < guiObjects.size(); i++) {
            PropertyComponent<?> propertyComponent = ((PropertyComponent<?>) guiObjects.get(i));
            maxStringWidth = Math.max(maxStringWidth, propertyComponent.getStringObject().getWidth());
        }

        int propertyOffsetX = maxStringWidth;

        for (int i = 0; i < guiObjects.size(); i++) {
            PropertyComponent<?> propertyComponent = (PropertyComponent<?>) guiObjects.get(i);
            propertyComponent.setPropertyOffsetX(propertyOffsetX);
            propertyComponent.setWidth(width);
        }
    }

    public void applyProperties() {
        try {
            for (int i = 0; i < minimizableProperties.size(); i++) {
                MinimizableHolder<PropertiesHolder> minimizable = minimizableProperties.get(i);
                List<AbstractGuiObject> propertyComponent = minimizable.getSubObjects();
                for (int j = 0; j < propertyComponent.size(); j++) {
                    ((PropertyComponent<?>) propertyComponent.get(j)).setSetting();
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void render() {
        GUIRenderer.get().add(propertiesContainer.getX(), propertiesContainer.getY(), propertiesContainer.getWidth(), propertiesContainer.getHeight(),
                BACKGROUND_COLOR.x, BACKGROUND_COLOR.y, BACKGROUND_COLOR.z, BACKGROUND_COLOR.w);
    }

    public boolean isMouseHover() {
        return propertiesContainer.isMouseHover();
    }

    public boolean hasComponents() {
        return minimizableProperties.size() > 0;
    }

    public void close() {
        if (rightSideGuiObjects.size() > 0) {
            for (int i = 0; i < rightSideGuiObjects.size(); i++) {
                gui.unregisterGuiObject(rightSideGuiObjects.get(i));
            }

            rightSideGuiObjects.clear();
            minimizableProperties.clear();
        }
    }
}