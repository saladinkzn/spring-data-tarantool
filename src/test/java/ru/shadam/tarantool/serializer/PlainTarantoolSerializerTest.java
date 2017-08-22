package ru.shadam.tarantool.serializer;

import org.junit.Assert;
import org.junit.Test;

import java.time.ZoneId;
import java.util.*;

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

    @Test
    public void testDeserializeDate() {
        PlainTarantoolSerializer<Date> serializer = new PlainTarantoolSerializer<>(Date.class);
        Date date = serializer.deserialize(Collections.singletonList(1503430935));

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone(ZoneId.of("Europe/Moscow")));
        calendar.set(2017, Calendar.AUGUST, 22, 22, 42, 15);

        Assert.assertEquals(calendar.getTimeInMillis() / 1000, date.getTime());
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