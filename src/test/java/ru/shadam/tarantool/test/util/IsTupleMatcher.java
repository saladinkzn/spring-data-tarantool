/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.shadam.tarantool.test.util;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import ru.shadam.tarantool.core.convert.Path;
import ru.shadam.tarantool.core.convert.Tuple;

import java.util.*;

/**
 * {@link TypeSafeMatcher} implementation for checking contents of {@link Tuple}.
 *
 * @author Christoph Strobl
 * @since 1.7
 */
public class IsTupleMatcher extends TypeSafeMatcher<Tuple> {

    Map<Path, Object> expected = new LinkedHashMap<>();
    Set<Path> without = new LinkedHashSet<>();

    /*
     * (non-Javadoc)
     * @see org.hamcrest.SelfDescribing#describeTo(org.hamcrest.Description)
     */
    @Override
    public void describeTo(Description description) {

        if (!expected.isEmpty()) {
            description.appendValueList("Expected Bucket content [{", "},{", "}].", expected.entrySet());
        }
        if (!without.isEmpty()) {
            description.appendValueList("Expected Bucket to not include [", ",", "].", without);
        }

    }

    /*
     * (non-Javadoc)
     * @see org.hamcrest.TypeSafeMatcher#matchesSafely(java.lang.Object)
     */
    @Override
    protected boolean matchesSafely(Tuple bucket) {

        if (bucket == null) {
            return false;
        }

        if (bucket.isEmpty() && expected.isEmpty()) {
            return true;
        }

        for (Path notContained : without) {
            Object value = bucket.get(notContained);
            if (value != null) {
                return false;
            }
        }

        for (Map.Entry<Path, Object> entry : expected.entrySet()) {

            Object actualValue = bucket.get(entry.getKey());
            Object expectedValue = entry.getValue();
            if (expectedValue == null && actualValue != null) {
                return false;
            }

            if (expectedValue != null && actualValue == null) {
                return false;
            }
        }

        return true;
    }

    /**
     * Creates new {@link IsTupleMatcher}.
     *
     * @return
     */
    public static IsTupleMatcher isTuple() {
        return new IsTupleMatcher();
    }

    /**
     * Checks for presence of type hint at given path.
     *
     * @param path
     * @param type
     * @return
     */
    public IsTupleMatcher containingTypeHint(Path path, Class<?> type) {

        this.expected.put(path, type);
        return this;
    }

    /**
     * Checks for presence of equivalent String value at path.
     *
     * @param path
     * @param value
     * @return
     */
    public IsTupleMatcher containingUtf8String(Path path, String value) {

        this.expected.put(path, value);
        return this;
    }

    /**
     * Checks for presence of given value at path.
     *
     * @param path
     * @param value
     * @return
     */
    public IsTupleMatcher containing(Path path, Object value) {

        this.expected.put(path, value);
        return this;
    }

    public IsTupleMatcher matchingPath(Path path, Matcher<Object> matcher) {

        this.expected.put(path, matcher);
        return this;
    }

    /**
     * Checks for presence of equivalent time in msec value at path.
     *
     * @param path
     * @param date
     * @return
     */
    public IsTupleMatcher containingDateAsMsec(Path path, Date date) {

        this.expected.put(path, date);
        return this;
    }

    /**
     * Checks given path is not present.
     *
     * @param path
     * @return
     */
    public IsTupleMatcher without(Path path) {
        this.without.add(path);
        return this;
    }

}
