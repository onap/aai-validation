/**
 * ============LICENSE_START===================================================
 * Copyright (c) 2018-2019 European Software Marketing Ltd.
 * ============================================================================
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
 * ============LICENSE_END=====================================================
 */
package org.onap.aai.validation.publisher;

import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import org.onap.aai.validation.exception.ValidationServiceException;
import org.onap.aai.validation.result.ValidationResult;
import org.onap.aai.validation.result.ValidationResultBuilder;
import org.onap.aai.validation.test.util.TestEntity;
import org.onap.aai.validation.test.util.ValidationResultIsEqual;

/**
 * Will be injected by Spring
 *
 */
public class MockEventPublisher implements MessagePublisher {

    private ValidationResult expectedValidationResult;
    private String testDescription;
    private boolean publishedMessage;

    public MockEventPublisher() {
        // Deliberately empty - no configuration needed
    }

    public void setTestEntity(TestEntity entity) throws URISyntaxException, IOException {
        this.expectedValidationResult = entity.getExpectedValidationResult();
        this.publishedMessage = false;
    }

    public void setTestDescription(String testDescription) {
        this.testDescription = testDescription;
    }

    @Override
    public void publishMessage(String message) throws ValidationServiceException {
        assertThat(testDescription, ValidationResultBuilder.fromJson(message),
                ValidationResultIsEqual.equalTo(expectedValidationResult));
        publishedMessage = true;
    }

    @Override
    public void publishMessages(Collection<String> messages) throws ValidationServiceException {
        for (String message : messages) {
            publishMessage(message);
        }
    }

    public boolean processedSuccessfully() {
        return publishedMessage || expectedValidationResult == null;
    }
}
