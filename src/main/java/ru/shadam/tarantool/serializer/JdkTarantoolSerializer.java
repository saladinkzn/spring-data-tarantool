package ru.shadam.tarantool.serializer;

import org.springframework.core.convert.converter.Converter;
import org.springframework.core.serializer.support.DeserializingConverter;
import org.springframework.core.serializer.support.SerializingConverter;
import org.springframework.util.Assert;

import java.util.Base64;
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
            String base64string = Base64.getEncoder().encodeToString(serializer.convert(value));
            return Collections.singletonList(base64string);
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

        String onlyElement = (String) value.get(0);
        byte[] bytes = Base64.getDecoder().decode(onlyElement);
        try {
            return deserializer.convert(bytes);
        } catch (Exception ex) {
            throw new RuntimeException("Cannot deserialize", ex);
        }
    }
}
