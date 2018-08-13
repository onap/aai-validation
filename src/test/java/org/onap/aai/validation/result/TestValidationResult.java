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
package org.onap.aai.validation.result;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.inject.Inject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.aai.validation.exception.ValidationServiceException;
import org.onap.aai.validation.reader.EventReader;
import org.onap.aai.validation.reader.data.Entity;
import org.onap.aai.validation.result.ValidationResult;
import org.onap.aai.validation.result.Violation;
import org.onap.aai.validation.result.Violation.Builder;
import org.onap.aai.validation.result.Violation.ViolationType;
import org.onap.aai.validation.test.util.TestUtil;
import org.onap.aai.validation.util.JsonUtil;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(properties = {"schemaIngestPropLoc = src/test/resources/oxm-reader/schemaIngest.properties"})
@ContextConfiguration(locations = {"classpath:validation-result/test-validation-service-beans.xml"})
public class TestValidationResult {

    static {
        System.setProperty("APP_HOME", ".");
    }

    @Inject
    private EventReader eventReader;

    private static String vserverEvent;
    private static Entity entity;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        vserverEvent = TestUtil.getFileAsString(TestData.VSERVER.getFilename());
    }

    @Before
    public void setUp() throws Exception {
        entity = eventReader.getEntity(vserverEvent);
    }

    enum TestData {
        // @formatter:off
		VSERVER ("validation-result/vserver-create-event.json");

		private String filename;
		TestData(String filename) {this.filename = filename;}
		public String getFilename() {return this.filename;}
		// @formatter:on
    }

    @Test
    public void testValidationResultWithViolationDetailsAsString() throws Exception {
        // Violation details
        Map<String, Object> violationDetails = new HashMap<>();
        violationDetails.put("attr1", "val1");
        violationDetails.put("attr2", "val2");

        ValidationResult validationResult = getValidationResult(violationDetails);
        ValidationResult transformedVr = toAndFromJson(validationResult);

        assertThatValidationResultIsValid(transformedVr);
        Violation v = assertThatViolationIsValid(transformedVr, validationResult.getViolations().get(0));
        assertThat(v.getViolationDetails(), is(violationDetails));
    }

    @Test
    public void testValidationResultWithViolationDetailsIncludingNull() throws Exception {
        // Violation details
        Map<String, Object> violationDetails = new HashMap<>();
        violationDetails.put("attr1", "val1");
        violationDetails.put("attr2", null);

        ValidationResult validationResult = getValidationResult(violationDetails);
        ValidationResult transformedVr = toAndFromJson(validationResult);

        // Check
        assertThatValidationResultIsValid(transformedVr);
        Violation v = assertThatViolationIsValid(transformedVr, validationResult.getViolations().get(0));
        assertThat(v.getViolationDetails(), is(violationDetails));
    }

    @Test
    public void testValidationResultWithViolationDetailsAsList() throws Exception {
        // Violation details
        Map<String, Object> violationDetails = new HashMap<>();
        violationDetails.put("attr1", Arrays.asList("val1", "val2"));
        violationDetails.put("attr2", Arrays.asList("val3", "val4"));

        ValidationResult validationResult = getValidationResult(violationDetails);
        ValidationResult transformedVr = toAndFromJson(validationResult);

        // Check
        assertThatValidationResultIsValid(transformedVr);
        Violation v = assertThatViolationIsValid(transformedVr, validationResult.getViolations().get(0));
        assertThat(v.getViolationDetails(), is(violationDetails));
    }

    @Test
    public void testValidationResultWithViolationDetailsAsInt() throws Exception {
        // Violation details
        Map<String, Object> violationDetails = new HashMap<>();
        violationDetails.put("attr1", 1);
        violationDetails.put("attr2", 2);

        ValidationResult validationResult = getValidationResult(violationDetails);
        ValidationResult vr = toAndFromJson(validationResult);

        // Check
        assertThatValidationResultIsValid(vr);
        Violation v = assertThatViolationIsValid(vr, validationResult.getViolations().get(0));
        assertThat(v.getViolationDetails().get("attr1"), is(1.0));
        assertThat(v.getViolationDetails().get("attr2"), is(2.0));
    }

    @Test
    public void testValidationResultWithViolationDetailsAsObject() throws Exception {
        // Violation details
        JsonArray jsonArray = new JsonArray();
        jsonArray.add("v1");
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("p1", jsonArray);
        jsonObject.add("p2", jsonArray);
        Map<String, Object> violationDetails = new HashMap<>();
        violationDetails.put("attr1", jsonObject);

        ValidationResult validationResult = getValidationResult(violationDetails);
        ValidationResult transformedVr = toAndFromJson(validationResult);

        // Check
        assertThatValidationResultIsValid(transformedVr);
        Violation v = assertThatViolationIsValid(transformedVr, validationResult.getViolations().get(0));
        String jsonDetails = v.getViolationDetails().get("attr1").toString();
        JsonParser jsonParser = new JsonParser();
        JsonElement jsonElement = jsonParser.parse(jsonDetails);
        assertThat(jsonObject, is(jsonElement));
    }

    @Test
    public void testCompareObjects() throws Exception {
        ValidationResult validationResult = new ValidationResult(entity);
        assertThat(validationResult, is(not(equalTo(null))));

        validationResult.setEntityId(new JsonObject());
        assertThat(validationResult, is(not(equalTo(null))));

        ValidationResult other = new ValidationResult(entity);
        assertThat(validationResult, is(not(equalTo(other))));

        validationResult.setEntityType("type");
        assertThat(validationResult, is(not(equalTo(other))));

        Map<String, Object> violationDetails = new HashMap<>();

        //@formatter:off
		Violation violation = new Violation.Builder(entity)
				.category("category")
				.severity("severity")
				.violationType("violationType")
				.violationDetails(violationDetails)
				.errorMessage("errorMessage")
				.build();
		//@formatter:on

        validationResult.addViolation(violation);
        assertThat(validationResult, is(not(equalTo(other))));

        // Force call to hashCode()
        assertThat(validationResult.hashCode(), is(not(equalTo(other.hashCode()))));

    }

    /**
     * Tests for comparing two Violation objects. The generated Violation ID must be deterministic.
     *
     * @throws Exception
     */
    @Test
    public void testCompareViolationObjects() throws Exception {
        // Use the standard vserver event
        Builder builder = new Violation.Builder(entity);

        // Force call to toString() for coverage only
        assertThat(builder.toString(), is(equalTo(new Violation.Builder(entity).toString())));

        // Build a blank violation
        Violation violation = builder.build();

        // Identity tests
        assertThat(violation, is(not(equalTo(null))));
        assertThat(violation, is(not(equalTo(1))));
        assertThat(violation, is(equalTo(violation)));

        // Ensure that any violations we build are identical
        testViolationIdsForEquality(builder, builder, true);

        // Create a copy of the vserver event and vary the resourceVersion
        Entity entity2 = eventReader
                .getEntity(vserverEvent.replaceFirst("resource-version\": \"1464193654", "resource-version\": \"123"));

        // The violationId produced for identically built violations is the same for each builder (although the vserver
        // events differ).
        testViolationIdsForEquality(new Violation.Builder(entity), new Violation.Builder(entity2), true);

        // The violationId produced must differ whenever the violation values differ.
        testViolationIdsForInequality(new Violation.Builder(entity), new Violation.Builder(entity2));

        // Make a new variant of the vserver event using a different entity Id
        Entity entity3 = eventReader.getEntity(
                vserverEvent.replaceFirst("vserver-id\": \"example-vserver-id-val-34666", "vserver-id\": \"123"));

        // The violationId produced for identically built violations is now different for each builder (because the
        // entity Ids differ).
        testViolationIdsForEquality(new Violation.Builder(entity), new Violation.Builder(entity3), false);
    }

    /**
     * Generate various violations using the supplied builders and assert the expected equality of the generated
     * Violation IDs whenever the values supplied to the builders are the same.
     *
     * @param b1 a builder
     * @param b2 another builder
     * @param expectedResult whether or not the two builders should produce identical violations
     * @throws ValidationServiceException
     */
    private void testViolationIdsForEquality(Builder b1, Builder b2, Boolean expectedResult)
            throws ValidationServiceException {
        Violation v1 = b1.build();
        Violation v2 = b2.build();
        assertThatViolationsAreEqual(v1, v2, expectedResult);

        // Use the same category
        String category = "INVALID OBJ";
        v1 = b1.category(category).build();
        v2 = b2.category(category).build();
        assertThatViolationsAreEqual(v1, v2, expectedResult);

        // Use the same severity
        String severity = "CRITICAL";
        v1 = b1.severity(severity).build();
        v2 = b2.severity(severity).build();
        assertThatViolationsAreEqual(v1, v2, expectedResult);

        // Use the same violation type
        v1 = b1.violationType(ViolationType.RULE).build();
        v2 = b2.violationType(ViolationType.RULE).build();
        assertThatViolationsAreEqual(v1, v2, expectedResult);

        // Use the same validation rule
        String rule = "prov-status";
        v1 = b1.validationRule(rule).build();
        v2 = b2.validationRule(rule).build();
        assertThatViolationsAreEqual(v1, v2, expectedResult);

        // Use the same error message
        String errorMessage = "Invalid prov-status value. Must have a value not equal to ACTIVE/active.";
        v1 = b1.errorMessage(errorMessage).build();
        v2 = b2.errorMessage(errorMessage).build();
        assertThatViolationsAreEqual(v1, v2, expectedResult);

        // Use the same violation details
        Map<String, Object> details = new HashMap<>();
        details.put(rule, "ACTIVE");
        v1 = b1.violationDetails(details).build();
        v2 = b2.violationDetails(details).build();
        assertThatViolationsAreEqual(v1, v2, expectedResult);
    }

    /**
     * Generate violations using the supplied builders and assert that the generated Violation IDs differ whenever the
     * values supplied to the builders differ.
     *
     * @param builder
     * @param builder2
     * @throws ValidationServiceException
     */
    private void testViolationIdsForInequality(Builder builder, Builder builder2) throws ValidationServiceException {
        Violation violation;
        Violation other;

        // Vary the violation type
        violation = builder.violationType("").build();
        other = builder2.violationType(ViolationType.RULE).build();
        assertThatViolationIdsDiffer(violation, other);

        violation = builder.violationType(ViolationType.NONE).build();
        other = builder2.violationType(ViolationType.RULE).build();
        assertThatViolationIdsDiffer(violation, other);

        // Vary the validation rule
        violation = builder.validationRule(null).build();
        other = builder2.validationRule("rule").build();
        assertThatViolationIdsDiffer(violation, other);

        violation = builder.validationRule("rule1").build();
        other = builder2.validationRule(null).build();
        assertThatViolationIdsDiffer(violation, other);

        violation = builder.validationRule("rule1").build();
        other = builder2.validationRule("rule2").build();
        assertThatViolationIdsDiffer(violation, other);

        // Vary the category
        violation = builder.category(null).build();
        other = builder2.category("category").build();
        assertThatViolationIdsDiffer(violation, other);

        violation = builder.category("category").build();
        other = builder2.category(null).build();
        assertThatViolationIdsDiffer(violation, other);

        violation = builder.category("category1").build();
        other = builder2.category("category2").build();
        assertThatViolationIdsDiffer(violation, other);

        // Vary the error message
        violation = builder.validationRule("rule").build();
        other = builder2.validationRule("rule").errorMessage("message2").build();
        assertThatViolationIdsDiffer(violation, other);

        violation = builder.validationRule("rule").errorMessage("message1").build();
        other = builder2.validationRule("rule").errorMessage("message2").build();
        assertThatViolationIdsDiffer(violation, other);

        // Vary the severity
        violation = builder.errorMessage("msg").build();
        other = builder2.errorMessage("msg").severity("sev2").build();
        assertThatViolationIdsDiffer(violation, other);

        violation = builder.errorMessage("msg").severity("sev1").build();
        other = builder2.errorMessage("msg").severity("sev2").build();
        assertThatViolationIdsDiffer(violation, other);
    }

    private ValidationResult getValidationResult(Map<String, Object> violationDetails)
            throws ValidationServiceException {
        ValidationResult validationResult = new ValidationResult(entity);

        //@formatter:off
		Violation violation = new Violation.Builder(entity)
				.category("category")
				.severity("severity")
				.violationType("violationType")
				.violationDetails(violationDetails)
				.errorMessage("errorMessage")
				.build();
		//@formatter:on

        validationResult.addViolation(violation);

        return validationResult;
    }

    private ValidationResult toAndFromJson(ValidationResult validationResult) {
        return JsonUtil.toAnnotatedClassfromJson(validationResult.toJson(), ValidationResult.class);
    }

    private void assertThatValidationResultIsValid(ValidationResult vr) {
        assertTrue("Expected valid UUID", isValidEventId(vr.getValidationId()));
        assertIsValidTimestamp(vr.getValidationTimestamp());
        JsonObject expectedEntityId = new JsonObject();
        expectedEntityId.addProperty("vserver-id", "example-vserver-id-val-34666");
        assertThat(vr.getEntityId(), is(expectedEntityId));
        assertThat(vr.getEntityType(), is("vserver"));
        assertThat(vr.getResourceVersion(), is("1464193654"));
        assertThat(vr.getEntityLink(), is(
                "cloud-infrastructure/cloud-regions/cloud-region/region1/AAIregion1/tenants/tenant/example-tenant-id-val-88551/vservers/vserver/example-vserver-id-val-34666"));
    }

    private Violation assertThatViolationIsValid(ValidationResult vr, Violation expectedViolation) {
        Violation v = vr.getViolations().get(0);
        assertThat(v.getViolationId(), is(expectedViolation.getViolationId()));
        assertThat(v.getCategory(), is("category"));
        assertThat(v.getSeverity(), is("severity"));
        assertThat(v.getViolationType(), is("violationType"));
        assertThat(v.getErrorMessage(), is("errorMessage"));
        return v;
    }

    private void assertThatViolationsAreEqual(Violation v1, Violation v2, Boolean expectedResult) {
        assertThat("Violation equality in error:\n" + v1 + " equals " + v2, v1.equals(v2), is(expectedResult));
        assertThat("Violation ID equality in error:\n" + v1.getViolationId() + " equals " + v2.getViolationId(),
                v1.getViolationId().equals(v2.getViolationId()), is(expectedResult));
        // Force a call to toString() for code coverage only
        assertThat(v1.toString().equals(v2.toString()), is(equalTo(expectedResult)));
    }

    private void assertThatViolationIdsDiffer(Violation violation, Violation other) {
        assertThat(violation.getViolationId(), is(not(equalTo(other.getViolationId()))));
        assertThat(violation, is(not(equalTo(other))));
    }

    private boolean isValidEventId(String eventId) {
        try {
            UUID.fromString(eventId);
        } catch (IllegalArgumentException exception) {
            return false;
        }
        return true;
    }

    private void assertIsValidTimestamp(String date) {
        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmssX").withZone(ZoneOffset.UTC);
        Instant.from(f.parse(date));
    }
}
