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
package org.onap.aai.validation.ruledriven.rule;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.inject.Inject;
import org.junit.Test;
import org.onap.aai.validation.reader.OxmReader;
import org.onap.aai.validation.reader.data.AttributeValues;
import org.onap.aai.validation.ruledriven.RuleDrivenValidator;
import org.onap.aai.validation.ruledriven.rule.Rule;

public class TestConfigurationLoader {

    static {
        System.setProperty("APP_HOME", ".");
    }

    private enum AAIRelation {
        RELATED_TO("related-to"),
        PROPERTY_KEY("property-key"),
        PROPERTY_VALUE("property-value");

        private final String text;

        /**
         * @param text
         */
        private AAIRelation(final String text) {
            this.text = text;
        }

        /*
         * (non-Javadoc)
         *
         * @see java.lang.Enum#toString()
         */
        @Override
        public String toString() {
            return text;
        }
    }

    @Inject
    private OxmReader oxmReader;

    /**
     * If vserver is related to an image with image-name = 'TRINITY' then generic-vnf.vnf-name must match xxxxnnnnv
     */
    @Test
    public void testTrinityRule() throws Exception {
        Path configurationPath = Paths.get("bundleconfig/etc/rules");

        RuleDrivenValidator validator = new RuleDrivenValidator(configurationPath, oxmReader, null, null);
        validator.initialise();

        // Find the trinity rule
        Rule trinityRule = null;
        for (Rule rule : validator.getRulesForEntity("vserver", "aai-event")) {
            if (rule.getName().contains("TRINITY") && rule.getName().contains("generic-vnf")) {
                trinityRule = rule;
            }
        }

        // Create a set of relationship objects to be passed to the rule
        Set<JsonObject> relationships = new LinkedHashSet<>();
        AttributeValues attributeValues = new AttributeValues();
        attributeValues.put("relationship-list.relationship[*]", relationships);

        // Test the rule against the relationships
        RuleTester ruleTester = new RuleTester(trinityRule, attributeValues);
        ruleTester.test(true);

        JsonObject genericVnfData = createRelationshipData(relationships, "generic-vnf");
        ruleTester.test(true);

        // Add a new object for the image relationship
        JsonObject imageData = createRelationshipData(relationships, "image");
        ruleTester.test(true);

        createRelationshipData(relationships, "pserver");
        ruleTester.test(true);

        // Add a new JSON object for the image name
        JsonObject imageNameProperty = createRelatedToProperty(imageData);
        ruleTester.test(true);

        setPropertyKey(imageNameProperty, "image.image-name");
        ruleTester.test(true);

        setPropertyValue(imageNameProperty, "not TRINITY");
        ruleTester.test(true);

        setPropertyValue(imageNameProperty, "TRINITY");
        ruleTester.test(false);

        JsonObject vnfNameProperty = createRelatedToProperty(genericVnfData);
        ruleTester.test(false);

        setPropertyKey(vnfNameProperty, "generic-vnf.vnf-name");
        ruleTester.test(false);

        setPropertyValue(vnfNameProperty, "invalid");
        ruleTester.test(false);

        setPropertyValue(vnfNameProperty, "");
        ruleTester.test(false);

        setPropertyValue(vnfNameProperty, "bmsx0001v");
        ruleTester.test(true);

        // Add another new object for a different image relationship
        JsonObject image2Data = createRelationshipData(relationships, "image");
        ruleTester.test(true);

        JsonObject image2NameProperty = createRelatedToProperty(image2Data);
        ruleTester.test(true);

        setPropertyKey(image2NameProperty, "image.image-name");
        ruleTester.test(true);

        setPropertyValue(image2NameProperty, "not TRINITY");
        ruleTester.test(true);

        setPropertyValue(vnfNameProperty, "invalid");
        ruleTester.test(false);

        setPropertyValue(imageNameProperty, "not TRINITY");
        ruleTester.test(true);

        setPropertyValue(image2NameProperty, "TRINITY");
        ruleTester.test(false);

        setPropertyValue(image2NameProperty, "also not TRINITY");
        ruleTester.test(true);
    }

    /**
     * @param relationshipData
     * @return
     */
    private JsonObject createRelatedToProperty(JsonObject relationshipData) {
        JsonObject imageNameProperty = new JsonObject();
        JsonElement jsonArray = new JsonArray();
        jsonArray.getAsJsonArray().add(imageNameProperty);
        relationshipData.add("related-to-property", jsonArray);
        return imageNameProperty;
    }

    /**
     * @param relationships
     * @param relatedObject
     * @return
     */
    private JsonObject createRelationshipData(Set<JsonObject> relationships, String relatedObject) {
        JsonObject relationData = new JsonObject();
        relationData.addProperty(AAIRelation.RELATED_TO.toString(), relatedObject);
        relationships.add(relationData);
        return relationData;
    }

    private void setPropertyKey(JsonObject objectMap, String propertyKeyName) {
        objectMap.addProperty(AAIRelation.PROPERTY_KEY.toString(), propertyKeyName);
    }

    private void setPropertyValue(JsonObject objectMap, String propertyValue) {
        objectMap.addProperty(AAIRelation.PROPERTY_VALUE.toString(), propertyValue);
    }
}
