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
package org.onap.aai.validation.ruledriven.mock;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import com.google.gson.JsonObject;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.onap.aai.validation.Validator;
import org.onap.aai.validation.exception.ValidationServiceException;
import org.onap.aai.validation.reader.EventReader;
import org.onap.aai.validation.reader.data.AttributeValues;
import org.onap.aai.validation.reader.data.Entity;
import org.onap.aai.validation.reader.data.EntityId;
import org.onap.aai.validation.result.ValidationResult;
import org.onap.aai.validation.result.Violation;
import org.onap.aai.validation.ruledriven.RuleDrivenValidator;

/**
 * Test that the rules present under bundleconfig/etc/rules/ can be loaded and evaluated (using a mocked event).
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class TestDefaultRules {

    static {
        System.setProperty("APP_HOME", ".");
    }

    enum TestCase {
        NULL,
        VSERVER;
    }

    // Data returned by the mocked EventReader
    enum TestData {
        // @formatter:off
		ENTITTY_DATA          ("vserver dummy json data"),
		ENTITTY_TYPE          ("vserver"),
		RESOURCE_VERSION_VALUE("1476735182"),
		VSERVER_ID_KEY        ("vserver-id"),
		VSERVER_ID_VALUE      ("13b629a4-87ae-492d-943f-acb8f3d9c3d9");
		// @formatter:on

        private String value;

        TestData(String value) {
            this.value = value;
        }
    }

    @Mock
    private EventReader eventReader;

    @Mock
    private Entity entity;

    private Validator ruleDrivenValidator;

    @Before
    public void createMockEventReader() throws ValidationServiceException {
        when(eventReader.getEventType(TestCase.VSERVER.name())).thenReturn(Optional.of("aai-event"));
        when(eventReader.getEventType(TestCase.NULL.name())).thenReturn(null);

        when(eventReader.getEntityType(anyString())).thenReturn(Optional.of(TestData.ENTITTY_TYPE.value));
        when(eventReader.getEntity(anyString())).thenReturn(entity);

        // Mocked entity returned by the event reader
        when(entity.getType()).thenReturn(TestData.ENTITTY_TYPE.value);
        when(entity.getResourceVersion()).thenReturn(Optional.of(TestData.RESOURCE_VERSION_VALUE.value));

        EntityId entityId = new EntityId(TestData.VSERVER_ID_KEY.value, TestData.VSERVER_ID_VALUE.value);
        when(entity.getIds()).thenReturn(new ArrayList<>(Arrays.asList(entityId)));

        // Return dummy values for any requested attributes
        when(entity.getAttributeValues(anyListOf(String.class))).thenAnswer(new Answer<AttributeValues>() {
            @SuppressWarnings("unchecked")
            @Override
            public AttributeValues answer(InvocationOnMock invocation) {
                AttributeValues attributeValues = new AttributeValues();
                for (String attribute : (List<String>) invocation.getArguments()[0]) {
                    if (attribute.contains("[*]")) {
                        attributeValues.put(attribute, Collections.emptyList());
                    } else {
                        attributeValues.put(attribute, "");
                    }
                }
                return attributeValues;
            }
        });
    }

    @Before
    public void createRuleDrivenValidator() throws ValidationServiceException {
        Path configurationPath = Paths.get("bundleconfig/etc/rules");
        ruleDrivenValidator = new RuleDrivenValidator(configurationPath, null, eventReader, null);
    }

    @Test
    public void testExecuteRulesForVserver() throws Exception {
        List<ValidationResult> results = ruleDrivenValidator.validate(TestCase.VSERVER.name());
        assertThat(results.size(), is(1));

        ValidationResult validationResult = results.get(0);
        assertThat(validationResult.getEntityType(), is(equalTo(TestData.ENTITTY_TYPE.value)));
        JsonObject expectedEntityId = new JsonObject();
        expectedEntityId.addProperty(TestData.VSERVER_ID_KEY.value, TestData.VSERVER_ID_VALUE.value);
        assertThat(validationResult.getEntityId(), is(equalTo(expectedEntityId)));
        assertThat(validationResult.getViolations().size(), is(2));

        Violation violation = validationResult.getViolations().get(0);
        assertThat(violation.getCategory(), is(equalTo("MISSING_REL")));
    }

}
