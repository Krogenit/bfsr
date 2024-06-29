package net.bfsr.editor.property.holder;

import net.bfsr.editor.property.Property;
import net.bfsr.editor.property.event.ChangeNameEventListener;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

public interface PropertiesHolder {
    void setDefaultValues();
    void setName(String name);
    String getName();
    void addChangeNameEventListener(ChangeNameEventListener listener);
    void clearListeners();
    default PropertiesHolder copy() {
        Class<? extends PropertiesHolder> aClass = getClass();
        try {
            PropertiesHolder propertiesHolder = aClass.getConstructor().newInstance();
            copyFields(this, propertiesHolder);
            return propertiesHolder;
        } catch (IllegalAccessException | IllegalArgumentException | InstantiationException | NegativeArraySizeException |
                 NoSuchMethodException |
                 SecurityException |
                 InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }
    default void paste(PropertiesHolder propertiesHolder) {
        try {
            copyFields(propertiesHolder, this);
        } catch (IllegalAccessException | IllegalArgumentException | NegativeArraySizeException | SecurityException e) {
            e.printStackTrace();
        }
    }
    default void copyFields(PropertiesHolder source, PropertiesHolder dest) throws IllegalAccessException {
        Field[] declaredFields = getClass().getDeclaredFields();

        for (int i = 0; i < declaredFields.length; i++) {
            Field field = declaredFields[i];
            Property annotation = field.getDeclaredAnnotation(Property.class);
            if (annotation != null) {
                field.setAccessible(true);
                Class<?> type = field.getType();
                if (type.isPrimitive() || type == String.class || type.isEnum()) {
                    field.set(dest, field.get(source));
                } else if (type.isArray()) {
                    Object array = field.get(source);
                    int length = Array.getLength(array);
                    Class<?> componentType = type.getComponentType();
                    if (componentType.isPrimitive()) {
                        Object newArray = Array.newInstance(componentType, length);
                        System.arraycopy(array, 0, newArray, 0, length);
                        field.set(dest, newArray);
                    }
                } else if (type.isAssignableFrom(List.class)) {
                    ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
                    Class<?> listElementClass = (Class<?>) parameterizedType.getActualTypeArguments()[0];
                    if (PropertiesHolder.class.isAssignableFrom(listElementClass)) {
                        List<? extends PropertiesHolder> list = (List<? extends PropertiesHolder>) field.get(source);
                        List<PropertiesHolder> newList = new ArrayList<>(list.size());
                        for (int i1 = 0; i1 < list.size(); i1++) {
                            PropertiesHolder propertiesHolder1 = list.get(i1);
                            PropertiesHolder copy = propertiesHolder1.copy();
                            newList.add(copy);
                        }

                        field.set(dest, newList);
                    } else if (listElementClass.isPrimitive() || listElementClass == String.class) {
                        List list = (List) field.get(source);
                        List newList = new ArrayList(list.size());
                        newList.addAll(list);
                        field.set(dest, newList);
                    }
                }
            }
        }
    }
}