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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import javax.inject.Inject;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.onap.aai.validation.exception.ValidationServiceException;
import org.onap.aai.validation.modeldriven.configuration.mapping.ModelInstanceMapper;
import org.onap.aai.validation.modeldriven.validator.InstanceReader;
import org.onap.aai.validation.test.util.TestUtil;
import org.onap.aai.validation.util.JsonUtil;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(properties = {"schemaIngestPropLoc = src/test/resources/oxm-reader/schemaIngest.properties"})
@ContextConfiguration(locations = {"classpath:model-validation/instance-reader/test-validation-service-beans.xml"})
public class TestInstanceReader {

    static {
        System.setProperty("APP_HOME", ".");
    }

    private static ModelInstanceMapper mapping;
    private static ModelInstanceMapper mappingRootUnknown;
    private static ModelInstanceMapper mappingRootMissing;
    private static String connector;
    private static String connectorSibling;
    private static String expectedVirtualDataCenter;
    private static String expectedVirtualDataCenterModelName;
    private static String connectorModelName;
    private static String expectedLogicalLink;
    private static String expectedGenericVnf;
    private static String expectedPserver;

    @Inject
    private InstanceReader instanceReader;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        mapping = TestInstanceReader.getMapping(TestData.MAPPING.getFilename());
        mappingRootUnknown = TestInstanceReader.getMapping(TestData.MAPPING_ROOT_UNKNOWN.getFilename());
        mappingRootMissing = TestInstanceReader.getMapping(TestData.MAPPING_ROOT_MISSING.getFilename());
        connector = TestUtil.getFileAsString(TestData.CONNECTOR.getFilename());
        connectorModelName = TestUtil.getFileAsString(TestData.CONNECTOR_MODEL_NAME.getFilename());
        connectorSibling = TestUtil.getFileAsString(TestData.CONNECTOR_SIBLING.getFilename());
        expectedVirtualDataCenter = TestUtil.getFileAsString(TestData.EXPECTED_VDC.getFilename());
        expectedVirtualDataCenterModelName = TestUtil.getFileAsString(TestData.EXPECTED_VDC_MODEL_NAME.getFilename());
        expectedLogicalLink = TestUtil.getFileAsString(TestData.EXPECTED_LOGICAL_LINK.getFilename());
        expectedGenericVnf = TestUtil.getFileAsString(TestData.EXPECTED_GENERIC_VNF.getFilename());
        expectedPserver = TestUtil.getFileAsString(TestData.EXPECTED_PSERVER.getFilename());

    }

    enum TestData {
        // @formatter:off
		MAPPING                 ("model-validation/instance-reader/model-instance-mapping.json_conf"),
		MAPPING_ROOT_UNKNOWN    ("model-validation/instance-reader/model-instance-mapping-root-unknown.json_conf"),
		MAPPING_ROOT_MISSING    ("model-validation/instance-reader/model-instance-mapping-root-missing.json_conf"),
		CONNECTOR               ("model-validation/instance-reader/connector.json"),
		CONNECTOR_MODEL_NAME    ("model-validation/instance-reader/connector-model-name.json"),
		CONNECTOR_SIBLING       ("model-validation/instance-reader/connector-sibling-inventory-items.json"),
		EXPECTED_VDC            ("model-validation/instance-reader/expected-virtual-data-center.json"),
		EXPECTED_VDC_MODEL_NAME ("model-validation/instance-reader/expected-virtual-data-center-model-name.json"),
		EXPECTED_LOGICAL_LINK   ("model-validation/instance-reader/expected-logical-link.json"),
		EXPECTED_GENERIC_VNF    ("model-validation/instance-reader/expected-generic-vnf.json"),
		EXPECTED_PSERVER        ("model-validation/instance-reader/expected-pserver.json");

		private String filename;
		TestData(String filename) {this.filename = filename;}
		public String getFilename() {return this.filename;}
		// @formatter:on
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testGetValuesNoModelName() throws Exception {
        // Set expectation
        JsonParser jsonParser = new JsonParser();
        JsonElement jsonElement = jsonParser.parse(expectedVirtualDataCenter);
        String expectedValue = jsonElement.toString();

        // Method under test
        Multimap<String, String> values = instanceReader.getValues(connector, mapping);

        assertFalse(values.isEmpty());
        assertThat(values.keys().iterator().next(), is(equalTo("virtual-data-center")));
        assertThat(values.get("virtual-data-center").iterator().next(), is(equalTo(expectedValue)));
    }

    @Test
    public void testGetValuesWithModelName() throws Exception {
        // Set expectation
        JsonParser jsonParser = new JsonParser();
        JsonElement jsonElement = jsonParser.parse(expectedVirtualDataCenterModelName);
        String expectedValue = jsonElement.toString();

        // Method under test
        Multimap<String, String> values = instanceReader.getValues(connectorModelName, mapping);

        assertFalse(values.isEmpty());
        assertThat(values.keys().iterator().next(), is(equalTo("Test VC Model Name")));
        assertThat(values.get("Test VC Model Name").iterator().next(), is(equalTo(expectedValue)));
    }

    @Test
    public void testNavigateInstance() throws Exception {
        // Set expectation
        JsonParser jsonParser = new JsonParser();
        JsonElement jsonElement = jsonParser.parse(expectedLogicalLink);
        String expectedValue = jsonElement.toString();

        // Method under test
        Multimap<String, String> values = instanceReader.getValues(connector, mapping);

        String virtualDataCenterInstance = values.get("virtual-data-center").iterator().next();

        // Method under test
        values = instanceReader.getValues(virtualDataCenterInstance, mapping);

        assertThat(values.keys().iterator().next(), is(equalTo("Test LL Model Name")));
        assertThat(values.get("Test LL Model Name").iterator().next(), is(equalTo(expectedValue)));
    }

    @Test
    public void testNavigateInstanceWithSiblingInventoryItems() throws Exception {
        // Set expectations
        JsonParser jsonParser = new JsonParser();
        JsonElement genericVnfJsonElement = jsonParser.parse(expectedGenericVnf);
        String expectedGenericVnf = genericVnfJsonElement.toString();

        JsonElement pserverJsonElement = jsonParser.parse(expectedPserver);
        String expectedPserver = pserverJsonElement.toString();

        // Method under test
        Multimap<String, String> values = instanceReader.getValues(connectorSibling, mapping);

        String virtualDataCenterInstance = values.get("virtual-data-center").iterator().next();

        // Method under test
        values = instanceReader.getValues(virtualDataCenterInstance, mapping);

        String logicalLinkInstance = values.get("Test LL Model Name").iterator().next();

        // Method under test
        values = instanceReader.getValues(logicalLinkInstance, mapping);

        assertThat(values.get("generic-vnf").iterator().next(), is(equalTo(expectedGenericVnf)));
        assertThat(values.get("pserver").iterator().next(), is(equalTo(expectedPserver)));
    }

    @Test
    public void testGetValuesRootUnknown() throws Exception {
        Multimap<String, String> values = instanceReader.getValues(connector, mappingRootUnknown);

        assertThat(values.isEmpty(), is(true));
    }

    @Test
    public void testGetValuesRootMissing() throws Exception {
        thrown.expect(ValidationServiceException.class);
        thrown.expectMessage("VS-604");

        instanceReader.getValues(connector, mappingRootMissing);
    }

    @Test
    public void testGetInstanceTypeNoModelName() throws Exception {
        String instanceType = instanceReader.getInstanceType(connector);
        assertThat(instanceType, is("connector"));
    }

    @Test
    public void testGetModelName() throws Exception {
        String instanceType = instanceReader.getModelName(connectorModelName);
        assertThat(instanceType, is("Test Connector Model Name"));
    }

    @Test
    public void testGetInstanceIdNoModelName() throws Exception {
        String instanceId = instanceReader.getInstanceId(connector);
        assertThat(instanceId, is("c7611ebe-c324-48f1-8085-94aef0c12fd"));
    }

    @Test
    public void testGetInstanceIdModelName() throws Exception {
        String instanceId = instanceReader.getInstanceId(connectorModelName);
        assertThat(instanceId, is("c7611ebe-c324-48f1-8085-94aef0c12fd"));
    }

    @Test
    public void testGetResourceVersion() throws Exception {
        String resourceVersion = instanceReader.getResourceVersion(connector);

        assertThat(resourceVersion, is("1467975776"));
    }

    private static ModelInstanceMapper getMapping(String mappingFileName) throws Exception {
        JSONArray jsonArray = new JSONArray(TestUtil.getFileAsString(mappingFileName));
        JSONObject jsonObject = jsonArray.getJSONObject(0);

        return JsonUtil.fromJson(jsonObject.toString(), ModelInstanceMapper.class);
    }
}
