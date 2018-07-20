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
package org.onap.aai.validation.reader;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.onap.aai.validation.config.EventReaderConfig;
import org.onap.aai.validation.exception.ValidationServiceError;
import org.onap.aai.validation.exception.ValidationServiceException;
import org.onap.aai.validation.reader.data.EntityId;

/**
 * An entity reader for reading A&AI events. This implementation is used by the EventReader
 *
 */
public class EventEntityReader implements EntityReader {

    private JsonReader jsonReader;
    private OxmReader oxmReader;

    private EventReaderConfig config;

    /**
     * @param eventReaderConfig
     * @param jsonReader
     * @param oxmReader
     */
    public EventEntityReader(final EventReaderConfig eventReaderConfig, final JsonReader jsonReader,
            final OxmReader oxmReader) {
        this.jsonReader = jsonReader;
        this.oxmReader = oxmReader;
        this.config = eventReaderConfig;
    }

    /**
     * Parse the supplied json and return the content (values) specified by the supplied path
     *
     * @param json
     * @param path
     * @return either a String or an Array of objects
     * @throws ValidationServiceException
     */
    @Override
    public Object getObject(String json, String path) throws ValidationServiceException {
        return jsonReader.getObject(jsonReader.parse(json), path);
    }

    public String getEntityResourceVersionPath() {
        return config.getEntityResourceVersionPath();
    }

    @Override
    public List<EntityId> getIds(String json, String type) throws ValidationServiceException {
        List<EntityId> ids = new ArrayList<>();
        for (String pk : oxmReader.getPrimaryKeys(type)) {
            String pkPaths = config.getEntityIdPath(pk);
            String pkValue = getPropertyForMultiplePaths(json, pkPaths).orElseThrow(
                    () -> new ValidationServiceException(ValidationServiceError.EVENT_READER_MISSING_PROPERTY,
                            pkPaths));
            ids.add(new EntityId(pk, pkValue));
        }
        return ids;
    }

    /**
     * Get an entity property from an entity in JSON format.
     *
     * @param json the entity
     * @param path the JSON path to the property
     * @return an optional property value
     * @throws ValidationServiceException
     */
    public Optional<String> getProperty(String json, String path) throws ValidationServiceException {
        return jsonReader.get(json, path).stream().findFirst();
    }

    @Override
    public Optional<String> getResourceVersion(String json) throws ValidationServiceException {
        return getPropertyForMultiplePaths(json, getEntityResourceVersionPath());
    }

    /**
     * Takes a comma separated list of jsonpaths and applies each one in turn until a value is found.
     * 
     * @param json The json to search
     * @param multiplePaths Comma separated list of jsonpath strings
     * @return The value of the first jsonpath string that returns a value
     * @throws ValidationServiceException
     */
    private Optional<String> getPropertyForMultiplePaths(String json, String multiplePaths)
            throws ValidationServiceException {
        Optional<String> propertyValue = Optional.empty();
        for (String path : multiplePaths.split(",")) {
            propertyValue = getProperty(json, path);
            if (propertyValue.isPresent()) {
                break;
            }
        }
        return propertyValue;
    }

}
