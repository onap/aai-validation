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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.core.Response.Status;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.aai.validation.controller.ValidationController;
import org.onap.aai.validation.exception.ValidationServiceError;
import org.onap.aai.validation.exception.ValidationServiceException;
import org.onap.aai.validation.publisher.MockEventPublisher;
import org.onap.aai.validation.result.ValidationResult;
import org.onap.aai.validation.ruledriven.validator.TestRuleDrivenValidator;
import org.onap.aai.validation.services.ValidateService;
import org.onap.aai.validation.services.ValidateServiceImpl;
import org.onap.aai.validation.test.util.TestEntity;
import org.onap.aai.validation.test.util.ValidationResultIsEqual;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Integration test using a mocked message publisher which compares the actual validation result with an expected
 * result. This test invokes the validation controller directly.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(properties = {"schemaIngestPropLoc = src/test/resources/oxm-reader/schemaIngest.properties"})
@ContextConfiguration(
        locations = {"classpath:" + ITestValidatorHttpInterface.TEST_FOLDER + "/itest-mock-validator-beans.xml"})
public class ITestValidatorHttpInterface {
    public static final String TEST_FOLDER = "mock-validator-beans";
    private static final String SYSTEM_TEST_FOLDER = "system_test";

    private ValidateService httpService;

    @Inject
    ValidationController validationController;

    @Inject
    MockEventPublisher messagePublisher;

    static {
        System.setProperty("AJSC_HOME", ".");
    }

    @Before
    public void setUp() throws ValidationServiceException {
        validationController.initialise();
        httpService = new ValidateServiceImpl(validationController, null); // Added this as part of a hotfix. Needs
                                                                           // proper handling.
    }

    /**
     * @throws ValidationServiceException
     */
    @Test
    public void testValidateRuleDrivenSystemTestInstances() throws ValidationServiceException {
        testEntities("/data/rule-driven", "/results/expected/rule-driven");
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
        } catch (URISyntaxException e) {
            throw new ValidationServiceException(ValidationServiceError.RULE_EXECUTION_ERROR, e);
        }
    }

    /**
     * @param entity
     * @throws ValidationServiceException
     */
    private void testValidation(TestEntity entity) throws ValidationServiceException {
        try {
            messagePublisher.setTestEntity(entity);
            messagePublisher.setTestDescription(entity.inputFile.getAbsolutePath());

            // Invoke the HTTP interface directly
            ResponseEntity<String> response = httpService.validate(entity.getJson());
            assertThat("Message publishing error for " + entity.inputFile, messagePublisher.processedSuccessfully(),
                    is(true));

            if (response.getStatusCodeValue() == Status.OK.getStatusCode()) {
                String responseText = response.getBody();
                JsonObject jsonObject = new JsonParser().parse(responseText).getAsJsonObject();
                assertThat(ValidationResult.fromJson(jsonObject.toString()),
                        is(ValidationResultIsEqual.equalTo(entity.getExpectedValidationResult())));
            } else {
                // Add tests (expected results) for sending malformed JSON, multiple entities, etc.
            }
        } catch (JsonSyntaxException | URISyntaxException | IOException e) {
            throw new ValidationServiceException(ValidationServiceError.RULE_EXECUTION_ERROR, e);
        }
    }

}
