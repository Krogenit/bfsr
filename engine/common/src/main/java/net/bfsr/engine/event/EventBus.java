package net.bfsr.engine.event;

import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.bus.config.IBusConfiguration;

public class EventBus extends MBassador<Event> {
    public EventBus(IBusConfiguration config) {
        super(config);
    }
}