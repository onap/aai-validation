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
package org.onap.aai.validation.ruledriven.configuration;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.onap.aai.validation.ruledriven.configuration.build.EntityBuilder;

/**
 * These tests are required to ensure code coverage by unit testing
 *
 */
public class TestConfigFileBuilder {

    static {
        System.setProperty("APP_HOME", ".");
    }

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    /**
     * Validate that a content builder accepts a numeric value
     *
     * @throws Exception
     */
    @Test
    public void testNumberItem() throws Exception {
        ConfigFileBuilder builder = new ConfigFileBuilder(testFolder);
        EntityBuilder ab = new EntityBuilder();
        ab.appendValue("dummy", 3);
        ab.appendValue("dummy2", 5L);
        builder.addContent(ab);
        builder.toString();
    }

    @Test
    public void testToString() throws Exception {
        ConfigFileBuilder builder = new ConfigFileBuilder(testFolder);
        builder.addContent(new EntityBuilder());
        builder.toString();
    }
}
