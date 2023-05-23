package net.bfsr.engine.dialog;

import org.lwjgl.util.tinyfd.TinyFileDialogs;

public class SystemDialogs extends AbstractSystemDialogs {
    @Override
    public String openFileDialog(String title, String path, boolean allowMultipleSelects) {
        return TinyFileDialogs.tinyfd_openFileDialog(title, path, null, null, allowMultipleSelects);
    }
}