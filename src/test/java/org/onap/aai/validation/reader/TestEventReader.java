/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright (c) 2018-2019 AT&T Intellectual Property. All rights reserved.
 * Copyright (c) 2018-2019 European Software Marketing Ltd.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */
package org.onap.aai.validation.reader;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.aai.validation.exception.ValidationServiceException;
import org.onap.aai.validation.reader.data.Entity;
import org.onap.aai.validation.reader.data.EntityId;
import org.onap.aai.validation.test.util.TestUtil;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(locations = {"classpath:oxm-reader/schemaIngest.properties"})
@ContextConfiguration(locations = {"classpath:event-reader/test-validation-service-beans.xml"})
public class TestEventReader {

    static {
        System.setProperty("APP_HOME", ".");
    }

    @Inject
    private EventReader eventReader;

    private static String vserverEvent;
    private static String genericVnfEvent;
    private static String invalidEvent1;
    private static String invalidEvent2;
    private static String invalidEvent3;
    private static String invalidEvent4;
    private static String invalidEvent5;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        vserverEvent = TestUtil.getFileAsString(TestData.VSERVER.getFilename());
        genericVnfEvent = TestUtil.getFileAsString(TestData.GENERIC_VNF.getFilename());
        invalidEvent1 = TestUtil.getFileAsString(TestData.INVALID_1.getFilename());
        invalidEvent2 = TestUtil.getFileAsString(TestData.INVALID_2.getFilename());
        invalidEvent3 = TestUtil.getFileAsString(TestData.INVALID_3.getFilename());
        invalidEvent4 = TestUtil.getFileAsString(TestData.INVALID_4.getFilename());
        invalidEvent5 = TestUtil.getFileAsString(TestData.INVALID_5.getFilename());
    }

    enum TestData {
        // @formatter:off
		VSERVER      ("event-reader/vserver-create-event.json"),
		GENERIC_VNF  ("event-reader/generic-vnf-create-event.json"),
        INVALID_1      ("event-reader/invalid-event-1.json"),
        INVALID_2      ("event-reader/invalid-event-2.json"),
        INVALID_3      ("event-reader/invalid-event-3.json"),
        INVALID_4      ("event-reader/invalid-event-4.json"),
        INVALID_5      ("event-reader/invalid-event-5.json");
        // @formatter:on

        private String filename;

        TestData(String filename) {
            this.filename = filename;
        }

        public String getFilename() {
            return this.filename;
        }
    }

    @Test
    public void testGetEventDomain() throws Exception {
        Optional<String> eventType = eventReader.getEventDomain(vserverEvent);

        assertThat(eventType.get(), is("devINT1"));
    }

    @Test
    public void testGetEventAction() throws Exception {
        Optional<String> action = eventReader.getEventAction(vserverEvent);

        assertThat(action.get(), is("CREATE"));
    }

    @Test
    public void testGetEventType() throws Exception {
        Optional<String> eventType = eventReader.getEventType(vserverEvent);

        assertThat(eventType.isPresent(), is(true));
        assertThat(eventType.get(), is("AAI-EVENT"));
    }

    @Test(expected = ValidationServiceException.class)
    public void testGetEventTypeMalformedJson() throws Exception {
        eventReader.getEventType("this is malformed");
    }

    @Test
    public void testGetEventTypeFromUnrecognisableEvent() throws Exception {
        Optional<String> eventType = eventReader.getEventType("this-is-not-an-event-but-is-valid-json");

        assertThat(eventType.isPresent(), is(false));
    }

    @Test
    public void testGetEventTypeThatIsMissing() throws Exception {
        Optional<String> eventType = eventReader.getEventType(invalidEvent1);

        assertThat(eventType.isPresent(), is(false));
    }

    @Test
    public void testGetEntityType() throws Exception {
        Optional<String> entityType = eventReader.getEntityType(vserverEvent);

        assertThat(entityType.get(), is("vserver"));
    }

    @Test
    public void testGetEntity() throws Exception {
        Entity entity = eventReader.getEntity(genericVnfEvent);

        assertThat(entity.getType(), is("generic-vnf"));

        // Dig deeper to check we have the object we want
        JsonParser parser = new JsonParser();
        JsonElement jsonElement = parser.parse(entity.getJson());
        String id = jsonElement.getAsJsonObject().get("vnf-id").getAsString();

        assertThat(id, is("VAPP-1581"));
    }

    @Test
    public void testEntityLink() throws Exception {
        Entity entity = eventReader.getEntity(vserverEvent);

        assertThat(entity.getEntityLink(), is(
                "cloud-infrastructure/cloud-regions/cloud-region/region1/AAIregion1/tenants/tenant/example-tenant-id-val-88551/vservers/vserver/example-vserver-id-val-34666"));
    }

    @Test(expected = ValidationServiceException.class)
    public void testGetEntityWithMissingEntityType() throws Exception {
        eventReader.getEntity(invalidEvent1);
    }

    @Test(expected = ValidationServiceException.class)
    public void testGetEntityWithUnknownEntityType() throws Exception {
        eventReader.getEntity(invalidEvent2);
    }

    @Test
    public void testGetNestedEntity() throws Exception {
        Entity entity = eventReader.getEntity(vserverEvent);

        assertThat(entity.getType(), is("vserver"));

        // Dig deeper to check we have the object we want
        JsonParser parser = new JsonParser();
        JsonElement jsonElement = parser.parse(entity.getJson());
        String id = jsonElement.getAsJsonObject().get("vserver-id").getAsString();

        assertThat(id, is("example-vserver-id-val-34666"));
    }

    @Test(expected = ValidationServiceException.class)
    public void testTooManyNestedEntitiesThrowsException() throws Exception {
        eventReader.getEntity(invalidEvent4);
    }

    @Test
    public void testGetEntityIds() throws Exception {
        Entity entity = eventReader.getEntity(vserverEvent);

        List<EntityId> ids = entity.getIds();

        assertThat(ids, hasSize(1));
        EntityId entityId = ids.get(0);

        assertThat(entityId.getPrimaryKey(), is("vserver-id"));
        assertThat(entityId.getValue(), is("example-vserver-id-val-34666"));
    }

    @Test
    public void testCompareEntityIds() throws Exception {
        EntityId entityId = new EntityId();
        assertThat(entityId, is(not(equalTo(null))));

        entityId.setPrimaryKey("key");
        assertThat(entityId, is(not(equalTo(null))));
        entityId.setValue("value");
        assertThat(entityId, is(not(equalTo(null))));

        EntityId other = new EntityId();
        assertThat(entityId, is(not(equalTo(other))));

        other.setPrimaryKey("key");
        assertThat(entityId, is(not(equalTo(other))));

        other.setValue("value");
        assertThat(entityId, is(equalTo(other)));

        // Force call to hashCode()
        assertThat(entityId.hashCode(), is(equalTo(other.hashCode())));
    }

    @Test
    public void testGetEntityIdsForUnknownEntityType() throws Exception {
        Entity entity = eventReader.getEntity(invalidEvent3);

        List<EntityId> ids = entity.getIds();

        assertThat(ids, is(empty()));
    }

    @Test
    public void testGetResourceVersion() throws Exception {
        Entity entity = eventReader.getEntity(vserverEvent);

        Optional<String> resourceVersion = entity.getResourceVersion();

        assertThat(resourceVersion.isPresent(), is(true));
        assertThat(resourceVersion.get(), is("1464193654"));
    }

    @Test
    public void testGetResourceVersionMissing() throws Exception {
        Entity entity = eventReader.getEntity(invalidEvent5);

        Optional<String> resourceVersion = entity.getResourceVersion();
        assertThat(resourceVersion.isPresent(), is(false));
    }

    @Test
    public void testGetProperty() throws Exception {
        Entity entity = eventReader.getEntity(vserverEvent);

        String resourceVersion = (String) entity.getAttributeValues(Arrays.asList("prov-status")).get("prov-status");

        assertThat(resourceVersion, is("PREPROV"));
    }

    @Test
    public void testEntityLinkIsStripped() throws Exception {
        Entity entity = eventReader.getEntity(vserverEvent);
        String entityLink = entity.getEntityLink();
        assertThat(entityLink,
                is("cloud-infrastructure/cloud-regions/cloud-region/"
                        + "region1/AAIregion1/tenants/tenant/example-tenant-id-val-88551/"
                        + "vservers/vserver/example-vserver-id-val-34666"));
    }
}
