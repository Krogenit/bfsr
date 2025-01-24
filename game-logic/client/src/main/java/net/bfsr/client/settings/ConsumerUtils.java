package net.bfsr.client.settings;

final class ConsumerUtils {
    static final ClientSettings.ChangeValueConsumer FLOAT_DEFAULT_CONSUMER = (option, value) -> option.setFloat((float) value);
    static final ClientSettings.ChangeValueConsumer BOOLEAN_DEFAULT_CONSUMER = (option, value) -> option.invertBooleanValue();
    static final ClientSettings.ChangeValueConsumer INTEGER_DEFAULT_CONSUMER = (option, value) -> option.setInteger((float) value);
}