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
package org.onap.aai.validation.config;

import org.springframework.beans.factory.annotation.Value;

/**
 * Loaded via Spring from src/main/resources/event-reader.properties.
 */
public class EventReaderConfig extends PropertiesConfig {

    @Value("${event.domain.path}")
    private String eventDomainPath;

    @Value("${event.action.path}")
    private String eventActionPath;

    @Value("${event.type.path}")
    private String eventTypePath;

    @Value("${event.entity.type.path}")
    private String entityTypePath;

    @Value("${event.entity.type.top.path}")
    private String topEntityTypePath;

    @Value("${event.entity.link.path}")
    private String entityLinkPath;

    @Value("${event.entity.link.delimiter}")
    private String entityLinkDelimiter;

    @Value("${event.entity.path}")
    private String entityPath;

    @Value("${event.entity.nested.path}")
    private String nestedEntityPath;

    /** Entity relative path. Use when the entity has been extracted from the event. */
    @Value("${entity.id.path}")
    private String entityIdPath;

    /** Entity relative path. Use when the entity has been extracted from the event. */
    @Value("${entity.resource.version.path}")
    private String entityResourceVersionPath;

    public String getEventDomainPath() {
        return eventDomainPath;
    }

    public void setEventDomainPath(String eventDomainPath) {
        this.eventDomainPath = eventDomainPath;
    }

    public String getEventActionPath() {
        return eventActionPath;
    }

    public void setEventActionPath(String eventActionPath) {
        this.eventActionPath = eventActionPath;
    }

    public String getEventTypePath() {
        return eventTypePath;
    }

    public void setEventTypePath(String eventTypePath) {
        this.eventTypePath = eventTypePath;
    }

    public String getTopEntityTypePath() {
        return topEntityTypePath;
    }

    public void setTopEntityTypePath(String topEntityTypePath) {
        this.topEntityTypePath = topEntityTypePath;
    }

    public String getEntityLinkPath() {
        return entityLinkPath;
    }

    public void setEntityLinkPath(String entityLinkPath) {
        this.entityLinkPath = entityLinkPath;
    }

    public String getEntityLinkDelimiter() {
        return entityLinkDelimiter;
    }

    public void setEntityLinkDelimiter(String entityLinkDelimiter) {
        this.entityLinkDelimiter = entityLinkDelimiter;
    }

    public String getEntityTypePath() {
        return entityTypePath;
    }

    public void setEntityTypePath(String entityTypePath) {
        this.entityTypePath = entityTypePath;
    }

    public String getEntityPath() {
        return entityPath;
    }

    public void setEntityPath(String entityPath) {
        this.entityPath = entityPath;
    }

    /**
     * Formats the nested entity path using the entity type provided.
     *
     * @param entityType
     *        an entity type
     * @return the formatted nested entity path
     */
    public String getNestedEntityPath(String entityType) {
        return formatter(nestedEntityPath, entityType);
    }

    public void setNestedEntityPath(String nestedEntityPath) {
        this.nestedEntityPath = nestedEntityPath;
    }

    /**
     * Formats the entity ID path using the entity type provided.
     *
     * @param entityType
     *        an entity type
     * @return the formatted entity ID path
     */
    public String getEntityIdPath(String entityType) {
        return formatter(entityIdPath, entityType);
    }

    public void setEntityIdPath(String entityIdPath) {
        this.entityIdPath = entityIdPath;
    }

    public String getEntityResourceVersionPath() {
        return entityResourceVersionPath;
    }

    public void setEntityResourceVersionPath(String entityResourceVersionPath) {
        this.entityResourceVersionPath = entityResourceVersionPath;
    }
}
