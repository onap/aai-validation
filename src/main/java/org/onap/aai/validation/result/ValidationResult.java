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
package org.onap.aai.validation.result;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.Expose;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.onap.aai.validation.exception.ValidationServiceException;
import org.onap.aai.validation.reader.data.Entity;
import org.onap.aai.validation.reader.data.EntityId;
import org.onap.aai.validation.util.JsonUtil;

/**
 * The result of an instance validation. This can include zero or more {@link Violation} objects.
 */
public class ValidationResult {

    @Expose
    private String validationId;

    @Expose
    private String validationTimestamp;

    @Expose
    private JsonElement entityId;

    @Expose
    private String entityType;

    @Expose
    private String entityLink;

    @Expose
    private String resourceVersion;
    
    @Expose
    private JsonElement entity;


	@Expose
    private List<Violation> violations = new ArrayList<>();

    /**
     * Create the validation payload initialised with an event identifier and a timestamp.
     *
     * @param entity
     * @throws ValidationServiceException
     */
    public ValidationResult(Entity entity) throws ValidationServiceException {
        this.validationId = UUID.randomUUID().toString();
        this.validationTimestamp =
                DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmssX").withZone(ZoneOffset.UTC).format(Instant.now());
        this.entityId = new JsonObject();
        for (EntityId id : entity.getIds()) {
            this.entityId.getAsJsonObject().addProperty(id.getPrimaryKey(), id.getValue());
        }
        this.entityType = entity.getType();
        this.entityLink = entity.getEntityLink();
        this.resourceVersion = entity.getResourceVersion().orElse(null);   
        this.entity = entity.getJson()!=null ?new JsonParser().parse(entity.getJson()): new JsonObject();
    }    
 

    /**
     * Add a validation violation.
     *
     * @param violation a single {@link Violation} to add to the validation result
     */
    public void addViolation(Violation violation) {
        this.violations.add(violation);
    }

    /**
     * Add a list of validation violations.
     *
     * @param violations a List of {@link Violation} objects to add to the validation result
     */
    public void addViolations(List<Violation> violations) {
        this.violations.addAll(violations);
    }

    public String getValidationId() {
        return validationId;
    }

    public void setValidationId(String eventId) {
        this.validationId = eventId;
    }

    public String getValidationTimestamp() {
        return validationTimestamp;
    }

    public void setValidationTimestamp(String timestamp) {
        this.validationTimestamp = timestamp;
    }

    public JsonElement getEntityId() {
        return entityId;
    }

    public void setEntityId(JsonElement entityId) {
        this.entityId = entityId;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getEntityLink() {
        return entityLink;
    }

    public void setEntityLink(String uri) {
        this.entityLink = uri;
    }

    public String getResourceVersion() {
        return resourceVersion;
    }

    public void setResourceVersion(String resourceVersion) {
        this.resourceVersion = resourceVersion;
    }

    public JsonElement getEntity() {
		return entity;
	}

	public void setEntity(JsonElement entity) {
		this.entity = entity;
	}
	
    public List<Violation> getViolations() {
        return violations;
    }

    public void setViolations(List<Violation> violations) {
        this.violations = violations;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.entityId, this.entityLink, this.entityType, this.resourceVersion, this.validationId,
                this.validationTimestamp, this.violations, this.entity);
    }

    /*
     * validationId is checked first, as this is meant to be unique
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ValidationResult)) {
            return false;
        } else if (obj == this) {
            return true;
        }
        ValidationResult rhs = (ValidationResult) obj;
     // @formatter:off
     return new EqualsBuilder()
                  .append(entityId, rhs.entityId)
                  .append(entityLink, rhs.entityLink)
                  .append(entityType, rhs.entityType)
                  .append(resourceVersion, rhs.resourceVersion)
                  .append(validationId, rhs.validationId)
                  .append(validationTimestamp, rhs.validationTimestamp)
                  .append(violations, rhs.violations)
                  .append(entity, rhs.entity)
                  .isEquals();
     // @formatter:on
    }

    @Override
    public String toString() {
        return JsonUtil.toJson(this);
    }

    /**
     * Create a JSON representation of the object, with each violation's validationRule omitted when it has a null value
     *
     * @return this object formatted as a JSON string ready for publishing
     */
    public String toJson() {
        return toString();
    }

    /**
     * Create a new object from the JSON representation
     *
     * @param json representation of the Validation Result
     * @return a ValidationResult object
     */
    public static ValidationResult fromJson(String json) {
        ValidationResult validationResult = JsonUtil.toAnnotatedClassfromJson(json, ValidationResult.class);
        if (validationResult != null) {
            validationResult.initialiseValues();
        }
        return validationResult;
    }

    /**
     * Ensure that any unset fields are properly initialised. This is particularly useful when the object has been
     * deserialised from a JSON string, as any missing/undefined values will not be read by the deserialiser and thus
     * the corresponding fields will not be set.
     */
    private void initialiseValues() {
        List<Violation> violationList = getViolations();
        if (violationList != null) {
            for (Violation violation : violationList) {
                violation.initialiseValues();
            }
        }
    }
}
