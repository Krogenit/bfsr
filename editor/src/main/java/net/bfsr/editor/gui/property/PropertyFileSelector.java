package net.bfsr.editor.gui.property;

import net.bfsr.editor.gui.EditorTheme;
import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.component.Button;
import net.bfsr.engine.gui.object.SimpleGuiObject;
import net.bfsr.engine.util.PathHelper;
import net.bfsr.engine.util.RunnableUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.function.BiConsumer;

import static net.bfsr.editor.gui.EditorTheme.*;

public class PropertyFileSelector extends PropertyComponent {
    private final Button button;
    private String path;

    public PropertyFileSelector(int width, int height, String name, int propertyOffsetX, int fontSize, int stringOffsetY,
                                Object object, List<Field> fields, Object[] values,
                                BiConsumer<Object, Integer> valueSetterConsumer) {
        super(new Button(null, width - propertyOffsetX, height, (String) values[0], EditorTheme.FONT_TYPE, fontSize,
                        stringOffsetY,
                        RunnableUtils.EMPTY_RUNNABLE),
                width, height, name, EditorTheme.FONT_TYPE, fontSize, propertyOffsetX, 0, stringOffsetY, object, fields, values,
                valueSetterConsumer);
        this.path = (String) values[0];
        button = ((Button) subObjects.get(0));
        button.setColor(INPUT_COLOR_GRAY, INPUT_COLOR_GRAY, INPUT_COLOR_GRAY, 1.0f);
        button.setHoverColor(INPUT_COLOR_GRAY, INPUT_COLOR_GRAY, INPUT_COLOR_GRAY, 1.0f);
        button.setOutlineColor(INPUT_OUTLINE_COLOR_GRAY, INPUT_OUTLINE_COLOR_GRAY, INPUT_OUTLINE_COLOR_GRAY, 1.0f);
        button.setOutlineHoverColor(INPUT_OUTLINE_HOVER_COLOR_GRAY, INPUT_OUTLINE_HOVER_COLOR_GRAY,
                INPUT_OUTLINE_HOVER_COLOR_GRAY, 1.0f);
        button.setTextColor(TEXT_COLOR_GRAY, TEXT_COLOR_GRAY, TEXT_COLOR_GRAY, 1.0f);
        button.setOnMouseClickRunnable(() -> {
            try {
                String selectedFilePath =
                        Engine.systemDialogs.openFileDialog("Select file", PathHelper.CLIENT_CONTENT.resolve(path).toString(),
                                false);
                if (selectedFilePath != null) {
                    setPath(selectedFilePath);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void setSetting() {
        valueConsumer.accept(path, 0);
    }

    private void setPath(String path) throws IOException {
        this.path = PathHelper.convertToLocalPath(path);
        button.setString(this.path);
    }

    @Override
    public SimpleGuiObject setWidth(int width) {
        button.setWidth(width - propertyOffsetX);
        button.setStringXOffset(button.getWidth() / 2);
        return super.setWidth(width);
    }
}