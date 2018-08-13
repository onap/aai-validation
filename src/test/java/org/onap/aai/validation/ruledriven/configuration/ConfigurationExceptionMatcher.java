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

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.onap.aai.validation.ruledriven.configuration.GroovyConfigurationException;

public class ConfigurationExceptionMatcher {
    public static Matcher<GroovyConfigurationException> hasInvalidToken(final String expectedToken) {
        return new TypeSafeMatcher<GroovyConfigurationException>() {
            private String foundToken;

            @Override
            public void describeTo(Description description) {
                description.appendText("result from getInvalidToken() equals \"").appendText(expectedToken)
                        .appendText("\"");
            }

            @Override
            public void describeMismatchSafely(final GroovyConfigurationException exception,
                    final Description mismatchDescription) {
                mismatchDescription.appendText("was ").appendValue(foundToken);
            }

            @Override
            protected boolean matchesSafely(GroovyConfigurationException exception) {
                foundToken = exception.getInvalidToken();
                return foundToken != null && foundToken.equalsIgnoreCase(expectedToken);
            }
        };
    }

    public static Matcher<GroovyConfigurationException> configTextContains(final String expectedConfigText) {
        return new TypeSafeMatcher<GroovyConfigurationException>() {
            private String foundConfigText;

            @Override
            public void describeTo(Description description) {
                description.appendText("result from getConfigText() containing \"").appendText(expectedConfigText)
                        .appendText("\"");
            }

            @Override
            public void describeMismatchSafely(final GroovyConfigurationException exception,
                    final Description mismatchDescription) {
                mismatchDescription.appendText("was ").appendValue(foundConfigText);
            }

            @Override
            protected boolean matchesSafely(GroovyConfigurationException exception) {
                foundConfigText = exception.getConfigText();
                return foundConfigText != null && foundConfigText.contains(expectedConfigText);
            }
        };
    }
}
