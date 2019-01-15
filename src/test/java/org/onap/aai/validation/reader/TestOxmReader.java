/*
 * ============LICENSE_START===================================================
 * Copyright (c) 2018-2019 Amdocs
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

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;
import javax.inject.Inject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(locations = {"classpath:oxm-reader/schemaIngest.properties"})
@ContextConfiguration(locations = {"classpath:oxm-reader/oxm-reader-beans.xml"})
public class TestOxmReader {

    static {
        System.setProperty("APP_HOME", ".");
    }

    @Inject
    private OxmReader oxmReader;

    @Test
    public void testGetPrimaryKeysSingleKey() throws Exception {
        List<String> primaryKeys = oxmReader.getPrimaryKeys("connector");
        assertThat(primaryKeys.get(0), is("resource-instance-id"));
    }

    @Test
    public void testGetPrimaryKeysMultipleKeys() throws Exception {
        List<String> primaryKeys = oxmReader.getPrimaryKeys("cloud-region");
        assertThat(primaryKeys, contains("cloud-owner", "cloud-region-id"));
    }

    @Test
    public void testGetPrimaryKeysUnknownObject() throws Exception {
        List<String> primaryKeys = oxmReader.getPrimaryKeys("most-surely-does-not-exist");
        assertThat(primaryKeys, empty());
    }
}
