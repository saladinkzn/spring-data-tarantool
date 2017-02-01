package ru.shadam.tarantool.serializer;

import org.junit.Assert;
import org.junit.Test;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author sala
 */
public class PlainTarantoolSerializerTest {
    @Test
    public void testSerializeEnum() {
        PlainTarantoolSerializer<TestEnum> serializer = new PlainTarantoolSerializer<>(TestEnum.class);
        final List tuple = serializer.serialize(TestEnum.FIRST);
        Assert.assertEquals("FIRST", tuple.get(0));
    }

    @Test
    public void testSerializeDate() {
        final PlainTarantoolSerializer<Date> datePlainTarantoolSerializer = new PlainTarantoolSerializer<>(Date.class);
        Calendar calendar = Calendar.getInstance();
        calendar.set(2017, 1, 1);
        final Date date = calendar.getTime();
        final List serialized = datePlainTarantoolSerializer.serialize(date);
        Assert.assertEquals(date.getTime(), serialized.get(0));
    }

    /**
     * Tarantool returns inserted number as minimal size Number
     */
    @Test
    public void deserializeLongFromInt() {
        final PlainTarantoolSerializer<Long> longPlainTarantoolSerializer = new PlainTarantoolSerializer<>(Long.class);

        final Long deserialized = longPlainTarantoolSerializer.deserialize(Collections.singletonList(1));
        Assert.assertEquals(1L, deserialized.longValue());
    }

    private enum TestEnum { FIRST; }
}