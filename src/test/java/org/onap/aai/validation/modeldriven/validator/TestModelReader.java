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

import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import com.google.common.collect.Multimap;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import javax.inject.Inject;
import org.dom4j.Element;
import org.dom4j.Node;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.aai.validation.exception.ValidationServiceException;
import org.onap.aai.validation.modeldriven.ModelCacheManager;
import org.onap.aai.validation.modeldriven.configuration.mapping.ModelInstanceMapper;
import org.onap.aai.validation.modeldriven.parser.XMLModelParser;
import org.onap.aai.validation.modeldriven.validator.ModelReader;
import org.onap.aai.validation.test.util.TestUtil;
import org.onap.aai.validation.util.JsonUtil;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:model-validation/model-reader/test-validation-service-beans.xml"})
public class TestModelReader {

    static {
        System.setProperty("AJSC_HOME", ".");
    }

    private Element modelElement;
    private ModelInstanceMapper mapping;

    @Inject
    private ModelCacheManager cache;

    @Test
    public void testGetValues() throws Exception {
        mapping = getMapping("model-validation/model-reader/model-instance-mapping-attributes-1.json");
        modelElement = XMLModelParser
                .parse(new File("src/test/resources/model-validation/model-reader/connector-widget-id.xml"), false);

        Multimap<String, Node> values = ModelReader.getValues(modelElement, mapping, cache);

        assertThat(values.entries().size(), is(2));
        assertThat(values.containsEntry("product", null), is(true));
        assertThat(values.containsEntry("vpn-id", null), is(true));
    }

    /**
     * @throws Exception
     */
    @Test(expected = ValidationServiceException.class)
    public void testGetValuesWithUnknownPath() throws Exception {
        mapping = getMapping("model-validation/model-reader/model-instance-mapping-attributes-2.json");
        modelElement = XMLModelParser
                .parse(new File("src/test/resources/model-validation/model-reader/connector-widget-id.xml"), false);

        ModelReader.getValues(modelElement, mapping, cache);
    }

    @Test
    public void testGetValuesSingleModel() throws Exception {
        mapping = getMapping("model-validation/model-reader/model-instance-mapping-relationships.json");
        modelElement = XMLModelParser
                .parse(new File("src/test/resources/model-validation/model-reader/connector-widget-id.xml"), false);

        List<Node> expectedModels = XMLModelParser.getObjectsFromXPath(modelElement, mapping.getModel().getRoot());

        Multimap<String, Node> values = ModelReader.getValues(modelElement, mapping, cache);

        Collection<Entry<String, Node>> entries = values.entries();
        assertThat(entries.size(), is(1));
        assertThat(values.containsEntry("virtual-data-center", expectedModels.get(0)), is(true));
    }

    @Test
    public void testGetValuesMultipleModels() throws Exception {
        mapping = getMapping("model-validation/model-reader/model-instance-mapping-relationships.json");
        modelElement = XMLModelParser
                .parse(new File("src/test/resources/model-validation/model-reader/logical-link-widget-id.xml"), false);

        Multimap<String, Node> values = ModelReader.getValues(modelElement, mapping, cache);

        assertThat(values.keys().toArray(new String[] {}), arrayContainingInAnyOrder(new String[] {"logical-link",}));

        values = ModelReader.getValues(values.values().iterator().next(), mapping, cache);

        assertThat(values.keys().toArray(new String[] {}),
                arrayContainingInAnyOrder(new String[] {"pBgf", "vDbe", "ipe", "vSbg",}));
    }

    @Test
    public void testGetValuesCurrentModelNoChildrenNoValues() throws Exception {
        mapping = getMapping("model-validation/model-reader/model-instance-mapping-relationships.json");
        modelElement = XMLModelParser.parse(
                new File("src/test/resources/model-validation/model-reader/connector-widget-id-no-children-1.xml"),
                false);

        Multimap<String, Node> values = ModelReader.getValues(modelElement, mapping, cache);

        assertThat(values.isEmpty(), is(true));
    }

    @Test
    public void testGetValuesCurrentModelNoChildrenWithValues() throws Exception {
        mapping = getMapping("model-validation/model-reader/model-instance-mapping-relationships.json");
        modelElement = XMLModelParser.parse(
                new File("src/test/resources/model-validation/model-reader/connector-widget-id-no-children-2.xml"),
                false);

        Multimap<String, Node> parentValues = ModelReader.getValues(modelElement, mapping, cache);

        Multimap<String, Node> childValues =
                ModelReader.getValues(parentValues.entries().iterator().next().getValue(), mapping, cache);

        assertThat(childValues.isEmpty(), is(true));
    }

    @Test
    public void testResourceModelType() throws Exception {
        mapping = getMapping("model-validation/model-reader/model-instance-mapping-relationships.json");
        modelElement = XMLModelParser.parse(
                new File("src/test/resources/model-validation/model-reader/virtual-data-center-widget-id.xml"), false);

        Multimap<String, Node> values = ModelReader.getValues(modelElement, mapping, cache);

        assertThat(values.keys().toArray(new String[] {}),
                arrayContainingInAnyOrder(new String[] {"l2-bridge-for-wan-connector",}));
    }

    @Test
    public void testRootWithInvalidPath() throws Exception {
        mapping = getMapping("model-validation/model-reader/model-instance-mapping-relationships-root-1.json");
        modelElement = XMLModelParser.parse(
                new File("src/test/resources/model-validation/model-reader/virtual-data-center-widget-id.xml"), false);

        Multimap<String, Node> values = ModelReader.getValues(modelElement, mapping, cache);

        assertThat(values.entries(), is(empty()));
    }

    @Test
    public void testRootMissing() throws Exception {
        mapping = getMapping("model-validation/model-reader/model-instance-mapping-relationships-root-2.json");
        modelElement = XMLModelParser.parse(
                new File("src/test/resources/model-validation/model-reader/virtual-data-center-widget-id.xml"), false);

        Multimap<String, Node> values = ModelReader.getValues(modelElement, mapping, cache);
        assertThat(values.entries(), is(empty()));
    }

    @Test
    public void testFilterWithInvalidType() throws Exception {
        mapping = getMapping("model-validation/model-reader/model-instance-mapping-relationships-filter-1.json");
        modelElement = XMLModelParser
                .parse(new File("src/test/resources/model-validation/model-reader/connector-widget-id.xml"), false);

        Multimap<String, Node> values = ModelReader.getValues(modelElement, mapping, cache);

        assertThat(values.entries(), is(empty()));
    }

    @Test
    public void testFilterWithEmptyArray() throws Exception {
        mapping = getMapping("model-validation/model-reader/model-instance-mapping-relationships-filter-2.json");
        modelElement = XMLModelParser.parse(
                new File("src/test/resources/model-validation/model-reader/virtual-data-center-widget-id.xml"), false);

        Multimap<String, Node> values = ModelReader.getValues(modelElement, mapping, cache);

        assertThat(values.entries(), is(empty()));
    }

    @Test
    public void testFilterWithMissingValidProperty() throws Exception {
        mapping = getMapping("model-validation/model-reader/model-instance-mapping-relationships-filter-3.json");
        modelElement = XMLModelParser.parse(
                new File("src/test/resources/model-validation/model-reader/virtual-data-center-widget-id.xml"), false);

        Multimap<String, Node> values = ModelReader.getValues(modelElement, mapping, cache);

        assertThat(values.entries(), is(empty()));
    }

    @Test
    public void testFilterWithInvalidPath() throws Exception {
        mapping = getMapping("model-validation/model-reader/model-instance-mapping-relationships-filter-4.json");
        modelElement = XMLModelParser.parse(
                new File("src/test/resources/model-validation/model-reader/virtual-data-center-widget-id.xml"), false);

        Multimap<String, Node> values = ModelReader.getValues(modelElement, mapping, cache);

        assertThat(values.entries(), is(empty()));
    }

    @Test
    public void testFilterWithMissingPath() throws Exception {
        mapping = getMapping("model-validation/model-reader/model-instance-mapping-relationships-filter-5.json");
        modelElement = XMLModelParser.parse(
                new File("src/test/resources/model-validation/model-reader/virtual-data-center-widget-id.xml"), false);

        Multimap<String, Node> values = ModelReader.getValues(modelElement, mapping, cache);

        assertThat(values.entries(), is(empty()));
    }

    @Test
    public void testFilterMissing() throws Exception {
        mapping = getMapping("model-validation/model-reader/model-instance-mapping-relationships-filter-6.json");
        modelElement = XMLModelParser.parse(
                new File("src/test/resources/model-validation/model-reader/virtual-data-center-widget-id.xml"), false);

        Multimap<String, Node> values = ModelReader.getValues(modelElement, mapping, cache);

        assertThat(values.keys().toArray(new String[] {}),
                arrayContainingInAnyOrder(new String[] {"l2-bridge-for-wan-connector"}));
    }

    @Test
    public void testGetValuesWithMultipleModelFetch() throws Exception {
        mapping = getMapping("model-validation/model-reader/model-instance-mapping-relationships-with-id.json");
        modelElement = XMLModelParser
                .parse(new File("src/test/resources/model-validation/model-reader/connector-widget-id.xml"), false);

        Multimap<String, Node> values = ModelReader.getValues(modelElement, mapping, cache);

        assertThat(values.entries(), is(empty()));
    }

    private ModelInstanceMapper getMapping(String mappingFileName) throws Exception {
        JSONArray jsonArray = new JSONArray(TestUtil.getFileAsString(mappingFileName));
        JSONObject jsonObject = jsonArray.getJSONObject(0);

        return JsonUtil.fromJson(jsonObject.toString(), ModelInstanceMapper.class);
    }
}
