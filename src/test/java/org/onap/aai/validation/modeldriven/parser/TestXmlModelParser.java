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
package org.onap.aai.validation.modeldriven.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.dom4j.Element;
import org.dom4j.Node;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TestXmlModelParser {

    static {
        System.setProperty("APP_HOME", ".");
    }

    private static final String CONNECTOR_MODEL_ID = "460c6de2-a92b-4e3b-9ba3-538ce782b2fa";
    private static final String MODEL_ID_ATTRIBUTE = "model-name-version-id";

    private Element modelElement;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        File modelFile = new File("src/test/resources/model-validation/model-parser/all-models.xml");
        Element modelsElement = XMLModelParser.parse(modelFile, false);
        modelElement = XMLModelParser.getModelElementWithId(modelsElement, MODEL_ID_ATTRIBUTE, CONNECTOR_MODEL_ID);
    }

    @Test
    public void testParseXmlModelFile() throws Exception {
        assertEquals("Invalid model element name.", "model", modelElement.getName());
    }

    @Test
    public void testGetAttributes() throws Exception {
        String attrsXPath = "metadata/metadatum/metaname";
        List<Node> attrNodes = XMLModelParser.getObjectsFromXPath(modelElement, attrsXPath);
        assertEquals("Unexpected number of attributes.", 2, attrNodes.size());

        List<String> validAttrs = new ArrayList<>();
        validAttrs.add("a");
        validAttrs.add("b");

        List<String> actualAttrs = new ArrayList<>();
        for (Node node : attrNodes) {
            actualAttrs.add(node.getText());
        }

        assertTrue("Unexpected attribute names.", validAttrs.containsAll(actualAttrs));
    }

    @Test
    public void testGetRelatedObjects() throws Exception {
        String relObjsXPath = "model-elements/model-element";
        List<Node> relatedNodes = XMLModelParser.getObjectsFromXPath(modelElement, relObjsXPath);
        assertEquals("Unexpected number of related objects.", 1, relatedNodes.size());

        Node relatedNode = relatedNodes.get(0).selectSingleNode("model-element-uuid");
        assertEquals("Unexpected related object UUID.", "71b825be-febf-45f7-b86a-ca0e3de19c90", relatedNode.getText());
    }

    @Test
    public void testModelValidationFailure() throws Exception {
        File modelFile = new File("src/test/resources/model-validation/model-parser/all-models.xml");

        assertNull("Validation failure should result in null being returned.", XMLModelParser.parse(modelFile, true));
    }
}
