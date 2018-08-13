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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.nio.file.InvalidPathException;
import java.util.List;
import java.util.Map;
import java.util.ServiceConfigurationError;
import org.junit.Test;
import org.onap.aai.setup.SchemaLocationsBean;
import org.onap.aai.setup.Version;
import org.onap.aai.validation.reader.OxmConfigTranslator;
import org.springframework.test.util.ReflectionTestUtils;

public class TestOxmConfigTranslator {

    static {
        System.setProperty("APP_HOME", ".");
    }

    @Test
    public void testOxmFiles() {
        OxmConfigTranslator translator = buildConfigTranslator("src/test/resources/oxm-reader/single/");
        Map<Version, List<String>> latestVersion = translator.getNodeFiles();
        assertThat(latestVersion.size(), is(3));
        assertThat(latestVersion.values().iterator().next().size(), is(1));
        assertThat(latestVersion.values().iterator().next().get(0), not(isEmptyString()));
        Map<Version, List<String>> latestVersion1 = translator.getEdgeFiles();
        assertThat(latestVersion1.size(), is(1));
        assertThat(latestVersion1.values().iterator().next().size(), is(1));
        assertThat(latestVersion1.values().iterator().next().get(0), not(isEmptyString()));
    }

    @Test
    public void testMultipleOxmFilesPerVersion() {
        OxmConfigTranslator translator = buildConfigTranslator("src/test/resources/oxm-reader/multiple");
        Map<Version, List<String>> latestVersion = translator.getNodeFiles();
        assertThat(latestVersion.size(), is(2));
        assertThat(latestVersion.values().iterator().next().size(), is(2));
        assertThat(latestVersion.values().iterator().next().get(0), not(isEmptyString()));
        Map<Version, List<String>> latestVersion1 = translator.getEdgeFiles();
        assertThat(latestVersion1.size(), is(0));
    }

    @Test
    public void testZeroMatchingFiles() {
        OxmConfigTranslator translator = buildConfigTranslator("src/test/resources/oxm-reader/");
        Map<Version, List<String>> versionsMap = translator.getNodeFiles();
        assertThat(versionsMap.size(), is(0));
    }

    @Test(expected = ServiceConfigurationError.class)
    public void testNullNodesPath() {
        buildConfigTranslator(null).getNodeFiles();
    }

    @Test(expected = ServiceConfigurationError.class)
    public void testNullEdgesPath() {
        buildConfigTranslator(null).getEdgeFiles();
    }

    @Test(expected = ServiceConfigurationError.class)
    public void testNonExistentNodesPath() {
        buildConfigTranslator("no-such-folder-exists/").getNodeFiles();
    }

    @Test(expected = ServiceConfigurationError.class)
    public void testNonExistentEdgesPath() {
        SchemaLocationsBean bean = new SchemaLocationsBean();
        ReflectionTestUtils.setField(bean, "nodeDirectory", "src/test/resources/oxm-reader/");
        ReflectionTestUtils.setField(bean, "edgeDirectory", "no-such-folder-exists/");
        new OxmConfigTranslator(bean).getEdgeFiles();
    }

    @Test(expected = InvalidPathException.class)
    public void testInvalidPath() {
        buildConfigTranslator("\0").getEdgeFiles();
    }

    private OxmConfigTranslator buildConfigTranslator(String path) {
        return new OxmConfigTranslator(createSchemaLocationsBean(path));
    }

    private SchemaLocationsBean createSchemaLocationsBean(String path) {
        SchemaLocationsBean bean = new SchemaLocationsBean();
        ReflectionTestUtils.setField(bean, "nodeDirectory", path);
        ReflectionTestUtils.setField(bean, "edgeDirectory", path);
        return bean;
    }

}
