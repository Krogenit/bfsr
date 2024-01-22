package net.bfsr.event;

import net.bfsr.PerformanceTest;
import net.bfsr.engine.event.Event;
import net.bfsr.engine.event.EventBusManager;
import net.bfsr.engine.event.EventHandler;
import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.bus.config.BusConfiguration;
import net.engio.mbassy.bus.config.Feature;
import net.engio.mbassy.bus.config.IBusConfiguration;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Listener;
import net.engio.mbassy.listener.References;

import java.util.ArrayList;
import java.util.List;

public final class EventBusPerformanceTest {
    /**
     * 8x times faster event publishing, 1.5x times faster listeners register and unregister with {@link EventBusManager}
     *
     * @param args
     */
    public static void main(String[] args) {
        MBassador<Event> mBassador = createEventBus();
        EventBusManager eventBusManager = new EventBusManager();

        int testsCount = 10;
        int countRegisters = 1000;
        List<EventListener> mBassadorEventListeners = new ArrayList<>(testsCount * countRegisters);
        List<EventListener> eventBusEventListeners = new ArrayList<>();

        for (int i = 0; i < testsCount; i++) {
            PerformanceTest.beginTest();
            for (int j = 0; j < countRegisters; j++) {
                EventListener listener = new EventListener();
                mBassador.subscribe(listener);
                mBassadorEventListeners.add(listener);
            }
            PerformanceTest.finishTest("mbassador register");

            PerformanceTest.beginTest();
            for (int j = 0; j < countRegisters; j++) {
                EventListener eventListener = new EventListener();
                eventBusManager.register(eventListener);
                eventBusEventListeners.add(eventListener);
            }
            PerformanceTest.finishTest("eventbusmanager register");

            PerformanceTest.beginTest();
            mBassador.publish(new TestEvent());
            PerformanceTest.finishTest("mbassador event publish");

            PerformanceTest.beginTest();
            eventBusManager.publish(new TestEvent());
            PerformanceTest.finishTest("eventbusmanager event publish");

            PerformanceTest.beginTest();
            for (int j = 0; j < countRegisters; j++) {
                mBassador.unsubscribe(mBassadorEventListeners.get(countRegisters * i + j));
            }
            PerformanceTest.finishTest("mbassador unregister");

            PerformanceTest.beginTest();
            for (int j = 0; j < countRegisters; j++) {
                eventBusManager.unregister(eventBusEventListeners.get(countRegisters * i + j));
            }
            PerformanceTest.finishTest("eventbusmanager unregister");
        }
    }

    @Listener(references = References.Strong)
    public static class EventListener {
        @Handler
        public void event(TestEvent event) {
            event.getClass();
        }

        @EventHandler
        public net.bfsr.engine.event.EventListener<TestEvent> event() {
            return TestEvent::getClass;
        }
    }

    public static class TestEvent extends Event {}

    private static MBassador<Event> createEventBus() {
        IBusConfiguration config = new BusConfiguration()
                .addFeature(Feature.SyncPubSub.Default())
                .addFeature(Feature.AsynchronousHandlerInvocation.Default())
                .addFeature(Feature.AsynchronousMessageDispatch.Default())
                .setProperty(IBusConfiguration.Properties.BusId, "Main Event Bus")
                .addPublicationErrorHandler(error -> System.out.println(error.getMessage() + " " + error.getCause()));

        return new MBassador<>(config);
    }
}