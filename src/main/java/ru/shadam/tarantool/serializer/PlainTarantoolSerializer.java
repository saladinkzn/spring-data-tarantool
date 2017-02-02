package ru.shadam.tarantool.serializer;

import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.ConfigurableConversionService;
import ru.shadam.tarantool.core.convert.TarantoolDefaultConversionService;

import java.lang.reflect.Array;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author sala
 */
public class PlainTarantoolSerializer<T> implements TarantoolSerializer<T> {
    private static final Class<?>[] NATIVE_TYPES = new Class<?>[] {
        Boolean.class, Number.class, String.class, byte[].class
    };
    private final Class<T> desiredClass;
    private final ConfigurableConversionService conversionService;


    public PlainTarantoolSerializer(Class<T> desiredClass) {
        this(desiredClass, new TarantoolDefaultConversionService());
    }

    public PlainTarantoolSerializer(Class<T> desiredClass, ConfigurableConversionService conversionService) {
        this.desiredClass = desiredClass;
        this.conversionService = conversionService;

        conversionService.addConverter(Date.class, Number.class, (Converter<Date, Number>) Date::getTime);
        conversionService.addConverter(Number.class, Date.class, (Converter<Number, Date>) it -> new Date(it.longValue()));
    }

    @Override
    public List serialize(T value) {
        return Collections.singletonList(convertToTarantoolNative(value));
    }

    @Override
    public T deserialize(List value) {
        if(value == null) {
            return null;
        }

        if(value.size() != 1) {
            throw new IllegalArgumentException("Tuples are not supported as keys by this serializer");
        }
        return conversionService.convert(value.get(0), desiredClass);
    }

    public Object convertToTarantoolNative(Object item) {
        if(supported(item)) {
            return item;
        }

        final Class<?> sourceType = item.getClass();
        for(Class<?> nativeType: NATIVE_TYPES) {
            if(conversionService.canConvert(sourceType, nativeType)) {
                return conversionService.convert(item, nativeType);
            }
        }
        return null;
    }

    public boolean supported(Object item) {
        if(item == null) {
            return true;
        } else if (item instanceof Boolean) {
            return true;
        } else if (item instanceof Number) {
            return true;
        } else if (item instanceof String) {
            return true;
        } else if (item instanceof byte[]) {
            return true;
        } else if (item instanceof List) {
            return ((List<?>) item).stream().allMatch(this::supported);
        } else if (item.getClass().isArray()) {
            int length = Array.getLength(item);
            for (int i = 0; i < length; i++) {
                supported(Array.get(item, i));
            }
        } else if (item instanceof Map) {
            final Map<Object, Object> map = (Map<Object, Object>) item;
            return map.entrySet().stream().allMatch(e -> supported(e.getKey()) && supported(e.getValue()));
        }
        return false;
    }
}
