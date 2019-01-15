/**
 * ============LICENSE_START===================================================
 * Copyright (c) 2018 Amdocs
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
package org.onap.aai.validation.reader;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceConfigurationError;
import org.eclipse.persistence.internal.oxm.mappings.Descriptor;
import org.eclipse.persistence.jaxb.dynamic.DynamicJAXBContext;
import org.onap.aai.nodes.NodeIngestor;
import org.onap.aai.setup.SchemaVersion;
import org.onap.aai.validation.exception.ValidationServiceError;
import org.onap.aai.validation.exception.ValidationServiceException;
import org.onap.aai.validation.util.StringUtils;

/**
 * Reads the primary keys of a specific OXM resource (identified by version).
 */
public class OxmReader {

    private NodeIngestor nodeIngestor;
    private SchemaVersion version;
    private Map<String, List<String>> primaryKeysMap = new HashMap<>();

    public OxmReader(NodeIngestor nodeIngestor, SchemaVersion version) {
        this.nodeIngestor = nodeIngestor;
        this.version = version;
    }

    /**
     * Get the primary keys for a given entity type.
     *
     * @param entityType the name of the entity type
     * @return the primary keys for the entity type
     * @throws ValidationServiceException
     */
    public List<String> getPrimaryKeys(String entityType) throws ValidationServiceException {
        if (primaryKeysMap.isEmpty()) {
            throw new ValidationServiceException(ValidationServiceError.OXM_MISSING_KEY, entityType);
        }

        List<String> primaryKeys = primaryKeysMap.get(entityType);
        return primaryKeys != null ? primaryKeys : Collections.emptyList();
    }

    /**
     * Populate a Map of primary keys for the entity types defined in the OXM.<br>
     * The primary keys are keyed by the Default Root Element.
     */
    public void init() {
        for (Descriptor<?, ?, ?, ?, ?, ?, ?, ?, ?, ?> descriptor : getObjectDescriptors()) {
            if (!descriptor.getPrimaryKeyFieldNames().isEmpty()) {
                primaryKeysMap.put(descriptor.getDefaultRootElement(),
                        StringUtils.stripSuffix(descriptor.getPrimaryKeyFieldNames(), "/text()"));
            }
        }
    }

    /**
     * Gets a list of descriptors from the OXM. These descriptor objects contain property information about each entity
     * type.
     *
     * @return list of descriptors
     */
    @SuppressWarnings("rawtypes")
    private List<Descriptor> getObjectDescriptors() {
        DynamicJAXBContext dynamicJaxbContext = nodeIngestor.getContextForVersion(version);
        if (dynamicJaxbContext == null) {
            throw new ServiceConfigurationError("OXM Version " + version + " was not ingested.");
        }
        return dynamicJaxbContext.getXMLContext().getDescriptors();
    }

    public Map<String, List<String>> getPrimaryKeysMap() {
        return primaryKeysMap;
    }

}
