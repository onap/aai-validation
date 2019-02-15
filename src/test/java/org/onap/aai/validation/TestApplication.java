/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright (c) 2018-2019 AT&T Intellectual Property. All rights reserved.
 * Copyright (c) 2018-2019 European Software Marketing Ltd.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.aai.validation;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

/**
 * Invoke the Spring Boot Application (primarily for code coverage).
 *
 */
@SpringBootTest(classes = ValidationServiceApplication.class)
@TestPropertySource(locations = {"classpath:oxm-reader/schemaIngest.properties", "classpath:application.properties"})
@ContextConfiguration(locations = {"classpath:validation-service-beans.xml"})
public class TestApplication {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Before
    public void init() {
        System.setProperty("CONFIG_HOME", "src/test/resources/model-validation/instance-validator");
        System.setProperty("APP_HOME", ".");
        System.setProperty("schema.translator.list", "config");
        System.clearProperty("KEY_STORE_PASSWORD");
    }

    @Test
    public void testApplicationWithNullArgs() {
        System.setProperty("KEY_STORE_PASSWORD", "test");
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Args must not be null");
        ValidationServiceApplication.main(null);
    }

    @Test
    public void testApplicationWithNullKeyStorePassword() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("roperty KEY_STORE_PASSWORD not set");
        ValidationServiceApplication.main(new String[] {});
    }

}
