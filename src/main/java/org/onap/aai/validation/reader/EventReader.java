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

import com.jayway.jsonpath.DocumentContext;
import java.util.List;
import java.util.Optional;
import org.onap.aai.validation.config.EventReaderConfig;
import org.onap.aai.validation.exception.ValidationServiceError;
import org.onap.aai.validation.exception.ValidationServiceException;
import org.onap.aai.validation.reader.data.Entity;
import org.onap.aai.validation.util.StringUtils;

/**
 * Reads event objects.
 *
 */
public class EventReader {

    private EventReaderConfig eventReaderConfig;

    private JsonReader jsonReader;

    private EntityReader entityReader;

    /**
     *
     * @param eventReaderConfig the event reader configuration including paths to event properties
     * @param jsonReader a JSON reader
     * @param oxmReader an OXM reader to retrieve the primary key names for the entity
     */
    public EventReader(final EventReaderConfig eventReaderConfig, final JsonReader jsonReader,
            final OxmReader oxmReader) {
        this.eventReaderConfig = eventReaderConfig;
        this.jsonReader = jsonReader;
        this.entityReader = new EventEntityReader(eventReaderConfig, jsonReader, oxmReader);
    }

    ///////////////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Get the domain of the event.
     *
     * @param event a JSON String with the event contents
     * @return the domain of the event
     * @throws ValidationServiceException
     */
    public Optional<String> getEventDomain(String event) throws ValidationServiceException {
        List<String> readerResult = jsonReader.get(event, eventReaderConfig.getEventDomainPath());
        return getFirst(readerResult);
    }

    /**
     * Get the action of the event.
     *
     * @param event a JSON String with the event contents
     * @return the action of the event
     * @throws ValidationServiceException
     */
    public Optional<String> getEventAction(String event) throws ValidationServiceException {
        List<String> readerResult = jsonReader.get(event, eventReaderConfig.getEventActionPath());
        return getFirst(readerResult);
    }

    /**
     * Get the type of the event.
     *
     * @param event a JSON String with the event contents
     * @return the type of the event
     * @throws ValidationServiceException
     */
    public Optional<String> getEventType(String event) throws ValidationServiceException {
        List<String> readerResult = jsonReader.get(event, eventReaderConfig.getEventTypePath());
        return getFirst(readerResult);
    }

    /**
     * Get the entity type of the entity in the event.
     *
     * @param event a JSON String with the event contents
     * @return the type of the entity
     * @throws ValidationServiceException
     */
    public Optional<String> getEntityType(String event) throws ValidationServiceException {
        List<String> readerResult = jsonReader.get(event, eventReaderConfig.getEntityTypePath());
        return getFirst(readerResult);
    }

    /**
     * Get the entity contained in the event.
     *
     * @param event a JSON String with the event contents
     * @return the entity
     */
    public Entity getEntity(String event) throws ValidationServiceException {
        DocumentContext document = jsonReader.parse(event);

        String entityType = getValue(document, eventReaderConfig.getEntityTypePath())
                .orElseThrow(() -> new ValidationServiceException(ValidationServiceError.EVENT_READER_MISSING_PROPERTY,
                        eventReaderConfig.getEntityTypePath()));
        String topEntityType = getValue(document, eventReaderConfig.getTopEntityTypePath()).orElse(entityType);
        String entityLink = getEntityLink(document);
        String json = findEntity(event, topEntityType, entityType);

        return new Entity(json, entityType, entityLink, entityReader);
    }

    /**
     * Get the value of the JSON property defined by the path.
     *
     * @param json a JSON string
     * @param path the path to a property
     * @return the value
     * @throws ValidationServiceException if the value is not present
     */
    public String getValue(final String json, final String path) throws ValidationServiceException {
        return getFirst(jsonReader.get(json, path)).orElseThrow(
                () -> new ValidationServiceException(ValidationServiceError.EVENT_READER_MISSING_PROPERTY, path));
    }

    ///////////////////////////////////////////////////////////////////////////
    // PRIVATE METHODS
    ///////////////////////////////////////////////////////////////////////////

    private String findEntity(String event, String topEntityType, String entityType) throws ValidationServiceException {
        String json;
        if (entityType.equals(topEntityType)) {
            json = getValue(event, eventReaderConfig.getEntityPath());
        } else {
            json = findNestedEntity(event, eventReaderConfig.getNestedEntityPath(entityType));
        }
        return json;
    }

    /**
     * @param event
     * @param path
     * @return
     * @throws ValidationServiceException
     */
    private String findNestedEntity(String event, String path) throws ValidationServiceException {
        List<String> entities = jsonReader.get(event, path);
        if (entities.isEmpty()) {
            throw new ValidationServiceException(ValidationServiceError.EVENT_READER_MISSING_PROPERTY, path);
        } else if (entities.size() > 1) {
            throw new ValidationServiceException(ValidationServiceError.EVENT_READER_TOO_MANY_ENTITIES);
        }
        return entities.get(0);
    }

    private Optional<String> getFirst(List<String> l) {
        return l.stream().findFirst();
    }

    private Optional<String> getValue(final DocumentContext document, final String path) {
        return getFirst(jsonReader.getAsList(document, path));
    }

    /**
     * Gets the entity link from the event but altered by stripping a prefix identified by a delimiter configured in the
     * event reader properties configuration.
     *
     * @param document the parsed JSON event
     * @return the entity link
     * @throws ValidationServiceException
     */
    private String getEntityLink(DocumentContext document) throws ValidationServiceException {
        String entityLink = getValue(document, eventReaderConfig.getEntityLinkPath()).orElse("");
        String strippedEntityLink = null;
        try {
            strippedEntityLink = StringUtils.stripPrefixRegex(entityLink, eventReaderConfig.getEntityLinkDelimiter());
        } catch (ValidationServiceException e) {
            throw new ValidationServiceException(ValidationServiceError.EVENT_READER_PROPERTY_READ_ERROR, e);
        }
        return strippedEntityLink;
    }

    ///////////////////////////////////////////////////////////////////////////
    // GETTERS AND SETTERS
    ///////////////////////////////////////////////////////////////////////////

    /**
     *
     * @return event configuration
     */
    public EventReaderConfig getEventReaderConfig() {
        return eventReaderConfig;
    }

    /**
     *
     * @param eventReaderConfig event configuration
     */
    public void setEventReaderConfig(EventReaderConfig eventReaderConfig) {
        this.eventReaderConfig = eventReaderConfig;
    }

}
