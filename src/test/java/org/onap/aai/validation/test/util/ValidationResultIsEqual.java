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
package org.onap.aai.validation.test.util;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.onap.aai.validation.result.ValidationResult;

/**
 * Matcher for comparing actual and expected ValidationResults
 *
 */
public class ValidationResultIsEqual extends BaseMatcher<ValidationResult> {
    private final ValidationResult expected;

    public static Matcher<? super ValidationResult> equalTo(ValidationResult validationResult) {
        return new ValidationResultIsEqual(validationResult);
    }

    private ValidationResultIsEqual(ValidationResult expected) {
        this.expected = expected;
    }

    @Override
    public boolean matches(Object o) {
        if (expected == null) {
            return false;
        }
        ValidationResult actual = (ValidationResult) o;
        return actual.getEntityId().equals(expected.getEntityId()) && //
                actual.getEntityType().equals(expected.getEntityType()) && //
                (actual.getEntityLink() == null ? "" : actual.getEntityLink()).equals(expected.getEntityLink()) && //
                actual.getResourceVersion().equals(expected.getResourceVersion()) && //
                actual.getViolations().equals(expected.getViolations());
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(expected == null ? "<not defined>" : expected.toString());
    }

}
