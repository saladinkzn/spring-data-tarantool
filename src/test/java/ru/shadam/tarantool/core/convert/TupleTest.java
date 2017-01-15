package ru.shadam.tarantool.core.convert;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

/**
 * @author sala
 */
public class TupleTest {

    @Test
    public void testGetSetOps() {
        Tuple tuple = new Tuple();
        tuple.set(Path.of(0), "lol");
        tuple.set(Path.of(1), Collections.singletonList("def"));
        Assert.assertEquals("lol", tuple.get(Path.of(0)));
        Assert.assertEquals("def", tuple.get(Path.of(1, 0)));
    }

    @Test
    public void testSet() {
        Tuple tuple = new Tuple();

        tuple.set(Path.of(8, 1), "abc");
    }

}