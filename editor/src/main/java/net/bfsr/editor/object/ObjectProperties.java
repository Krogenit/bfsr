package net.bfsr.editor.object;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.editor.property.holder.PropertiesHolderAdapter;

@Setter
@Getter
public class ObjectProperties extends PropertiesHolderAdapter {
    protected String name;
    protected String path;
    protected int treeIndex;

    public String getFullPath() {
        return path != null ? path.isEmpty() ? name : path + "/" + name : "";
    }
}