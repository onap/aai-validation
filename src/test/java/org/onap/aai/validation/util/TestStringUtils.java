/*
 * ============LICENSE_START===================================================
 * Copyright (c) 2018 Amdocs
 * ============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=====================================================
 */
package org.onap.aai.validation.util;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.onap.aai.validation.exception.ValidationServiceException;
import org.onap.aai.validation.util.StringUtils;

public class TestStringUtils {

    static {
        System.setProperty("APP_HOME", ".");
    }

    @Test
    public void testStripSinglePrefix() throws Exception {
        String prefixDelimiter = ".";
        String prefix = "prefix";
        String suffix = "suffix";
        String testString = prefix + prefixDelimiter + suffix;

        String result = StringUtils.stripPrefix(testString, prefixDelimiter);

        assertEquals("Prefix incorrectly stripped.", suffix, result);
    }

    @Test
    public void testStripMultiplePrefix() throws Exception {
        String prefixDelimiter = "***";
        String prefix = "prefix";
        String suffix = "suffix";
        String testString = prefix + prefixDelimiter + prefix + prefixDelimiter + suffix;

        String result = StringUtils.stripPrefix(testString, prefixDelimiter);

        assertEquals("Prefix incorrectly stripped.", suffix, result);
    }

    @Test
    public void testStripSubstr() {
        List<String> stringList = Arrays.asList("/text()one", "tw/text()o", "three/text()");
        List<String> stripSubstr = StringUtils.stripSuffix(stringList, "/text()");
        assertThat(stripSubstr, containsInAnyOrder("/text()one", "tw/text()o", "three"));
    }

    @Test
    public void testStripStringRegex() throws Exception {
        String prefixDelimiter = "/aai/v8/";
        String prefix = "prefix";
        String suffix = "suffix";
        String testString = prefix + prefixDelimiter + suffix;
        String regex = "\\/aai\\/v[0-9]*\\/";

        String stripString = StringUtils.stripPrefixRegex(testString, regex);

        assertThat(stripString, is(suffix));
    }

    @Test
    public void testStripStringRegexNotFound() throws Exception {
        String prefixDelimiter = "delimiter";
        String prefix = "prefix";
        String suffix = "suffix";
        String testString = prefix + prefixDelimiter + suffix;
        String regex = "\\/aai\\/v[0-9]*\\/";

        String stripString = StringUtils.stripPrefixRegex(testString, regex);

        assertThat(stripString, is(testString));
    }

    @Test
    public void testStripStringRegexMultiplePrefix() throws Exception {
        String prefixDelimiter = "/aai/v8/";
        String prefix = "prefix";
        String suffix = "text" + prefixDelimiter + "text";
        String testString = prefix + prefixDelimiter + suffix;
        String regex = "\\/aai\\/v[0-9]*\\/";

        String stripString = StringUtils.stripPrefixRegex(testString, regex);

        assertThat(stripString, is(suffix));
    }

    @Test
    public void testStripStringRegexNullString() throws Exception {
        String testString = null;
        String regex = "\\/aai\\/v[0-9]*\\/";

        String stripString = StringUtils.stripPrefixRegex(testString, regex);

        assertThat(stripString, nullValue());
    }

    @Test
    public void testStripStringRegexEmptyString() throws Exception {
        String testString = "";
        String regex = "\\/aai\\/v[0-9]*\\/";

        String stripString = StringUtils.stripPrefixRegex(testString, regex);

        assertThat(stripString, is(testString));
    }

    @Test
    public void testStripStringRegexNullRegex() throws Exception {
        String testString = "test";
        String regex = null;

        String stripString = StringUtils.stripPrefixRegex(testString, regex);

        assertThat(stripString, is(testString));
    }

    @Test
    public void testStripStringRegexEmptyRegex() throws Exception {
        String testString = "test";
        String regex = "";

        String stripString = StringUtils.stripPrefixRegex(testString, regex);

        assertThat(stripString, is(testString));
    }

    @Test(expected = ValidationServiceException.class)
    public void testStripStringRegexInvalidRegex() throws Exception {
        String prefixDelimiter = "/aai/v8/";
        String prefix = "prefix";
        String suffix = "suffix";
        String testString = prefix + prefixDelimiter + suffix;
        String regex = "***";

        StringUtils.stripPrefixRegex(testString, regex);
    }

    @Test
    public void testStripStringRegexLimitedPrefix() throws Exception {
        String prefixDelimiter = "/aai/v8/";
        String suffix = "suffix";
        String testString = prefixDelimiter + suffix;
        String regex = "\\/aai\\/v[0-9]*\\/";

        String stripString = StringUtils.stripPrefixRegex(testString, regex);

        assertThat(stripString, is(suffix));
    }
}
