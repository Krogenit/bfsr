package net.bfsr.editor.gui.property;

import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.component.Button;
import net.bfsr.engine.util.PathHelper;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.function.BiConsumer;

import static net.bfsr.editor.gui.EditorTheme.FONT;
import static net.bfsr.editor.gui.EditorTheme.setupButton;

public class PropertyFileSelector extends PropertyComponent {
    private final Button button;
    private String path;

    public PropertyFileSelector(int width, int height, String name, int propertyOffsetX, int fontSize, int stringOffsetY, Object object,
                                List<Field> fields, Object[] values, BiConsumer<Object, Integer> valueConsumer,
                                Runnable changeValueListener) {
        super(width, height, name, FONT, fontSize, propertyOffsetX, 0, stringOffsetY, object, fields, values, valueConsumer,
                changeValueListener);
        this.propertyOffsetX = label.getWidth() + MINIMIZABLE_STRING_X_OFFSET;
        path = (String) values[0];
        addNonConcealable(button = new Button(width - propertyOffsetX, height, path, FONT, fontSize, stringOffsetY, (mouseX, mouseY) -> {
            try {
                String selectedFilePath = Engine.getSystemDialogs()
                        .openFileDialog("Select file", PathHelper.CLIENT_CONTENT.resolve(path).toString(), false);
                if (selectedFilePath != null) {
                    setPath(selectedFilePath);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }));
        setupButton(button).atTopLeft(this.propertyOffsetX, propertyOffsetY);
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
    public void updateConcealableObjectsPositions() {
        button.atTopLeft(propertyOffsetX, propertyOffsetY);
        button.setWidth(width - propertyOffsetX);
        button.setStringXOffset(button.getWidth() / 2);
    }
}