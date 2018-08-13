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
package org.onap.aai.validation.reader;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.onap.aai.validation.reader.JsonReader;
import org.onap.aai.validation.test.util.TestUtil;

public class TestJsonReader {

    static {
        System.setProperty("APP_HOME", ".");
    }

    enum TestData {
        // @formatter:off
		SAMPLE_JSON ("json-reader/sample.json");

		private String filename;
		TestData(String filename) {this.filename = filename;}
		public String getFilename() {return this.filename;}
		// @formatter:on
    }

    @Test
    public void testGetString() throws Exception {
        String json = TestUtil.getFileAsString(TestData.SAMPLE_JSON.getFilename());
        JsonReader jsonReader = new JsonReader();
        List<String> result = jsonReader.get(json, "$.event-header.entity-type");
        assertThat(result.get(0), is("vserver"));
    }

    @Test
    public void testGetInteger() throws Exception {
        String json = TestUtil.getFileAsString(TestData.SAMPLE_JSON.getFilename());
        JsonReader jsonReader = new JsonReader();
        List<String> result = jsonReader.get(json, "$.event-header.sample-integer");
        assertThat(result.get(0), is("1"));
    }

    @Test
    public void testGetBoolean() throws Exception {
        String json = TestUtil.getFileAsString(TestData.SAMPLE_JSON.getFilename());
        JsonReader jsonReader = new JsonReader();
        List<String> result = jsonReader.get(json, "$.event-header.sample-boolean");
        assertThat(result.get(0), is("true"));
    }

    @Test
    public void testGetObjectAsString() throws Exception {
        String json = TestUtil.getFileAsString(TestData.SAMPLE_JSON.getFilename());
        JsonReader jsonReader = new JsonReader();
        List<String> result = jsonReader.get(json, "$.event-header.sample-object");

        assertThat(result.get(0), is("{\"property\":\"value\"}"));
    }

    @Test
    public void testGetArrayAsString() throws Exception {
        String json = TestUtil.getFileAsString(TestData.SAMPLE_JSON.getFilename());
        JsonReader jsonReader = new JsonReader();
        List<String> result = jsonReader.get(json, "$.event-header.sample-array");

        assertThat(result, is(Arrays.asList("one")));
    }

    @Test
    public void testGetWithInvalidPath() throws Exception {
        String json = TestUtil.getFileAsString(TestData.SAMPLE_JSON.getFilename());
        JsonReader jsonReader = new JsonReader();
        List<String> result = jsonReader.get(json, "$.unknown");
        assertThat(result, empty());
    }
}
