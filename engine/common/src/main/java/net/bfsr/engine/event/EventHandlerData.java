package net.bfsr.engine.event;

import java.lang.reflect.Method;

record EventHandlerData(Method method, Class<? extends Event> eventClass) {}