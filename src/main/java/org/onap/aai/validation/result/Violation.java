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
import com.google.gson.annotations.Expose;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.onap.aai.validation.exception.ValidationServiceError;
import org.onap.aai.validation.exception.ValidationServiceException;
import org.onap.aai.validation.reader.data.Entity;
import org.onap.aai.validation.reader.data.EntityId;
import org.onap.aai.validation.util.JsonUtil;

/**
 * A validation violation.
 */
public class Violation {

	public static final String ENTITY_TYPE_PROPERTY = "entityType";
	public static final String ENTITY_ID_PROPERTY = "entityId";
	public static final String ENTITY_MODELNAME_PROPERTY = "modelName";

	@Expose
	private final String violationId;

	@Expose
	private String modelName;

	@Expose
	private final String category;

	@Expose
	private final String severity;

	@Expose
	private final String violationType;

	/**
	 * The rule name (not applicable for model-driven) is not final as the value may be set to null via deserialisation
	 * and we will need to update the field.
	 */
	@Expose
	private Optional<String> validationRule;

	@Expose
	private final JsonElement violationDetails;

	@Expose
	private String errorMessage;

	/**
	 * rule-based or model-driven?
	 */
	public enum ViolationType {
		NONE, RULE, MODEL
	}

	/**
	 * Builder for a Violation.
	 */
	public static class Builder {

		private final MessageDigest messageDigest;
		private final JsonElement entityId;
		private final String entityType;
		private final String entityLink;
		private final String resourceVersion;
		private String category = null;
		private String severity = null;
		private String violationType = null;
		private Optional<String> validationRule = Optional.empty();
		private JsonElement violationDetails = new JsonObject();
		private String errorMessage = null;

		/**
		 * Create a Violation Builder for the supplied entity.
		 *
		 * @param entity
		 *            the entity
		 * @throws ValidationServiceException
		 *             the validation service exception
		 */
		public Builder(Entity entity) throws ValidationServiceException {
			this.entityId = new JsonObject();
			for (EntityId id : entity.getIds()) {
				this.entityId.getAsJsonObject().addProperty(id.getPrimaryKey(), id.getValue());
			}
			this.entityType = entity.getType();
			this.entityLink = entity.getEntityLink();
			this.resourceVersion = entity.getResourceVersion().orElse(null);
			try {
				messageDigest = MessageDigest.getInstance("SHA-256");
			} catch (NoSuchAlgorithmException e) {
				throw new ValidationServiceException(ValidationServiceError.MESSAGE_DIGEST_ERROR, e);
			}
		}

		/**
		 * Category.
		 *
		 * @param val
		 *            the val
		 * @return the builder
		 */
		public Builder category(String val) {
			category = val;
			return this;
		}

		/**
		 * Severity.
		 *
		 * @param val
		 *            the val
		 * @return the builder
		 */
		public Builder severity(String val) {
			severity = val;
			return this;
		}

		/**
		 * Violation type.
		 *
		 * @param val
		 *            the val
		 * @return the builder
		 */
		public Builder violationType(String val) {
			violationType = val;
			return this;
		}

		/**
		 * Violation type.
		 *
		 * @param type
		 *            the type
		 * @return the builder
		 */
		public Builder violationType(ViolationType type) {
			String name = type.name();
			// Convert to Camel Case
			return violationType(name.substring(0, 1).toUpperCase() + name.substring(1, name.length()).toLowerCase());
		}

		/**
		 * Validation rule.
		 *
		 * @param val
		 *            the val
		 * @return the builder
		 */
		public Builder validationRule(String val) {
			validationRule = Optional.ofNullable(val);
			return this;
		}

		/**
		 * Violation details.
		 *
		 * @param map
		 *            the map
		 * @return the builder
		 */
		public Builder violationDetails(Map<String, Object> map) {
			violationDetails = JsonUtil.toJsonElement(map);
			return this;
		}

		/**
		 * Error message.
		 *
		 * @param val
		 *            the val
		 * @return the builder
		 */
		public Builder errorMessage(String val) {
			errorMessage = val;
			return this;
		}

		/**
		 * Generate violation id.
		 *
		 * @return a deterministic identifier of the violation (from its details) which may be used to compare the
		 *         equality of violations which are created at different times
		 * @throws ValidationServiceException
		 *             the validation service exception
		 */
		public String generateViolationId() throws ValidationServiceException {
			try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
				StringBuilder result = new StringBuilder();
				writeObjectToStream(baos);
				messageDigest.reset(); // Not strictly needed as digest() will cause a reset
				for (byte byt : messageDigest.digest(baos.toByteArray())) {
					result.append(Integer.toString((byt & 0xff) + 0x100, 16).substring(1));
				}
				return result.toString();
			} catch (IOException e) {
				throw new ValidationServiceException(ValidationServiceError.JSON_READER_PARSE_ERROR, e);
			}
		}

		/**
		 * Builds the.
		 *
		 * @return a new Violation object
		 * @throws ValidationServiceException
		 *             the validation service exception
		 */
		public Violation build() throws ValidationServiceException {
			return new Violation(this);
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "Builder [entityId=" + entityId + ", entityType=" + entityType + ", entityLink=" + entityLink + ", resourceVersion=" + resourceVersion
					+ ", category=" + category + ", severity=" + severity + ", violationType=" + violationType + ", validationRule=" + validationRule
					+ ", violationDetails=" + violationDetails + ", errorMessage=" + errorMessage + "]";
		}

		/**
		 * Stream all fields that are required for generating a deterministic violation ID field. Note that
		 * resourceVersion is not included as this is allowed to vary
		 *
		 * @param baos
		 *            the byte array output stream
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private void writeObjectToStream(ByteArrayOutputStream baos) throws IOException {
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(this.category);
			oos.writeObject(this.entityId.toString());
			oos.writeObject(this.entityType);
			oos.writeObject(this.entityLink);
			oos.writeObject(this.severity);
			oos.writeObject(this.validationRule.toString());
			oos.writeObject(this.violationDetails.toString());
			oos.writeObject(this.violationType);
			oos.close();
		}

	}

	/**
	 * Instantiates a new Violation object via a builder.
	 *
	 * @param builder
	 *            the builder storing the Violation values
	 * @throws ValidationServiceException
	 *             the validation service exception
	 */
	private Violation(Builder builder) throws ValidationServiceException {
		violationId = builder.generateViolationId();
		category = builder.category;
		severity = builder.severity;
		violationType = builder.violationType;
		validationRule = builder.validationRule;
		// Clone by serialising and deserialising!
		violationDetails = JsonUtil.fromJson(JsonUtil.toJson(builder.violationDetails), JsonElement.class);
		errorMessage = builder.errorMessage;
	}

	/**
	 * Gets the violation id.
	 *
	 * @return the violation id
	 */
	public String getViolationId() {
		return violationId;
	}

	/**
	 * Gets the model name.
	 *
	 * @return the model name
	 */
	public String getModelName() {
		return modelName;
	}

	/**
	 * Sets the model name.
	 *
	 * @param modelName
	 *            the new model name
	 */
	// Naughty and breaks the builder pattern but it saves a lot of faffing about.
	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

	/**
	 * Gets the category.
	 *
	 * @return the category
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * Gets the severity.
	 *
	 * @return the severity
	 */
	public String getSeverity() {
		return severity;
	}

	/**
	 * Gets the violation type.
	 *
	 * @return the violation type
	 */
	public String getViolationType() {
		return violationType;
	}

	/**
	 * Gets the violation details.
	 *
	 * @return the violation details
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> getViolationDetails() {
		return JsonUtil.toAnnotatedClassfromJson(violationDetails.getAsJsonObject().toString(), Map.class);
	}

	/**
	 * Gets the error message.
	 *
	 * @return the error message
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.modelName, this.category, this.errorMessage, this.severity, this.validationRule, this.violationDetails, this.violationId, this.violationType);
	}

	/*
	 * A hand-crafted equivalence relation to compare two Violation objects. Note that the violationId is compared
	 * first, because this value is deterministically generated from the majority of the object's fields.
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
        if (!(obj instanceof Violation)) {
            return false;
     } else if (obj == this) {
            return true;
     }
        Violation rhs = (Violation) obj;
     // @formatter:off
     return new EqualsBuilder()
                  .append(modelName, rhs.modelName)
                  .append(category, rhs.category)
                  .append(errorMessage, rhs.errorMessage)
                  .append(severity, rhs.severity)
                  .append(validationRule, rhs.validationRule)
                  .append(violationDetails, rhs.violationDetails)
                  .append(violationId, rhs.violationId)
                  .append(violationType, rhs.violationType)
                  .isEquals();
     // @formatter:on
	}

	@Override
	public String toString() {
		return JsonUtil.toJson(this);
	}

	/**
	 * Ensure that any unset fields are properly initialised. This is particularly useful when the object has been
	 * deserialised from a JSON string, as any missing/undefined values will not be read by the deserialiser and thus
	 * the corresponding fields will not be set.
	 */
	public void initialiseValues() {
		if (validationRule == null) { // NOSONAR
			validationRule = Optional.empty();
		}
	}
}
