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

import java.util.List;

/**
 * Loads the properties needed by the controller using spring.
 */
public class RuleIndexingConfig extends PropertiesConfig {

    private List<String> indexedEvents;

    private List<String> excludedOxmValidationEvents;

    private List<String> indexAttributes;

    private String defaultIndexKey;

    public List<String> getIndexedEvents() {
        return indexedEvents;
    }

    public void setIndexedEvents(List<String> indexedEvents) {
        this.indexedEvents = indexedEvents;
    }

    public List<String> getExcludedOxmValidationEvents() {
        return excludedOxmValidationEvents;
    }

    public void setExcludedOxmValidationEvents(List<String> excludedOxmValidationEvents) {
        this.excludedOxmValidationEvents = excludedOxmValidationEvents;
    }

    public List<String> getIndexAttributes() {
        return indexAttributes;
    }

    public void setIndexAttributes(List<String> indexAttributes) {
        this.indexAttributes = indexAttributes;
    }

    public String getDefaultIndexKey() {
        return defaultIndexKey;
    }

    public void setDefaultIndexKey(String defaultIndexKey) {
        this.defaultIndexKey = defaultIndexKey;
    }

    public boolean skipOxmValidation(String event) {
        if (excludedOxmValidationEvents == null) {
            return false;
        }
        return excludedOxmValidationEvents.contains(event);
    }
}
