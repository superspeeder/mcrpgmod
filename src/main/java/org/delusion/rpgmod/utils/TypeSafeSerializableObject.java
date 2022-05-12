package org.delusion.rpgmod.utils;

import com.google.gson.reflect.TypeToken;

public class TypeSafeSerializableObject {

    public Class<?> type;
    public Object value;

    public TypeSafeSerializableObject(Object value) {
        this.value = value;
        type = value.getClass();
    }

    public static Object unwrap(Object value, Class<?> type) {
        if (type.isAssignableFrom(value.getClass())) {
            return value;
        } else {
            if (type.isAssignableFrom(Integer.class) && !value.getClass().isAssignableFrom(Integer.class)) {
                return tryConvToInteger(value);
            }

            if (type.isAssignableFrom(Double.class) && !value.getClass().isAssignableFrom(Double.class)) {
                return tryConvToDouble(value);
            }

            if (type.isAssignableFrom(Float.class) && !value.getClass().isAssignableFrom(Float.class)) {
                return tryConvToFloat(value);
            }

            if (type.isAssignableFrom(Long.class) && !value.getClass().isAssignableFrom(Double.class)) {
                return tryConvToLong(value);
            }

        }

        throw new IllegalArgumentException("Cannot unwrap value of type " + value.getClass().toGenericString() + " to a value of type " + type.toGenericString());
    }

    private static Integer tryConvToInteger(Object value) {
        if (Number.class.isAssignableFrom(value.getClass())) {
            return ((Number)value).intValue();
        }
        throw new IllegalArgumentException("Cannot convert value of type " + value.getClass().toGenericString() + " to an integer");
    }

    private static Double tryConvToDouble(Object value) {
        if (Number.class.isAssignableFrom(value.getClass())) {
            return ((Number)value).doubleValue();
        }
        throw new IllegalArgumentException("Cannot convert value of type " + value.getClass().toGenericString() + " to a double");
    }

    private static Float tryConvToFloat(Object value) {
        if (Number.class.isAssignableFrom(value.getClass())) {
            return ((Number)value).floatValue();
        }
        throw new IllegalArgumentException("Cannot convert value of type " + value.getClass().toGenericString() + " to a float");
    }

    private static Long tryConvToLong(Object value) {
        if (Number.class.isAssignableFrom(value.getClass())) {
            return ((Number)value).longValue();
        }
        throw new IllegalArgumentException("Cannot convert value of type " + value.getClass().toGenericString() + " to a long");
    }
}
