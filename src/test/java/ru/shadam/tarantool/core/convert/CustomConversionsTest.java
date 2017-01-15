package ru.shadam.tarantool.core.convert;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author sala
 */
public class CustomConversionsTest {
    private CustomConversions customConversions;

    @Before
    public void setUp() {
        customConversions = new CustomConversions();
    }

    @Test
    public void hasCustomWriteTarget() throws Exception {
        Assert.assertTrue(customConversions.hasCustomWriteTarget(byte[].class));
    }

}