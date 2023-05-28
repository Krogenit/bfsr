package net.bfsr.client.settings;

import java.util.function.BiConsumer;

final class ConsumerUtils {
    static final BiConsumer<ClientSettings, Object> FLOAT_DEFAULT_CONSUMER = (option, value) -> option.setFloat((float) value);
    static final BiConsumer<ClientSettings, Object> BOOLEAN_DEFAULT_CONSUMER = (option, value) -> option.invertBooleanValue();
    static final BiConsumer<ClientSettings, Object> INTEGER_DEFAULT_CONSUMER =
            (option, value) -> option.setInteger((float) value);
}