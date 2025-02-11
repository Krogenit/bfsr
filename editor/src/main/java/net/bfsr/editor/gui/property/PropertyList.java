package net.bfsr.editor.gui.property;

import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.GuiManager;
import net.bfsr.engine.gui.component.Button;
import net.bfsr.engine.gui.renderer.RectangleOutlinedRenderer;
import net.bfsr.engine.renderer.AbstractSpriteRenderer;
import net.bfsr.engine.renderer.font.glyph.Font;
import net.bfsr.engine.renderer.primitive.Primitive;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static net.bfsr.editor.gui.EditorTheme.TEXT_COLOR_GRAY;
import static net.bfsr.editor.gui.EditorTheme.setupButton;

abstract class PropertyList<T extends PropertyComponent, O> extends PropertyObject<T> {
    final GuiManager guiManager = Engine.getGuiManager();
    private final Button addButton;
    final Supplier<O> supplier;
    final int contextMenuStringXOffset = 8;

    PropertyList(int width, int height, String name, Font font, int fontSize, int propertyOffsetX, int stringOffsetY,
                 Supplier<O> supplier, Object object, List<Field> fields, Object[] values, BiConsumer<Object, Integer> valueConsumer,
                 Runnable changeValueListener) {
        super(width, height, name, font, fontSize, propertyOffsetX, stringOffsetY, object, fields, values, valueConsumer,
                changeValueListener);
        this.supplier = supplier;
        int addButtonSize = 20;
        add(addButton = new Button(addButtonSize, addButtonSize, "", font, fontSize,
                stringOffsetY, (mouseX, mouseY) -> addProperty(createObject())));
        setupButton(addButton).atBottomRight(0, 0);
        addButton.setRenderer(new RectangleOutlinedRenderer(addButton) {
            private static final Primitive PLUS_1_PRIMITIVE = new Primitive(-0.0833f, 0.5f, 0.0f, 1.0f, -0.0833f, -0.5f, 1.0f, 1.0f,
                    0.0833f, -0.5f, 1.0f, 0.0f, 0.0833f, 0.5f, 0.0f, 0.0f);
            private static final Primitive PLUS_2_PRIMITIVE = new Primitive(-0.5f, 0.0833f, 0.0f, 1.0f, -0.5f, -0.0833f, 1.0f, 1.0f,
                    0.5f, -0.0833f, 1.0f, 0.0f, 0.5f, 0.0833f, 0.0f, 0.0f);

            private int plus1Id, plus2Id;

            @Override
            protected void create() {
                super.create();
                renderer.getSpriteRenderer().addPrimitive(PLUS_1_PRIMITIVE);
                renderer.getSpriteRenderer().addPrimitive(PLUS_2_PRIMITIVE);

                int x = guiObject.getSceneX() + guiObject.getWidth() / 2;
                int y = guiObject.getSceneY() + guiObject.getHeight() / 2;
                idList.add(plus1Id = guiRenderer.add(x, y, 8, 8, TEXT_COLOR_GRAY, TEXT_COLOR_GRAY, TEXT_COLOR_GRAY, 1.0f));
                idList.add(plus2Id = guiRenderer.add(x, y, 8, 8, TEXT_COLOR_GRAY, TEXT_COLOR_GRAY, TEXT_COLOR_GRAY, 1.0f));
            }

            @Override
            protected void renderBody() {
                super.renderBody();
                guiRenderer.addDrawCommand(plus1Id, PLUS_1_PRIMITIVE.getBaseVertex());
                guiRenderer.addDrawCommand(plus2Id, PLUS_2_PRIMITIVE.getBaseVertex());
            }

            @Override
            protected void setBodyLastValues() {
                super.setBodyLastValues();
                int x = lastX + guiObject.getWidth() / 2;
                int y = lastY + guiObject.getHeight() / 2;
                guiRenderer.setLastPosition(plus1Id, x, y);
                guiRenderer.setLastPosition(plus2Id, x, y);
            }

            @Override
            public void updatePosition() {
                super.updatePosition();
                int x = guiObject.getSceneX() + guiObject.getWidth() / 2;
                int y = guiObject.getSceneY() + guiObject.getHeight() / 2;
                guiRenderer.setPosition(plus1Id, x, y);
                guiRenderer.setPosition(plus2Id, x, y);
            }
        });

        Button removeButton = new Button(20, 20, "", font, fontSize, stringOffsetY, (mouseX, mouseY) -> {
            if (properties.size() > 0) {
                removeProperty(properties.get(properties.size() - 1));
            }
        });
        add(setupButton(removeButton).atBottomRight(-20, 0));
        removeButton.setRenderer(new RectangleOutlinedRenderer(removeButton) {
            private int minusId;

            @Override
            protected void create() {
                super.create();

                int x = guiObject.getSceneX() + guiObject.getWidth() / 2;
                int y = guiObject.getSceneY() + guiObject.getHeight() / 2;
                idList.add(minusId = guiRenderer.add(x, y, 6, 2, TEXT_COLOR_GRAY, TEXT_COLOR_GRAY, TEXT_COLOR_GRAY, 1.0f));
            }

            @Override
            protected void renderBody() {
                super.renderBody();
                guiRenderer.addDrawCommand(minusId, AbstractSpriteRenderer.CENTERED_QUAD_BASE_VERTEX);
            }

            @Override
            protected void setBodyLastValues() {
                super.setBodyLastValues();
                int x = lastX + guiObject.getWidth() / 2;
                int y = lastY + guiObject.getHeight() / 2;
                guiRenderer.setLastPosition(minusId, x, y);
            }

            @Override
            public void updatePosition() {
                super.updatePosition();
                int x = guiObject.getSceneX() + guiObject.getWidth() / 2;
                int y = guiObject.getSceneY() + guiObject.getHeight() / 2;
                guiRenderer.setPosition(minusId, x, y);
            }
        });
    }

    @Override
    protected int getMaximizedHeight() {
        return super.getMaximizedHeight() + addButton.getHeight();
    }

    protected abstract O createObject();

    public abstract void addProperty(O propertiesHolder);

    protected void removeProperty(T guiObject) {
        properties.remove(guiObject);
        remove(guiObject);
    }
}