/**
 * ============LICENSE_START===================================================
 * Copyright (c) 2018-2019 European Software Marketing Ltd.
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
package org.onap.aai.validation.result;

import com.google.gson.JsonElement;
import java.util.List;

public interface ValidationResult {

    List<Violation> getViolations();

    JsonElement getEntityId();

    String getEntityLink();

    String getEntityType();

    String getResourceVersion();

    /**
     * Add a single validation violation.
     *
     * @param violation a single {@link Violation} to add to the validation result
     */
    void addViolation(Violation violation);

    /**
     * Add a list of validation violations.
     *
     * @param violations a List of {@link Violation} objects to add to the validation result
     */
    void addViolations(List<Violation> violations);

    /**
     * Create a JSON representation of the object, with each violation's validationRule omitted when it has a null value
     *
     * @return this object formatted as a JSON string ready for publishing
     */
    String toJson();

    String getValidationId();

    String getValidationTimestamp();

    void setEntityId(JsonElement entityId);

    void setEntityType(String entityType);

}
