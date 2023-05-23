package net.bfsr.event;

import net.bfsr.util.Side;
import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.bus.config.BusConfiguration;
import net.engio.mbassy.bus.config.Feature;
import net.engio.mbassy.bus.config.IBusConfiguration;

public final class EventBus {
    private static final MBassador[] SIDED_BUS = new MBassador[2];

    public static void register(Side side) {
        SIDED_BUS[side.ordinal()] = createMBassador(side);
    }

    public static void post(Side side, Object message) {
        SIDED_BUS[side.ordinal()].publish(message);
    }

    public static void subscribe(Side side, Object object) {
        SIDED_BUS[side.ordinal()].subscribe(object);
    }

    private static MBassador<?> createMBassador(Side side) {
        IBusConfiguration config = new BusConfiguration()
                .addFeature(Feature.SyncPubSub.Default())
                .addFeature(Feature.AsynchronousHandlerInvocation.Default())
                .addFeature(Feature.AsynchronousMessageDispatch.Default())
                .setProperty(IBusConfiguration.Properties.BusId, side + " Channel Bus")
                .addPublicationErrorHandler(error -> {
                    throw new RuntimeException(error.getMessage(), error.getCause());
                });

        return new MBassador<>(config);
    }
}