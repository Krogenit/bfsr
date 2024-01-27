package net.bfsr.engine.event;


import net.bfsr.engine.collection.UnorderedArrayList;

public class Listeners<T extends Event> {
    private final UnorderedArrayList<EventListener<T>> listeners = new UnorderedArrayList<>();

    void addListener(EventListener<T> listener) {
        listeners.add(listener);
    }

    void removeListener(EventListener<T> listener) {
        listeners.remove(listener);
    }

    void addOneTimeListener(EventListener<T> listener) {
        listeners.add(new OneTimeEventListener<>(this, listener));
    }

    public void publish(T event) {
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).run(event);
        }
    }
}