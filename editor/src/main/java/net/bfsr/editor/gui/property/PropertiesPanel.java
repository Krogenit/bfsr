package net.bfsr.editor.gui.property;

import net.bfsr.client.Core;
import net.bfsr.editor.gui.component.MinimizableGuiObject;
import net.bfsr.editor.gui.component.MinimizableHolder;
import net.bfsr.editor.property.PropertiesBuilder;
import net.bfsr.editor.property.holder.PropertiesHolder;
import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.component.Button;
import net.bfsr.engine.gui.component.GuiObject;
import net.bfsr.engine.gui.component.Label;
import net.bfsr.engine.gui.component.Rectangle;
import net.bfsr.engine.gui.component.ScrollPane;
import net.bfsr.engine.renderer.font.FontType;
import net.bfsr.engine.renderer.font.StringOffsetType;
import net.bfsr.engine.util.RunnableUtils;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;

import static net.bfsr.editor.gui.EditorTheme.TEXT_COLOR;
import static net.bfsr.editor.gui.EditorTheme.setup;
import static net.bfsr.editor.gui.EditorTheme.setupButton;
import static net.bfsr.editor.gui.EditorTheme.setupContextMenuButton;
import static net.bfsr.editor.gui.EditorTheme.setupScrollPane;

public class PropertiesPanel extends Rectangle {
    private final FontType fontType;
    private final int fontSize;
    private final int elementHeight = 20;
    private final int stringXOffset;
    private final int stringYOffset;
    private final int contextMenuStringXOffset;
    private final ScrollPane scrollPane;
    private final List<MinimizableHolder<PropertiesHolder>> minimizableProperties = new ArrayList<>();
    private final Button saveButton;
    private final Button removeButton;
    private PropertiesHolder clipboard;

    public PropertiesPanel(int width, int height, FontType fontType, int fontSize, int stringXOffset, int stringYOffset,
                           int contextMenuStringXOffset) {
        super(width, height);
        this.width = width;
        this.fontType = fontType;
        this.fontSize = fontSize;
        this.scrollPane = setupScrollPane(new ScrollPane(width, height - (elementHeight << 1), 16));
        this.stringXOffset = stringXOffset;
        this.stringYOffset = stringYOffset;
        this.contextMenuStringXOffset = contextMenuStringXOffset;

        String string = "Properties";
        add(new Label(fontType, string, fontSize, TEXT_COLOR.x, TEXT_COLOR.y, TEXT_COLOR.z, TEXT_COLOR.w)
                .compileAtOrigin()
                .atTopRight(-width, fontType.getStringCache().getCenteredYOffset(string, elementHeight, fontSize) + stringYOffset));

        add(scrollPane.atTopRight(-width, elementHeight).setHeightFunction((screenWidth, screenHeight) -> screenHeight -
                (elementHeight << 1)));

        int buttonWidth = width / 2;
        int x = -width;

        add(saveButton = new Button(buttonWidth, elementHeight, "Save", fontType, fontSize, stringYOffset));
        setupButton(saveButton).atBottomRight(x, -elementHeight);
        add(removeButton = new Button(buttonWidth, elementHeight, "Remove", fontType, fontSize, stringYOffset));
        setupButton(removeButton).atBottomRight(x + buttonWidth, -elementHeight);
    }

    public void add(PropertiesHolder propertiesHolder, String name) {
        createMinimizable(propertiesHolder, name, width - scrollPane.getScrollWidth(), elementHeight, 150);
        updatePositionAndSize();
    }

    private void createMinimizable(PropertiesHolder propertiesHolder, String name, int width, int height, int propertyOffsetX) {
        propertiesHolder.clearListeners();
        MinimizableHolder<PropertiesHolder> minimizableHolder = new MinimizableHolder<>(width, height, name, fontType, fontSize,
                stringYOffset, propertiesHolder);
        minimizableHolder.setRightClickRunnable(() -> {
            Vector2f mousePos = Engine.mouse.getPosition();
            int x1 = (int) mousePos.x;
            int y1 = (int) mousePos.y;
            String buttonName = "Copy";
            Button copyButton = new Button(x1, y1,
                    fontType.getStringCache().getStringWidth(buttonName, fontSize) + contextMenuStringXOffset, elementHeight,
                    buttonName, fontType, fontSize, stringXOffset, stringYOffset, StringOffsetType.DEFAULT,
                    RunnableUtils.EMPTY_RUNNABLE);
            copyButton.setLeftReleaseRunnable(() -> clipboard = propertiesHolder.copy());
            y1 += elementHeight;
            buttonName = "Paste";
            Button pastButton = new Button(x1, y1,
                    fontType.getStringCache().getStringWidth(buttonName, fontSize) + contextMenuStringXOffset, elementHeight,
                    buttonName, fontType, fontSize, stringXOffset, stringYOffset, StringOffsetType.DEFAULT,
                    RunnableUtils.EMPTY_RUNNABLE);
            pastButton.setLeftReleaseRunnable(() -> {
                if (clipboard != null && clipboard.getClass() == propertiesHolder.getClass()) {
                    propertiesHolder.paste(clipboard);
                    minimizableHolder.removeAll();
                    PropertiesBuilder.createGuiProperties(propertiesHolder, width - MinimizableGuiObject.MINIMIZABLE_STRING_X_OFFSET,
                            height, fontType, fontSize, propertyOffsetX, stringYOffset, minimizableHolder::add);
                    updatePositionAndSize();
                }
            });
            Core.get().getGuiManager().openContextMenu(setupContextMenuButton(copyButton), setupContextMenuButton(pastButton));
        });

        propertiesHolder.addChangeNameEventListener(minimizableHolder::setName);

        PropertiesBuilder.createGuiProperties(propertiesHolder, width - MinimizableGuiObject.MINIMIZABLE_STRING_X_OFFSET, height,
                fontType, fontSize, propertyOffsetX, stringYOffset, minimizableHolder::add);

        scrollPane.add(setup(minimizableHolder));
        minimizableProperties.add(minimizableHolder);
        minimizableHolder.tryMaximize();
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
            y += minimizable.getHeight();
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
    public boolean isIntersectsWithMouse() {
        return scrollPane.isIntersectsWithMouse();
    }

    public void open(Runnable saveRunnable, Runnable removeRunnable) {
        minimizableProperties.clear();
        scrollPane.clear();
        saveButton.setLeftReleaseRunnable(saveRunnable);
        removeButton.setLeftReleaseRunnable(removeRunnable);
    }
}