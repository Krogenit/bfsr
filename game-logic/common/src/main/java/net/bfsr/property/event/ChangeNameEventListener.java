package net.bfsr.property.event;

@FunctionalInterface
public interface ChangeNameEventListener {
    void onChangeName(String name);
}