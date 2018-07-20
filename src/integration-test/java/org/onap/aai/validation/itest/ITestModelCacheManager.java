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
package org.onap.aai.validation.itest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.aai.validation.config.ModelConfig;
import org.onap.aai.validation.config.RestConfig;
import org.onap.aai.validation.exception.ValidationServiceException;
import org.onap.aai.validation.modeldriven.ModelCacheManager;
import org.onap.aai.validation.modeldriven.ModelId;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:model-cache-manager/itest-validation-service-beans.xml"})
public class ITestModelCacheManager {

    static {
        System.setProperty("AJSC_HOME", ".");
        System.setProperty("CONFIG_HOME", System.getProperty("user.dir") + File.separator);
    }

    private static final String MODEL_ID_ATTRIBUTE = "model-invariant-id";
    private static final String CONNECTOR_MODEL_ID = "connector-widget-id";

    @Inject
    private RestConfig restConfig;

    @Inject
    private RestConfig fileBasedRestConfig;

    @Inject
    private ModelConfig modelConfig;

    @Test
    public void testReadModelFromRestClient() throws ValidationServiceException {
        ModelCacheManager modelCacheManager = new ModelCacheManager(modelConfig, restConfig);
        Element modelElement = modelCacheManager.get(new ModelId(MODEL_ID_ATTRIBUTE, CONNECTOR_MODEL_ID));
        assertNotNull("Failed to retrieve any models from the server!", modelElement);
        assertEquals("Failed to retrieve the correct model from the server!", "model", modelElement.getName());
    }

    @Test
    public void testReadInvalidModelFromRestClient() throws ValidationServiceException {
        ModelCacheManager modelCacheManager = new ModelCacheManager(modelConfig, restConfig);
        String modelId = "non-existent-model-id";

        assertNull("Invalid model ID should return null!",
                modelCacheManager.get(new ModelId(MODEL_ID_ATTRIBUTE, modelId)));
    }

    @Test
    public void testReadModelFromCache() throws ValidationServiceException {
        ModelCacheManager modelCacheManager = new ModelCacheManager(modelConfig, restConfig);

        // Put a test element into the cache
        modelCacheManager.put(new ModelId(MODEL_ID_ATTRIBUTE, CONNECTOR_MODEL_ID), new DefaultElement("test"));
        Element modelElement = modelCacheManager.get(new ModelId(MODEL_ID_ATTRIBUTE, CONNECTOR_MODEL_ID));

        assertEquals("Failed to retrieve model from cache!", "test", modelElement.getName());
    }

    @Test
    public void testCacheExpiry() throws ValidationServiceException {
        ModelCacheManager modelCacheManager = new ModelCacheManager(modelConfig, restConfig);
        String testModelId = "test-model-id";

        // Test cache expiry time is set to 1 second in the test config.
        // Put a test element into the cache manually.
        modelCacheManager.put(new ModelId(MODEL_ID_ATTRIBUTE, testModelId), new DefaultElement("test"));
        // Get try to get the element from the cache immediately.
        Element modelElement = modelCacheManager.get(new ModelId(MODEL_ID_ATTRIBUTE, testModelId));
        // Should not have expired yet.
        assertEquals("Failed to retrieve model from cache!", "test", modelElement.getName());

        // wait for the cache to expire.
        try {
            TimeUnit.MILLISECONDS.sleep(2500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Now when we try to get it again it should have expired and thus the cache should try to retrieve the model
        // from the server using the test model id, which should return null.
        assertNull("Invalid model ID should return null!",
                modelCacheManager.get(new ModelId(MODEL_ID_ATTRIBUTE, testModelId)));
    }

    @Test
    public void testReadModelFromFile() throws ValidationServiceException {
        ModelCacheManager modelCacheManager = new ModelCacheManager(modelConfig, fileBasedRestConfig);
        Element modelElement = modelCacheManager.get(new ModelId(MODEL_ID_ATTRIBUTE, CONNECTOR_MODEL_ID));
        assertNotNull("Model ID not found in cache!", modelElement);
        assertEquals("Failed to retrieve model from server!", "model", modelElement.getName());
    }
}
