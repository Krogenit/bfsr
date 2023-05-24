package net.bfsr.editor.property;

import net.bfsr.editor.property.event.PropertyReceiver;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Property {
    String name() default "";
    String arrayElementName() default "";
    PropertyGuiElementType elementType() default PropertyGuiElementType.INPUT_BOX;
    PropertyGuiElementType arrayElementType() default PropertyGuiElementType.INPUT_BOX;
    int fieldsAmount() default 1;
    Class<? extends PropertyReceiver> receiveHandler() default DefaultReceiveHandler.class;
}