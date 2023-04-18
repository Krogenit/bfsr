package net.bfsr.editor.gui.property;

import net.bfsr.client.core.Core;
import net.bfsr.client.gui.AbstractGuiObject;
import net.bfsr.client.gui.Gui;
import net.bfsr.client.gui.GuiObjectsContainer;
import net.bfsr.client.gui.button.Button;
import net.bfsr.client.input.Mouse;
import net.bfsr.client.renderer.font.FontType;
import net.bfsr.client.renderer.font.StringOffsetType;
import net.bfsr.client.renderer.font.string.StringObject;
import net.bfsr.client.renderer.gui.GUIRenderer;
import net.bfsr.editor.gui.component.MinimizableHolder;
import net.bfsr.editor.gui.component.PropertyComponent;
import net.bfsr.editor.property.PropertiesBuilder;
import net.bfsr.property.PropertiesHolder;
import net.bfsr.util.RunnableUtils;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;

import static net.bfsr.editor.gui.ColorScheme.*;

public class PropertiesPanel {
    private final Gui gui;
    private final int width;
    private final FontType fontType;
    private final int fontSize;
    private final int elementHeight = 20;
    private final int stringXOffset;
    private final int stringYOffset;
    private final int contextMenuStringXOffset;

    private final GuiObjectsContainer propertiesContainer;
    private final List<MinimizableHolder<PropertiesHolder>> minimizableProperties = new ArrayList<>();

    private StringObject rightHeader;
    private Button saveButton, removeButton;

    private PropertiesHolder clipboard;

    public PropertiesPanel(Gui gui, int width, FontType fontType, int fontSize, int stringXOffset, int stringYOffset, int contextMenuStringXOffset) {
        this.gui = gui;
        this.width = width;
        this.fontType = fontType;
        this.fontSize = fontSize;
        this.propertiesContainer = new GuiObjectsContainer(width, 16);
        this.stringXOffset = stringXOffset;
        this.stringYOffset = stringYOffset;
        this.contextMenuStringXOffset = contextMenuStringXOffset;
    }

    public void initElements() {
        String string = "Properties";
        rightHeader = new StringObject(fontType, string, fontSize, TEXT_COLOR.x, TEXT_COLOR.y, TEXT_COLOR.z, TEXT_COLOR.w).compile();
        rightHeader.atTopRightCorner(-width, fontType.getStringCache().getCenteredYOffset(string, elementHeight, fontSize) + stringYOffset);
        propertiesContainer.atTopRightCorner(-width, elementHeight).setHeightResizeFunction((width, height) -> Core.get().getScreenHeight() - (elementHeight << 1));

        int buttonWidth = width / 2;
        int x = -width;

        saveButton = new Button(buttonWidth, elementHeight, "Save", fontType, fontSize, stringYOffset);
        setupButtonColors(saveButton).atBottomRightCorner(x, -elementHeight);
        removeButton = new Button(buttonWidth, elementHeight, "Remove", fontType, fontSize, stringYOffset);
        setupButtonColors(removeButton).atBottomRightCorner(x + buttonWidth, -elementHeight);
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
        minimizableHolder.setOnRightClickSupplier(() -> {
            if (!minimizableHolder.isMouseHover()) return false;
            Vector2f mousePos = Mouse.getPosition();
            int x1 = (int) mousePos.x;
            int y1 = (int) mousePos.y;
            String buttonName = "Copy";
            Button copyButton = new Button(null, x1, y1, fontType.getStringCache().getStringWidth(buttonName, fontSize) + contextMenuStringXOffset, elementHeight,
                    buttonName, fontType, fontSize, stringXOffset, stringYOffset, StringOffsetType.DEFAULT, RunnableUtils.EMPTY_RUNNABLE);
            copyButton.setOnMouseClickRunnable(() -> clipboard = propertiesHolder.copy());
            y1 += elementHeight;
            buttonName = "Paste";
            Button pastButton = new Button(null, x1, y1, fontType.getStringCache().getStringWidth(buttonName, fontSize) + contextMenuStringXOffset, elementHeight,
                    buttonName, fontType, fontSize, stringXOffset, stringYOffset, StringOffsetType.DEFAULT, RunnableUtils.EMPTY_RUNNABLE);
            pastButton.setOnMouseClickRunnable(() -> {
                if (clipboard != null && clipboard.getClass() == propertiesHolder.getClass()) {
                    propertiesHolder.paste(clipboard);
                    minimizableHolder.removeAllSubObjects();
                    PropertiesBuilder.createGuiProperties(propertiesHolder, width - MinimizableHolder.MINIMIZABLE_STRING_X_OFFSET, height,
                            fontType, fontSize, propertyOffsetX, stringYOffset, minimizableHolder::addSubObject);
                    updatePropertiesPositions();
                }
            });
            gui.openContextMenu(setupContextMenuButtonColors(copyButton), setupContextMenuButtonColors(pastButton));
            return true;
        });

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
            updatePropertiesOffsetAndWidth(minimizable, width - propertiesContainer.getScrollWidth() - MinimizableHolder.MINIMIZABLE_STRING_X_OFFSET);
            minimizable.atTopRightCorner(x, y);
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

    public boolean isIntersectsWithMouse() {
        return propertiesContainer.isIntersectsWithMouse();
    }

    public boolean hasComponents() {
        return minimizableProperties.size() > 0;
    }

    public void close() {
        gui.unregisterGuiObject(rightHeader);
        gui.unregisterGuiObject(saveButton);
        gui.unregisterGuiObject(removeButton);
        gui.unregisterGuiObject(propertiesContainer);
        minimizableProperties.clear();
    }

    public void open(Runnable saveRunnable, Runnable removeRunnable) {
        gui.registerGuiObject(rightHeader);
        gui.registerGuiObject(propertiesContainer);
        saveButton.setOnMouseClickRunnable(saveRunnable);
        removeButton.setOnMouseClickRunnable(removeRunnable);
        gui.registerGuiObject(saveButton);
        gui.registerGuiObject(removeButton);
    }
}