package net.bfsr.editor.property.event;

@FunctionalInterface
public interface ChangeNameEventListener {
    void onChangeName(String name);
}