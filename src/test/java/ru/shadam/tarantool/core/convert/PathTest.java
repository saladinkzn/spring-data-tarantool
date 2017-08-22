package ru.shadam.tarantool.core.convert;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author sala
 */
public class PathTest {
    @Test
    public void testConcat() {
        Path path = Path.concat(Path.of(1), 2);
        Assert.assertEquals(Path.of(1, 2), path);
    }

}