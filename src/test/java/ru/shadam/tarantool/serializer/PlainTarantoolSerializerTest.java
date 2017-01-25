package ru.shadam.tarantool.serializer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author sala
 */
public class PlainTarantoolSerializerTest {
    private PlainTarantoolSerializer<List> serializer;

    @Before
    public void prepare() {
        serializer = new PlainTarantoolSerializer<>();
    }

    @Test
    public void testSerializeEnum() {
        final ArrayList tuple = new ArrayList();
        tuple.add(TestEnum.FIRST);
        final List serialized = serializer.serialize(tuple);
        Assert.assertEquals("FIRST", tuple.get(0));
    }

    @Test
    public void testSerializeDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2017, 1, 1);
        final Date date = calendar.getTime();
        final ArrayList tuple = new ArrayList();
        tuple.add(date);
        final List serialized = serializer.serialize(tuple);
        Assert.assertEquals(date.getTime(), serialized.get(0));
    }

    private enum TestEnum { FIRST; }
}