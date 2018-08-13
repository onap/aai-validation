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
package org.onap.aai.validation.ruledriven.validator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Stream;
import javax.inject.Inject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.aai.validation.exception.ValidationServiceException;
import org.onap.aai.validation.result.ValidationResult;
import org.onap.aai.validation.ruledriven.RuleDrivenValidator;
import org.onap.aai.validation.test.util.TestEntity;
import org.onap.aai.validation.test.util.ValidationResultIsEqual;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(properties = {"schemaIngestPropLoc = src/test/resources/oxm-reader/schemaIngest.properties"})
@ContextConfiguration(
        locations = {"classpath:" + TestRuleDrivenValidator.UNIT_TEST_FOLDER + "/test-rule-driven-validator-beans.xml"})
public class TestRuleDrivenValidator {

    static {
        System.setProperty("APP_HOME", ".");
    }

    public static final String UNIT_TEST_FOLDER = "rule-driven-validator";
    private static final String TEST_EVENTS_PATH = "/test_events";

    @Inject
    private RuleDrivenValidator validator;

    /**
     * @param testEntitiesPath
     * @param testEventsPath
     * @param resultsPath
     * @return all test entities
     * @throws URISyntaxException
     */
    public static List<TestEntity> getEntities(String testEntitiesPath, String testEventsPath, String resultsPath)
            throws URISyntaxException {
        Path testEvents = Paths.get(ClassLoader.getSystemResource(testEntitiesPath + testEventsPath).toURI());

        BiPredicate<Path, BasicFileAttributes> jsonMatcher =
                (path, basicFileAttributes) -> path.toFile().getName().matches(".*\\.json");

        List<TestEntity> entitiesList = new ArrayList<>();
        try (Stream<Path> paths = Files.find(testEvents, 2, jsonMatcher)) {
            paths.forEach((path) -> entitiesList.add(new TestEntity(testEvents, path, testEventsPath, resultsPath)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return entitiesList;
    }

    /**
     * @throws ValidationServiceException
     * @throws JsonSyntaxException
     * @throws URISyntaxException
     * @throws IOException
     */
    @Test
    public void testValidateUnitTestInstances()
            throws ValidationServiceException, JsonSyntaxException, URISyntaxException, IOException {
        validateEntities(UNIT_TEST_FOLDER, TEST_EVENTS_PATH, "/results/expected");
    }

    /**
     * @param inputEventsFolder
     * @param testEventsPath
     * @param resultsPath
     * @throws URISyntaxException
     * @throws ValidationServiceException
     * @throws IOException
     */
    private void validateEntities(String inputEventsFolder, String testEventsPath, String resultsPath)
            throws URISyntaxException, ValidationServiceException, IOException {
        for (TestEntity entity : getEntities(inputEventsFolder, testEventsPath, resultsPath)) {
            List<ValidationResult> results = validator.validate(entity.getJson());
            assertThat(results.size(), is(1));
            ValidationResult expectedResult = entity.getExpectedValidationResult();
            if (expectedResult == null) {
                Path testEvents = Paths.get(ClassLoader.getSystemResource(inputEventsFolder + resultsPath).toURI());
                StringBuilder sb = new StringBuilder();
                Files.walk(testEvents).forEach((path) -> sb.append(path).append("\n"));
                assertThat("Expected results missing (" + entity.expectedResultsFile + ")\n" + sb.toString(),
                        expectedResult, is(notNullValue()));
            }
            assertThat(entity.inputFile.getAbsolutePath(), results.get(0),
                    is(ValidationResultIsEqual.equalTo(expectedResult)));
        }
    }

}
