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
package org.onap.aai.validation.controller;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.aai.validation.Validator;
import org.onap.aai.validation.config.ValidationControllerConfig;
import org.onap.aai.validation.exception.ValidationServiceError;
import org.onap.aai.validation.exception.ValidationServiceException;
import org.onap.aai.validation.publisher.MessagePublisher;
import org.onap.aai.validation.reader.EventReader;
import org.onap.aai.validation.reader.data.Entity;
import org.onap.aai.validation.reader.data.EntityId;
import org.onap.aai.validation.result.ValidationResult;
import org.onap.aai.validation.result.ValidationResultBuilder;
import org.onap.aai.validation.result.Violation;

@RunWith(MockitoJUnitRunner.class)
public class TestValidationController {

    private static final String AAI_EVENT = "AAI-EVENT";
    private static final String CREATE = "CREATE";
    private static final String DELETE = "DELETE";
    private static final String DEV_INT_1 = "devINT1";
    private static final String ENTITY_LINK = "entityLink";
    private static final String MODEL = "model";
    private static final String RULE = "rule";
    private static final String TEST = "test";
    private static final String TESTDATA_EVENTTYPE_NAMEDQUERY = "aai named query eventtype";
    private static final String TESTDATA_EVENTTYPE_API = "aai api eventtype";
    private static final String TESTDATA_EVENTTYPE_AAI = "aai event eventype";
    private static final String TESTDATA_EVENTTYPE_UNKNOWN = "unknown eventtype";
    private static final String TESTDATA_EVENTTYPE_NULL = "event with null eventtype";
    private static final String TESTDATA_EVENTTYPE_END_EVENT = "END-EVENT";
    private static final String TESTDATA_DOMAIN_INVALID = "event with invalid domain";
    private static final String TESTDATA_DOMAIN_NULL = "event with null domain";
    private static final String TESTDATA_EVENTACTION_DELETE = "event with delete event action";
    private static final String TESTDATA_EVENTACTION_NULL = "event with null event action";
    private static final String TESTDATA_EXCEPTION_EVENT = "event causing exception";
    private static final String TESTDATA_HANDLE_EXCEPTION_EXCEPTION_EVENT =
            "event causing exception during handling of the original exception";
    private static final String TESTDATA_VALIDATION_RESULT_PUBLISH_ERROR =
            "event causing exception during publishing of validation result";
    private static final String VSERVER = "vserver";

    @Mock
    private ValidationControllerConfig validationControllerConfig;

    @Mock
    private EventReader eventReader;

    @Mock
    private Validator ruleDrivenValidator;

    @Mock
    private Validator modelDrivenValidator;

    @Mock
    private MessagePublisher messagePublisher;

    @InjectMocks
    private ValidationController validationController = new ValidationController(validationControllerConfig,
            eventReader, ruleDrivenValidator, modelDrivenValidator, messagePublisher);

    @Mock
    private Entity entity;

    static {
        System.setProperty("APP_HOME", ".");
    }

    @Before
    public void setupMocks() throws ValidationServiceException {

        Map<String, List<ValidationResult>> validationResultsMap = setupTestData();

        when(ruleDrivenValidator.validate(TESTDATA_EVENTTYPE_AAI))
                .thenReturn(validationResultsMap.get(TESTDATA_EVENTTYPE_AAI));
        when(ruleDrivenValidator.validate(TESTDATA_EVENTTYPE_API))
                .thenReturn(validationResultsMap.get(TESTDATA_EVENTTYPE_API));
        when(modelDrivenValidator.validate(TESTDATA_EVENTTYPE_NAMEDQUERY))
                .thenReturn(validationResultsMap.get(TESTDATA_EVENTTYPE_NAMEDQUERY));
        when(ruleDrivenValidator.validate(TESTDATA_EXCEPTION_EVENT))
                .thenThrow(new RuntimeException("Failed to validate"));
        when(ruleDrivenValidator.validate(TESTDATA_HANDLE_EXCEPTION_EXCEPTION_EVENT))
                .thenThrow(new RuntimeException("Failed to validate"));
        when(ruleDrivenValidator.validate(TESTDATA_VALIDATION_RESULT_PUBLISH_ERROR))
                .thenReturn(validationResultsMap.get(TESTDATA_VALIDATION_RESULT_PUBLISH_ERROR));
        when(ruleDrivenValidator.validate(TESTDATA_EVENTACTION_NULL))
                .thenReturn(validationResultsMap.get(TESTDATA_EVENTTYPE_AAI));

        Map<String, List<String>> eventTypeDataMap = setupEventTypeData();
        when(validationControllerConfig.getEventTypeRule()).thenReturn(eventTypeDataMap.get(RULE));
        when(validationControllerConfig.getEventTypeModel()).thenReturn(eventTypeDataMap.get(MODEL));
        when(validationControllerConfig.getEventTypeEnd()).thenReturn("END-EVENT");

        when(eventReader.getEventType(TESTDATA_EVENTTYPE_AAI)).thenReturn(Optional.of(AAI_EVENT));
        when(eventReader.getEventType(TESTDATA_EVENTTYPE_API)).thenReturn(Optional.of("AAI-DATA-EXPORT-API"));
        when(eventReader.getEventType(TESTDATA_EVENTTYPE_NAMEDQUERY)).thenReturn(Optional.of("AAI-DATA-EXPORT-NQ"));
        when(eventReader.getEventType(TESTDATA_EVENTTYPE_UNKNOWN)).thenReturn(Optional.of("EVENTTYPE-UNKNOWN"));
        when(eventReader.getEventType(TESTDATA_EVENTTYPE_NULL)).thenReturn(Optional.empty());
        when(eventReader.getEventType(TESTDATA_EXCEPTION_EVENT)).thenReturn(Optional.of(AAI_EVENT));
        when(eventReader.getEventType(TESTDATA_HANDLE_EXCEPTION_EXCEPTION_EVENT)).thenReturn(Optional.of(AAI_EVENT));
        when(eventReader.getEventType(TESTDATA_VALIDATION_RESULT_PUBLISH_ERROR)).thenReturn(Optional.of(AAI_EVENT));
        when(eventReader.getEventType(TESTDATA_EVENTACTION_NULL)).thenReturn(Optional.of(AAI_EVENT));

        when(validationControllerConfig.getEventDomain()).thenReturn(DEV_INT_1);
        when(eventReader.getEventDomain(TESTDATA_EVENTTYPE_AAI)).thenReturn(Optional.of(DEV_INT_1));
        when(eventReader.getEventDomain(TESTDATA_EVENTTYPE_API)).thenReturn(Optional.of(DEV_INT_1));
        when(eventReader.getEventDomain(TESTDATA_EVENTTYPE_NAMEDQUERY)).thenReturn(Optional.of(DEV_INT_1));
        when(eventReader.getEventDomain(TESTDATA_EVENTTYPE_UNKNOWN)).thenReturn(Optional.of(DEV_INT_1));
        when(eventReader.getEventDomain(TESTDATA_EVENTTYPE_NULL)).thenReturn(Optional.of(DEV_INT_1));
        when(eventReader.getEventDomain(TESTDATA_DOMAIN_INVALID)).thenReturn(Optional.of("invalidDomain"));
        when(eventReader.getEventDomain(TESTDATA_DOMAIN_NULL)).thenReturn(Optional.empty());
        when(eventReader.getEventDomain(TESTDATA_EXCEPTION_EVENT)).thenReturn(Optional.of(DEV_INT_1));
        when(eventReader.getEventDomain(TESTDATA_HANDLE_EXCEPTION_EXCEPTION_EVENT)).thenReturn(Optional.of(DEV_INT_1));
        when(eventReader.getEventDomain(TESTDATA_VALIDATION_RESULT_PUBLISH_ERROR)).thenReturn(Optional.of(DEV_INT_1));
        when(eventReader.getEventDomain(TESTDATA_EVENTACTION_DELETE)).thenReturn(Optional.of(DEV_INT_1));
        when(eventReader.getEventDomain(TESTDATA_EVENTACTION_NULL)).thenReturn(Optional.of(DEV_INT_1));

        List<String> excludedActions = new ArrayList<>();
        excludedActions.add(DELETE);
        when(validationControllerConfig.getExcludedEventActions()).thenReturn(excludedActions);
        when(eventReader.getEventAction(TESTDATA_EVENTTYPE_AAI)).thenReturn(Optional.of(CREATE));
        when(eventReader.getEventAction(TESTDATA_EVENTTYPE_API)).thenReturn(Optional.of(CREATE));
        when(eventReader.getEventAction(TESTDATA_EVENTTYPE_NAMEDQUERY)).thenReturn(Optional.of(CREATE));
        when(eventReader.getEventAction(TESTDATA_EVENTTYPE_UNKNOWN)).thenReturn(Optional.of(CREATE));
        when(eventReader.getEventAction(TESTDATA_EVENTTYPE_NULL)).thenReturn(Optional.of(CREATE));
        when(eventReader.getEventAction(TESTDATA_EXCEPTION_EVENT)).thenReturn(Optional.of(CREATE));
        when(eventReader.getEventAction(TESTDATA_HANDLE_EXCEPTION_EXCEPTION_EVENT)).thenReturn(Optional.of(CREATE));
        when(eventReader.getEventAction(TESTDATA_VALIDATION_RESULT_PUBLISH_ERROR)).thenReturn(Optional.of(CREATE));
        when(eventReader.getEventAction(TESTDATA_EVENTACTION_DELETE)).thenReturn(Optional.of(DELETE));
        when(eventReader.getEventAction(TESTDATA_EVENTACTION_NULL)).thenReturn(Optional.empty());

        when(eventReader.getEntityType(TESTDATA_EXCEPTION_EVENT)).thenReturn(Optional.of(VSERVER));
        when(eventReader.getEntity(TESTDATA_EXCEPTION_EVENT)).thenReturn(entity);
        when(eventReader.getEntityType(TESTDATA_HANDLE_EXCEPTION_EXCEPTION_EVENT)).thenThrow(
                new RuntimeException("Error during handling the exception for an event that couldn't be validated"));

        Mockito.doThrow(new ValidationServiceException(ValidationServiceError.EVENT_CLIENT_PUBLISHER_INIT_ERROR))
                .when(messagePublisher).publishMessage(
                        Mockito.contains("\"entityId\":\"[vserver-id=instanceid1]\",\"entityType\":\"entitytype1\","
                                + "\"resourceVersion\":\"resourceVersion1\""));
    }

    private Map<String, List<String>> setupEventTypeData() {
        Map<String, List<String>> eventTypeDataMap = new HashMap<>();
        List<String> eventTypeRule = new ArrayList<>();

        eventTypeRule.add(AAI_EVENT);
        eventTypeRule.add("AAI-DATA-EXPORT-API");
        eventTypeDataMap.put(RULE, eventTypeRule);

        List<String> eventTypeModel = new ArrayList<>();
        eventTypeModel.add("AAI-DATA-EXPORT-NQ");
        eventTypeDataMap.put(MODEL, eventTypeModel);

        return eventTypeDataMap;
    }

    private Map<String, List<ValidationResult>> setupTestData() throws ValidationServiceException {
        final Map<String, List<ValidationResult>> validationResultsMap = new HashMap<>();

        when(eventReader.getEntity(VSERVER)).thenReturn(entity);
        setUpEntityMock("20160525162737-61c49d41-5338-4755-af54-06cee9fe4aca", VSERVER, "1464193654");

        ValidationResultBuilder builder = new ValidationResultBuilder(eventReader, VSERVER);
        List<ValidationResult> aaiEventValidationResults = new ArrayList<>();
        aaiEventValidationResults.add(builder.build());
        aaiEventValidationResults.add(builder.build());
        validationResultsMap.put(TESTDATA_EVENTTYPE_AAI, aaiEventValidationResults);

        List<ValidationResult> apiEventValidationResults = new ArrayList<>();

        setUpEntityMock("20160525162737-61c49d41-5338-4755-af54-06cee9fe4acb", VSERVER, "1464193655");

        apiEventValidationResults.add(builder.build());
        validationResultsMap.put(TESTDATA_EVENTTYPE_API, apiEventValidationResults);

        List<ValidationResult> namedQueryEventValidationResults = new ArrayList<>();

        setUpEntityMock("20160525162737-61c49d41-5338-4755-af54-06cee9fe4acc", VSERVER, "1464193656");

        namedQueryEventValidationResults.add(builder.build());
        validationResultsMap.put(TESTDATA_EVENTTYPE_NAMEDQUERY, namedQueryEventValidationResults);

        List<ValidationResult> messagePublishExceptionValidationResults = new ArrayList<>();

        setUpEntityMock("instanceid1", "entitytype1", "resourceVersion1");

        messagePublishExceptionValidationResults.add(builder.build());
        validationResultsMap.put(TESTDATA_VALIDATION_RESULT_PUBLISH_ERROR, messagePublishExceptionValidationResults);

        return validationResultsMap;
    }

    @Test
    public void testExecuteForAaiEvent() throws Exception {
        // Test for AAI-EVENT
        validationController.execute(TESTDATA_EVENTTYPE_AAI, TEST);
        verify(ruleDrivenValidator, times(1)).validate(TESTDATA_EVENTTYPE_AAI);
        verify(messagePublisher, times(2)).publishMessage(Mockito
                .contains("\"entityId\":{\"vserver-id\":\"20160525162737-61c49d41-5338-4755-af54-06cee9fe4aca\"},"
                        + "\"entityType\":\"vserver\",\"entityLink\":\"entityLink\","
                        + "\"resourceVersion\":\"1464193654\",\"entity\":{},\"violations\":[]}"));
    }

    @Test
    public void testExecuteForApiEvent() throws Exception {
        // Test for AAI-DATA-EXPORT-API
        validationController.execute(TESTDATA_EVENTTYPE_API, TEST);
        verify(ruleDrivenValidator, times(1)).validate(TESTDATA_EVENTTYPE_API);
        verify(messagePublisher, times(1)).publishMessage(Mockito
                .contains("\"entityId\":{\"vserver-id\":\"20160525162737-61c49d41-5338-4755-af54-06cee9fe4acb\"},"
                        + "\"entityType\":\"vserver\",\"entityLink\":\"entityLink\","
                        + "\"resourceVersion\":\"1464193655\",\"entity\":{},\"violations\":[]}"));
    }

    @Test
    public void testExecuteForNqEvent() throws Exception {
        // Test for AAI-DATA-EXPORT-NQ
        validationController.execute(TESTDATA_EVENTTYPE_NAMEDQUERY, TEST);
        verify(modelDrivenValidator, times(1)).validate(TESTDATA_EVENTTYPE_NAMEDQUERY);
        verify(messagePublisher, times(1)).publishMessage(Mockito
                .contains("\"entityId\":{\"vserver-id\":\"20160525162737-61c49d41-5338-4755-af54-06cee9fe4acc\"},"
                        + "\"entityType\":\"vserver\",\"entityLink\":\"entityLink\","
                        + "\"resourceVersion\":\"1464193656\",\"entity\":{},\"violations\":[]}"));
    }

    @Test
    public void testExecuteForNullDomain() throws Exception {
        doVerifyMockInteractionsTest(TESTDATA_DOMAIN_NULL, TEST);
    }

    private void doVerifyMockInteractionsTest(String event, String eventSource) throws Exception {
        validationController.execute(event, eventSource);
        verify(eventReader, times(1)).getEventType(Mockito.anyString());
        verify(ruleDrivenValidator, times(0)).validate(Mockito.anyString());
        verify(modelDrivenValidator, times(0)).validate(Mockito.anyString());
        verify(messagePublisher, times(0)).publishMessage(Mockito.anyString());

    }

    @Test
    public void testExecuteForInvalidDomain() throws Exception {
        doVerifyMockInteractionsTest(TESTDATA_DOMAIN_INVALID, TEST);
    }

    @Test
    public void testExecuteForExcludedAction() throws Exception {
        doVerifyMockInteractionsTest(TESTDATA_EVENTACTION_DELETE, TEST);
    }

    @Test
    public void testExecuteForNullAction() throws Exception {
        validationController.execute(TESTDATA_EVENTACTION_NULL, TEST);
        verify(eventReader, times(2)).getEventType(Mockito.anyString());
        verify(ruleDrivenValidator, times(1)).validate(Mockito.anyString());
        verify(modelDrivenValidator, times(0)).validate(Mockito.anyString());
        verify(messagePublisher, times(2)).publishMessage(Mockito.anyString());
    }



    private void doEventTypeTest(String event, String eventSource, int numEventReaderInvocations) throws Exception {
        validationController.execute(event, eventSource);
        verify(eventReader, times(numEventReaderInvocations)).getEventType(event);
        verify(ruleDrivenValidator, times(0)).validate(Mockito.anyString());
        verify(modelDrivenValidator, times(0)).validate(Mockito.anyString());
        verify(messagePublisher, times(0)).publishMessage(Mockito.anyString());
    }

    @Test
    public void testExecuteForNullEventType() throws Exception {
        // The implementation checks whether this is an end event
        // Given that it is not, the event is then examined to see if it is a validation candidate
        doEventTypeTest(TESTDATA_EVENTTYPE_NULL, TEST, 2);
    }

    @Test
    public void testExecuteForUnknownEventType() throws Exception {
        doEventTypeTest(TESTDATA_EVENTTYPE_UNKNOWN, TEST, 2);
    }

    @Test
    public void testExecuteForEndEventType() throws Exception {
        doVerifyMockInteractionsTest(TESTDATA_EVENTTYPE_END_EVENT, TEST);
    }

    @Test
    public void testExceptionDuringValidation() throws Exception {
        String primaryKey = "vserver-id";
        String value = "example-vserver-id-val-34666";
        String resourceVersion = "123456789";

        EntityId entityId = new EntityId(primaryKey, value);

        when(entity.getResourceVersion()).thenReturn(Optional.of(resourceVersion));
        when(entity.getIds()).thenReturn(Collections.singletonList(entityId));
        validationController.execute(TESTDATA_EXCEPTION_EVENT, TEST);
        verify(ruleDrivenValidator, times(1)).validate(TESTDATA_EXCEPTION_EVENT);

        // @formatter:off
        Violation violation = new Violation.Builder(entity)
                                           .category("CANNOT_VALIDATE")
                                           .severity("CRITICAL")
                                           .violationType("NONE")
                                           .errorMessage("Failed to validate")
                                           .build();
        // @formatter:on

        JsonObject violationObject = new JsonParser().parse(violation.toString()).getAsJsonObject();
        violationObject.remove("validationRule"); // Not set

        JsonObject validationResult = new JsonObject();
        JsonObject entityIdObject = new JsonObject();
        entityIdObject.addProperty(primaryKey, value);
        validationResult.add(Violation.ENTITY_ID_PROPERTY, entityIdObject);
        validationResult.addProperty(Violation.ENTITY_TYPE_PROPERTY, "entitytype1");
        validationResult.addProperty(ENTITY_LINK, ENTITY_LINK);
        validationResult.addProperty("resourceVersion", resourceVersion);
        validationResult.add("entity", new JsonObject());
        JsonArray violations = new JsonArray();
        violations.add(violationObject);
        validationResult.add("violations", violations);

        String json = validationResult.toString();
        String messageContent = json.substring(1, json.length() - 1); // Remove the { } from the JSON string
        verify(messagePublisher).publishMessage(Mockito.contains(messageContent));
    }

    @Test
    public void testExceptionDuringHandlingValidationException() throws Exception {
        // Test for exception during handling of an exception scenario
        validationController.execute(TESTDATA_HANDLE_EXCEPTION_EXCEPTION_EVENT, TEST);
        verify(ruleDrivenValidator, times(1)).validate(TESTDATA_HANDLE_EXCEPTION_EXCEPTION_EVENT);
        verify(messagePublisher, times(0)).publishMessage(Mockito.anyString());
    }

    @Test
    public void testExceptionDuringMessagePublish() throws Exception {
        // Test for exception during publishing message.
        // Cant verify if the static application logger has been called.
        // This test is here for code coverage.
        validationController.execute(TESTDATA_VALIDATION_RESULT_PUBLISH_ERROR, TEST);
        verify(ruleDrivenValidator, times(1)).validate(TESTDATA_VALIDATION_RESULT_PUBLISH_ERROR);
        verify(messagePublisher, times(1)).publishMessage(
                Mockito.contains("\"entityId\":{\"vserver-id\":\"instanceid1\"},\"entityType\":\"entitytype1\","
                        + "\"entityLink\":\"entityLink\",\"resourceVersion\":\"resourceVersion1\","
                        + "\"entity\":{},\"violations\":[]}"));
    }

    private void setUpEntityMock(String id, String type, String resourceVersion) throws ValidationServiceException {
        when(entity.getType()).thenReturn(type);
        EntityId entityId = new EntityId("vserver-id", id);
        when(entity.getIds()).thenReturn(Collections.singletonList(entityId));
        when(entity.getEntityLink()).thenReturn(ENTITY_LINK);
        when(entity.getResourceVersion()).thenReturn(Optional.of(resourceVersion));
    }
}
