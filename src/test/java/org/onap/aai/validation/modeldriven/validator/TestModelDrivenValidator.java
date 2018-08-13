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
package org.onap.aai.validation.modeldriven.validator;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.dom4j.Element;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.onap.aai.validation.controller.ValidationController;
import org.onap.aai.validation.modeldriven.ModelCacheManager;
import org.onap.aai.validation.modeldriven.ModelId;
import org.onap.aai.validation.modeldriven.parser.XMLModelParser;
import org.onap.aai.validation.modeldriven.validator.ModelDrivenValidator;
import org.onap.aai.validation.result.ValidationResult;
import org.onap.aai.validation.result.Violation;
import org.onap.aai.validation.test.util.TestUtil;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(properties = {"schemaIngestPropLoc = src/test/resources/oxm-reader/schemaIngest.properties"})
@ContextConfiguration(locations = {"classpath:model-validation/instance-validator/test-validation-service-beans.xml"})
public class TestModelDrivenValidator {

    static {
        System.setProperty("AJSC_HOME", ".");
    }

    private static final String RESOURCE_VERSION = "1467975776";
    private static final String MODEL_ID_ATTRIBUTE_MNV = "model-name-version-id";
    private static final String MODEL_ID_ATTRIBUTE_MID = "model-id";

    @Mock
    private ModelCacheManager mockModelCacheManager;

    @InjectMocks
    @Inject
    private ModelDrivenValidator modelDrivenValidator;

    private String objectInstance;
    private String connectorModel;

    enum INSTANCE_VALIDATION_FILE {
        // @formatter:off
		CONNECTOR_MODEL           ("model-validation/instance-validator/connector-widget-id.xml"),
		NO_MODEL_ID               ("model-validation/instance-validator/connector-instance-no-model-id.json"),
		UNKNOWN_MODEL_ID          ("model-validation/instance-validator/connector-instance-unknown-model-id.json"),
		ERRORS                    ("model-validation/instance-validator/connector-instance-errors.json"),
		MULTIPLE_MISSING_ATTRS    ("model-validation/instance-validator/connector-instance-multiple-missing-attrs.json"),
		MULTIPLE_UNEXPECTED_ATTRS ("model-validation/instance-validator/connector-instance-multiple-unexpected-attrs.json"),
		SUCCESS                   ("model-validation/instance-validator/connector-instance-success.json");
		// @formatter:on

        private String filename;

        INSTANCE_VALIDATION_FILE(String filename) {
            this.filename = filename;
        }

        public String getFilename() {
            return this.filename;
        }
    }

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Before
    public void setUp() throws Exception {
        connectorModel = TestUtil.getFileAsString(INSTANCE_VALIDATION_FILE.CONNECTOR_MODEL.getFilename());
    }

    @Test
    public void testValidateInstanceWithoutModelId() throws Exception {
        objectInstance = TestUtil.getFileAsString(INSTANCE_VALIDATION_FILE.NO_MODEL_ID.getFilename());

        ValidationResult validationResult = modelDrivenValidator.validate(objectInstance).get(0);
        assertThatValidationResultIsValid(validationResult, "c7611ebe-c324-48f1-8085-94aef0c12fd", "connector",
                "1467975776");

        Violation violation = validationResult.getViolations().get(0);
        Map<String, Object> details = new HashMap<>();
        details.put("No model ID", null);
        assertThatViolationIsValid(violation, ValidationController.VALIDATION_ERROR_SEVERITY, details,
                "The model [null] could not be found", RESOURCE_VERSION);
    }

    @Test
    public void testValidateInstanceWithUnknownModelId() throws Exception {
        objectInstance = TestUtil.getFileAsString(INSTANCE_VALIDATION_FILE.UNKNOWN_MODEL_ID.getFilename());

        Mockito.when(mockModelCacheManager.get(new ModelId(MODEL_ID_ATTRIBUTE_MID, "UNKNOWN-MODEL"))).thenReturn(null);

        ValidationResult validationResult = modelDrivenValidator.validate(objectInstance).get(0);
        assertThatValidationResultIsValid(validationResult, "c7611ebe-c324-48f1-8085-94aef0c12fd", "connector",
                "1467975776");

        Violation violation = validationResult.getViolations().get(0);
        Map<String, Object> details = new HashMap<>();
        details.put("No model ID", "UNKNOWN-MODEL");
        assertThatViolationIsValid(violation, ValidationController.VALIDATION_ERROR_SEVERITY, details,
                "The model [UNKNOWN-MODEL] could not be found", RESOURCE_VERSION);
    }

    @Test
    public void testValidate() throws Exception {
        objectInstance = TestUtil.getFileAsString(INSTANCE_VALIDATION_FILE.ERRORS.getFilename());

        Element modelElement = XMLModelParser.parse(connectorModel, true);

        Mockito.when(mockModelCacheManager.get(new ModelId(MODEL_ID_ATTRIBUTE_MID, "connector-widget-id")))
                .thenReturn(modelElement);
        Mockito.when(mockModelCacheManager
                .get(new ModelId(MODEL_ID_ATTRIBUTE_MNV, "l2-bridge-for-wan-connector-resource-id"))).thenReturn(null);

        ValidationResult validationResult = modelDrivenValidator.validate(objectInstance).get(0);
        assertThatValidationResultIsValid(validationResult, "c7611ebe-c324-48f1-8085-94aef0c12fd", "connector",
                "1467975776");

        List<Violation> violations = validationResult.getViolations();

        assertThat(violations, hasSize(3));
        assertThat(getCategories(violations),
                containsInAnyOrder(Arrays.asList("MISSING_ATTR", "UNEXPECTED_ATTR", "UNEXPECTED_REL").toArray()));

        Violation violation = getValidationByCategory(violations, "MISSING_ATTR").get(0);
        Map<String, Object> details = new HashMap<>();
        details.put("MISSING ATTR", "product");
        assertThatViolationIsValid(violation, ValidationController.VALIDATION_ERROR_SEVERITY, details,
                "Attribute [product] is missing in the object instance", RESOURCE_VERSION);

        violation = getValidationByCategory(violations, "UNEXPECTED_ATTR").get(0);
        details = new HashMap<>();
        details.put("UNEXPECTED ATTR", "unexpected");
        assertThatViolationIsValid(violation, ValidationController.VALIDATION_ERROR_SEVERITY, details,
                "Attribute [unexpected] should not be present in the object instance", RESOURCE_VERSION);

        violation = getValidationByCategory(violations, "UNEXPECTED_REL").get(0);
        details = new HashMap<>();
        Map<String, Object> entityIdmap = new HashMap<>();
        entityIdmap.put("vdc-id", "vdc-01");
        details.put("entityId", entityIdmap);
        details.put("modelName", null);
        details.put("entityType", "virtual-data-center");
        details.put("UNEXPECTED REL", "logical-link");
        assertThatViolationIsValid(violation, ValidationController.VALIDATION_ERROR_SEVERITY, details,
                "Entity [vdc-id=vdc-01] of type [virtual-data-center] must not be related to [logical-link]",
                RESOURCE_VERSION);
    }

    @Test
    public void testValidateMultipleMissingAttrs() throws Exception {
        objectInstance = TestUtil.getFileAsString(INSTANCE_VALIDATION_FILE.MULTIPLE_MISSING_ATTRS.getFilename());

        Element modelElement = XMLModelParser.parse(connectorModel, true);

        Mockito.when(mockModelCacheManager.get(new ModelId(MODEL_ID_ATTRIBUTE_MID, "connector-widget-id")))
                .thenReturn(modelElement);
        Mockito.when(mockModelCacheManager
                .get(new ModelId(MODEL_ID_ATTRIBUTE_MNV, "l2-bridge-for-wan-connector-resource-id"))).thenReturn(null);

        List<Violation> violations = modelDrivenValidator.validate(objectInstance).get(0).getViolations();

        assertThat(violations, hasSize(2));
        assertThat(getCategories(violations),
                containsInAnyOrder(Arrays.asList("MISSING_ATTR", "MISSING_ATTR").toArray()));

        List<Violation> missingAttrValidations = getValidationByCategory(violations, "MISSING_ATTR");
        String detailsAsString = getDetails(missingAttrValidations).toString();
        assertThat(detailsAsString, containsString("{MISSING ATTR=product}"));
        assertThat(detailsAsString, containsString("{MISSING ATTR=vpn-id}"));
    }

    @Test
    public void testValidateMultipleUnexpectedAttrs() throws Exception {
        objectInstance = TestUtil.getFileAsString(INSTANCE_VALIDATION_FILE.MULTIPLE_UNEXPECTED_ATTRS.getFilename());

        Element modelElement = XMLModelParser.parse(connectorModel, true);

        Mockito.when(mockModelCacheManager.get(new ModelId(MODEL_ID_ATTRIBUTE_MID, "connector-widget-id")))
                .thenReturn(modelElement);
        Mockito.when(mockModelCacheManager
                .get(new ModelId(MODEL_ID_ATTRIBUTE_MNV, "l2-bridge-for-wan-connector-resource-id"))).thenReturn(null);

        List<Violation> violations = modelDrivenValidator.validate(objectInstance).get(0).getViolations();

        assertThat(violations, hasSize(2));
        assertThat(getCategories(violations),
                containsInAnyOrder(Arrays.asList("UNEXPECTED_ATTR", "UNEXPECTED_ATTR").toArray()));

        List<Violation> missingAttrViolations = getValidationByCategory(violations, "UNEXPECTED_ATTR");
        String detailsAsString = getDetails(missingAttrViolations).toString();
        assertThat(detailsAsString, containsString("{UNEXPECTED ATTR=city}"));
        assertThat(detailsAsString, containsString("{UNEXPECTED ATTR=state}"));
    }

    @Test
    public void testValidateSuccess() throws Exception {
        objectInstance = TestUtil.getFileAsString(INSTANCE_VALIDATION_FILE.SUCCESS.getFilename());

        Element modelElement = XMLModelParser.parse(connectorModel, true);

        Mockito.when(mockModelCacheManager.get(new ModelId(MODEL_ID_ATTRIBUTE_MID, "connector-widget-id")))
                .thenReturn(modelElement);
        Mockito.when(mockModelCacheManager
                .get(new ModelId(MODEL_ID_ATTRIBUTE_MNV, "l2-bridge-for-wan-connector-resource-id"))).thenReturn(null);

        List<Violation> violations = modelDrivenValidator.validate(objectInstance).get(0).getViolations();

        assertThat(violations, is(empty()));
    }

    private void assertThatValidationResultIsValid(ValidationResult validationResult, String entityInstanceId,
            String entityType, String resourceVersion) {
        assertThat(
                validationResult.getEntityId().getAsJsonObject().entrySet().iterator().next().getValue().getAsString(),
                is(equalTo(entityInstanceId)));
        assertThat(validationResult.getEntityType(), is(equalTo(entityType)));
        assertThat(validationResult.getResourceVersion(), is(equalTo(resourceVersion)));
    }

    private void assertThatViolationIsValid(Violation violation, String severity, Map<String, Object> violationDetails,
            String errorMessage, String resourceVersion) {
        assertThat(violation.getSeverity(), is(equalTo(severity)));
        assertThat(violation.getViolationType(), is(equalTo("Model")));
        assertThat(violation.getViolationDetails(), is(equalTo(violationDetails)));
        assertThat(violation.getErrorMessage(), is(equalTo(errorMessage)));
    }

    private List<String> getCategories(List<Violation> validations) {
        List<String> categories = new ArrayList<>();
        for (Violation validation : validations) {
            categories.add(validation.getCategory());
        }
        return categories;
    }

    private List<Map<String, Object>> getDetails(List<Violation> validations) {
        List<Map<String, Object>> details = new ArrayList<>();
        for (Violation validation : validations) {
            details.add(validation.getViolationDetails());
        }
        return details;
    }

    private List<Violation> getValidationByCategory(List<Violation> validations, String category) {
        List<Violation> validationsByCategory = new ArrayList<>();
        for (Violation validation : validations) {
            if (category.equals(validation.getCategory())) {
                validationsByCategory.add(validation);
            }
        }
        return validationsByCategory;
    }
}
