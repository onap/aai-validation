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

import java.io.IOException;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Invoke the Spring Boot Application (primarily for code coverage).
 *
 */
@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:data-dictionary/test-data-dictionary-beans.xml"})
@TestPropertySource(locations = {"classpath:schema-ingest.properties", "classpath:test-application.properties"})
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

    @Test
    public void testApplicationWithEmptyKeyStorePassword() {
        System.setProperty("KEY_STORE_PASSWORD", "");
        final CauseMatcher expectedCause = new CauseMatcher(IOException.class, "password was incorrect");
        expectedEx.expectCause(expectedCause);
        ValidationServiceApplication.main(new String[] {});
    }

    @Test
    public void testApplicationWithIncorrectKeyStorePassword() {
        System.setProperty("KEY_STORE_PASSWORD", "test");
        final CauseMatcher expectedCause = new CauseMatcher(IOException.class, "password was incorrect");
        expectedEx.expectCause(expectedCause);
        ValidationServiceApplication.main(new String[] {});
    }

    private static class CauseMatcher extends TypeSafeMatcher<Throwable> {

        private final Class<? extends Throwable> type;
        private final String expectedMessage;

        public CauseMatcher(Class<? extends Throwable> type, String expectedMessage) {
            this.type = type;
            this.expectedMessage = expectedMessage;
        }

        @Override
        protected boolean matchesSafely(Throwable item) {
            return item.getClass().isAssignableFrom(type) && item.getMessage().contains(expectedMessage);
        }

        @Override
        public void describeTo(Description description) {
            description.appendValue(type).appendText(" and message ").appendValue(expectedMessage);
        }
    }
}
