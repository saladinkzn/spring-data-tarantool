package ru.shadam.tarantool.serializer;

import org.springframework.core.convert.converter.Converter;
import org.springframework.core.serializer.support.DeserializingConverter;
import org.springframework.core.serializer.support.SerializingConverter;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.List;

/**
 * Imported from spring-data-redis
 *
 * @author Mark Pollack
 * @author Costin Leau
 * @author Mark Paluch
 */
public class JdkTarantoolSerializer implements TarantoolSerializer<Object> {
    private final Converter<Object, byte[]> serializer;
    private final Converter<byte[], Object> deserializer;

    public JdkTarantoolSerializer() {
        this(new SerializingConverter(), new DeserializingConverter());
    }

    public JdkTarantoolSerializer(Converter<Object, byte[]> serializer, Converter<byte[], Object> deserializer) {
        Assert.notNull("Serializer must not be null");
        Assert.notNull("Deserializer must not be null");

        this.serializer = serializer;
        this.deserializer = deserializer;
    }

    @Override
    public List serialize(Object value) {
        if(value == null) {
            return Collections.singletonList(null);
        }

        try {
            return Collections.singletonList(serializer.convert(value));
        } catch (Exception ex) {
            throw new RuntimeException("Cannot serialize", ex);
        }
    }

    @Override
    public Object deserialize(List value) {
        if(value.isEmpty()) {
            return null;
        }

        if(value.get(0) == null) {
            return null;
        }

        byte[] onlyElement = (byte[]) value.get(0);
        try {
            return deserializer.convert(onlyElement);
        } catch (Exception ex) {
            throw new RuntimeException("Cannot deserialize", ex);
        }
    }
}
