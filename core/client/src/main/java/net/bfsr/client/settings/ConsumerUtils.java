package net.bfsr.client.settings;

import java.util.function.BiConsumer;

final class ConsumerUtils {
    static final BiConsumer<Option, Object> FLOAT_DEFAULT_CONSUMER = (option, value) -> option.setFloat((float) value);
    static final BiConsumer<Option, Object> BOOLEAN_DEFAULT_CONSUMER = (option, value) -> option.invertBooleanValue();
    static final BiConsumer<Option, Object> INTEGER_DEFAULT_CONSUMER = (option, value) -> option.setInteger((float) value);
}
