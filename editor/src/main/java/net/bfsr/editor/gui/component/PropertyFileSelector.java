package net.bfsr.editor.gui.component;

import net.bfsr.client.gui.SimpleGuiObject;
import net.bfsr.client.gui.button.Button;
import net.bfsr.client.renderer.font.FontType;
import net.bfsr.property.PropertiesHolder;
import net.bfsr.util.PathHelper;
import net.bfsr.util.RunnableUtils;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

import static net.bfsr.editor.gui.ColorScheme.*;

public class PropertyFileSelector<P extends PropertiesHolder> extends PropertyComponent<P> {
    private final Button button;
    protected String path;

    public PropertyFileSelector(int width, int height, String name, int propertyOffsetX, int fontSize, int stringOffsetY, P object, List<Field> fields, Object[] values) {
        super(new Button(null, width - propertyOffsetX, height, (String) values[0], FontType.CONSOLA, fontSize, stringOffsetY, RunnableUtils.EMPTY_RUNNABLE),
                width, height, name, FontType.CONSOLA, fontSize, propertyOffsetX, 0, stringOffsetY, object, fields, values);
        this.path = (String) values[0];
        button = ((Button) subObjects.get(0));
        button.setColor(INPUT_COLOR_GRAY, INPUT_COLOR_GRAY, INPUT_COLOR_GRAY, 1.0f);
        button.setHoverColor(INPUT_COLOR_GRAY, INPUT_COLOR_GRAY, INPUT_COLOR_GRAY, 1.0f);
        button.setOutlineColor(INPUT_OUTLINE_COLOR_GRAY, INPUT_OUTLINE_COLOR_GRAY, INPUT_OUTLINE_COLOR_GRAY, 1.0f);
        button.setOutlineHoverColor(INPUT_OUTLINE_HOVER_COLOR_GRAY, INPUT_OUTLINE_HOVER_COLOR_GRAY, INPUT_OUTLINE_HOVER_COLOR_GRAY, 1.0f);
        button.setTextColor(TEXT_COLOR_GRAY, TEXT_COLOR_GRAY, TEXT_COLOR_GRAY, 1.0f);
        button.setOnMouseClickRunnable(() -> {
            try {
                String selectedFilePath = TinyFileDialogs.tinyfd_openFileDialog("Select file", PathHelper.CLIENT_CONTENT.resolve(path).toString(), null, null, false);
                if (selectedFilePath != null) {
                    setPath(selectedFilePath);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void setSetting() throws IllegalAccessException {
        fields.get(0).set(object, path);
    }

    public void setPath(String path) throws IOException {
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