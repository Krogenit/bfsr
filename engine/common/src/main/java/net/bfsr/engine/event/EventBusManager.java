package net.bfsr.engine.event;

import gnu.trove.map.TMap;
import gnu.trove.map.TObjectShortMap;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TObjectShortHashMap;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class EventBusManager {
    private static final TObjectShortMap<Class<? extends Event>> EVENT_CLASS_TO_INDEX_MAP = new TObjectShortHashMap<>();
    private static final TMap<Class<?>, List<EventHandlerData>> HANDLER_METHODS_BY_CLASS_MAP = new THashMap<>();

    static {
        Reflections reflections = new Reflections(new ConfigurationBuilder().forPackage("")
                .addScanners(Scanners.MethodsAnnotated, Scanners.SubTypes));
        Set<Class<?>> classes = reflections.get(Scanners.SubTypes.of(Event.class).asClass());
        short registryIndex = 0;
        for (Class<?> aClass : classes) {
            Class<? extends Event> eventClass = (Class<? extends Event>) aClass;
            EVENT_CLASS_TO_INDEX_MAP.put(eventClass, registryIndex);
            registryIndex++;
        }

        Set<Method> methods = reflections.get(Scanners.MethodsAnnotated.with(EventHandler.class).as(Method.class));
        methods.forEach(method -> {
            List<EventHandlerData> eventListeners = HANDLER_METHODS_BY_CLASS_MAP.computeIfAbsent(method.getDeclaringClass(),
                    aClass -> new ArrayList<>());
            if (method.getAnnotation(EventHandler.class) != null) {
                Type genericReturnType = method.getGenericReturnType();
                if (genericReturnType instanceof ParameterizedType parameterizedType) {
                    Class<?> type = (Class<?>) parameterizedType.getActualTypeArguments()[0];
                    if (Event.class.isAssignableFrom(type)) {
                        eventListeners.add(new EventHandlerData(method, (Class<? extends Event>) type));
                    }
                }
            }
        });
    }

    @SuppressWarnings("rawtypes")
    private final TMap<Class<?>, EventBus> eventBusMap = new THashMap<>();
    private final EventBus<Event>[] eventBuses = new EventBus[EVENT_CLASS_TO_INDEX_MAP.size()];


    private final TMap<Class<?>, List<EventListenerData>> eventListenersByClassMap = new THashMap<>();

    public EventBusManager() {
        EVENT_CLASS_TO_INDEX_MAP.forEachEntry((aClass, i) -> {
            EventBus<Event> eventBus = new EventBus<>();
            eventBuses[i] = eventBus;
            eventBusMap.put(aClass, eventBus);
            EVENT_CLASS_TO_INDEX_MAP.put(aClass, i);
            return true;
        });
    }

    public void register(Object eventHandler) {
        List<EventListenerData> eventListeners = eventListenersByClassMap.computeIfAbsent(eventHandler.getClass(), aClass -> {
            List<EventHandlerData> handlers = HANDLER_METHODS_BY_CLASS_MAP.get(eventHandler.getClass());
            if (handlers == null) throw new RuntimeException("Can't find handlers for event handler " + eventHandler.getClass());

            List<EventListenerData> listeners = new ArrayList<>(handlers.size());
            for (int i = 0; i < handlers.size(); i++) {
                EventHandlerData handlerData = handlers.get(i);
                EventBus<? extends Event> eventBus = eventBusMap.get(handlerData.eventClass());
                try {
                    listeners.add(new EventListenerData(eventBus, (EventListener<?>) handlerData.method().invoke(eventHandler)));
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException("Can't register event listener " + eventHandler.getClass(), e);
                }
            }

            return listeners;
        });

        for (int i = 0; i < eventListeners.size(); i++) {
            eventListeners.get(i).addListener();
        }
    }

    public void unregister(Object eventHandler) {
        List<EventListenerData> eventListeners = eventListenersByClassMap.get(eventHandler.getClass());
        if (eventListeners == null) {
            throw new RuntimeException("Can't find event listeners for event handler " + eventHandler.getClass());
        }

        for (int i = 0; i < eventListeners.size(); i++) {
            eventListeners.get(i).removeEventListener();
        }
    }

    public <T extends Event> void unregister(Class<T> eventClass, EventListener<T> eventListener) {
        eventBusMap.get(eventClass).removeListener(eventListener);
    }

    public void optimizeEvent(Event event) {
        event.setRegistryIndex(EVENT_CLASS_TO_INDEX_MAP.get(event.getClass()));
    }

    public void publish(Event event) {
        eventBusMap.get(event.getClass()).publish(event);
    }

    public void publishOptimized(Event event) {
        eventBuses[event.getRegistryIndex()].publish(event);
    }

    public <T extends Event> void addListener(Class<T> eventClass, EventListener<T> listener) {
        eventBusMap.get(eventClass).addListener(listener);
    }

    public <T extends Event> void addOneTimeListener(Class<T> eventClass, EventListener<T> listener) {
        eventBusMap.get(eventClass).addOneTimeListener(listener);
    }
}