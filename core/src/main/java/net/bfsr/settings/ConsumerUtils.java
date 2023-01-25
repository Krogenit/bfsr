package net.bfsr.settings;

import java.util.function.BiConsumer;

final class ConsumerUtils {
    static final BiConsumer<EnumOption, Object> FLOAT_DEFAULT_CONSUMER = (option, value) -> option.setFloat((float) value);
    static final BiConsumer<EnumOption, Object> BOOLEAN_DEFAULT_CONSUMER = (option, value) -> option.invertBooleanValue();
    static final BiConsumer<EnumOption, Object> INTEGER_DEFAULT_CONSUMER = (option, value) -> option.setInteger((float) value);
}
