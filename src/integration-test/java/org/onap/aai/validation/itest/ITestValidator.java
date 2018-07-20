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
package org.onap.aai.validation.itest;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import javax.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.aai.validation.controller.ValidationController;
import org.onap.aai.validation.controller.ValidationController.Result;
import org.onap.aai.validation.exception.ValidationServiceError;
import org.onap.aai.validation.exception.ValidationServiceException;
import org.onap.aai.validation.logging.LogHelper;
import org.onap.aai.validation.logging.LogReader;
import org.onap.aai.validation.publisher.MockEventPublisher;
import org.onap.aai.validation.ruledriven.validator.TestRuleDrivenValidator;
import org.onap.aai.validation.services.ValidateServiceImpl;
import org.onap.aai.validation.test.util.TestEntity;
import org.onap.aai.validation.test.util.ValidationResultIsEqual;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Integration test using a mocked message publisher which compares the actual validation result with an expected
 * result. This test invokes the validation controller directly.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(properties = {"schemaIngestPropLoc = src/test/resources/oxm-reader/schemaIngest.properties"})
@ContextConfiguration(locations = {"classpath:" + ITestValidator.TEST_FOLDER + "/itest-mock-validator-beans.xml"})
public class ITestValidator {

    static {
        System.setProperty("AJSC_HOME", ".");
    }

    public static final String TEST_FOLDER = "mock-validator-beans";
    private static final String SYSTEM_TEST_FOLDER = "system_test";

    @Inject
    ValidationController validationController;

    @Inject
    MockEventPublisher messagePublisher;

    @Before
    public void setUp() throws ValidationServiceException {
        validationController.initialise();
    }

    /**
     * @throws ValidationServiceException
     */
    @Test
    public void testValidateRuleDrivenSystemTestInstances() throws ValidationServiceException {
        testEntities("/data/rule-driven", "/results/expected/rule-driven");
    }

    /**
     * @throws ValidationServiceException
     */
    @Test
    public void testValidateModelDrivenSystemTestInstances() throws ValidationServiceException {
        testEntities("/data/model-driven", "/results/expected/model-driven");
    }

    /**
     * @param inputDataPath
     * @param resultsDataPath
     * @throws ValidationServiceException
     */
    private void testEntities(String inputDataPath, String resultsDataPath) throws ValidationServiceException {
        try {
            List<TestEntity> testEntities =
                    TestRuleDrivenValidator.getEntities(SYSTEM_TEST_FOLDER, inputDataPath, resultsDataPath);
            for (TestEntity entity : testEntities) {
                testValidation(entity);
            }
        } catch (URISyntaxException | IOException e) {
            throw new ValidationServiceException(ValidationServiceError.RULE_EXECUTION_ERROR, e);
        }
    }

    /**
     * @param entity
     * @throws ValidationServiceException
     * @throws IOException
     */
    private void testValidation(TestEntity entity) throws ValidationServiceException, IOException {
        LogReader errorReader = new LogReader(LogHelper.getLogDirectory(), "error");

        try {
            messagePublisher.setTestEntity(entity);
            messagePublisher.setTestDescription(entity.inputFile.getAbsolutePath());
            Result result = validationController.execute(entity.getJson(), "itest");
            assertThat("Message publishing error: success response", messagePublisher.processedSuccessfully(),
                    is(true));
            if (result.getValidationResults().isEmpty()) {
                assertThat(entity.expectedResultsFile, entity.getExpectedValidationResult(), is(nullValue()));
                if (entity.expectsError()) {
                    String expectedErrorMessage = entity.getExpectedErrorMessage();
                    if (!expectedErrorMessage.contains(ValidateServiceImpl.DEFAULT_MESSAGE_FOR_FILTERED_EVENTS)) {
                        String s = errorReader.getNewLines();
                        assertThat(s, is(notNullValue()));
                        assertThat(entity.inputFile.toString(), s, containsString(expectedErrorMessage));
                    }
                }
            } else {
                assertThat(result.getValidationResults().get(0),
                        is(ValidationResultIsEqual.equalTo(entity.getExpectedValidationResult())));
            }
        } catch (JsonSyntaxException | URISyntaxException | IOException e) {
            throw new ValidationServiceException(ValidationServiceError.RULE_EXECUTION_ERROR, e);
        }
    }

}
