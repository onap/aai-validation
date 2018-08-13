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
package org.onap.aai.validation.reader.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.onap.aai.validation.exception.ValidationServiceException;
import org.onap.aai.validation.reader.EntityReader;

/**
 * An A&AI entity.
 */
public class Entity {

	private String json;
	private String type;
	private EntityReader reader;
	private List<EntityId> ids = new ArrayList<>();
	private Optional<String> resourceVersion = Optional.empty();
	private String entityLink;

	/**
	 *
	 * @param json
	 *            the full entity JSON supplied in the event
	 * @param entityType
	 *            the entity type
	 * @param entityLink
	 * @param entityReader
	 *            an {@link EntityReader}
	 */
	public Entity(final String json, final String entityType, final String entityLink, final EntityReader entityReader) {
		this.json = json;
		this.type = entityType;
		this.entityLink = entityLink;
		this.reader = entityReader;
	}

	/**
	 * Get the full entity JSON.
	 *
	 * @return the entity JSON
	 */
	public String getJson() {
		return json;
	}

	/**
	 * Get the entity type.
	 *
	 * @return the entity type
	 */
	public String getType() {
		return type;
	}

	/**
	 * Get the entity identifiers. More than one identifier may be provided for composite keys.
	 *
	 * @return a list of entity identifiers
	 * @throws ValidationServiceException
	 */
	public List<EntityId> getIds() throws ValidationServiceException {
		if (ids.isEmpty()) {
			ids = reader.getIds(getJson(), getType());
		}
		return ids;
	}

	/**
	 * Get the resource version.
	 *
	 * @return the resource version
	 * @throws ValidationServiceException
	 */
	public Optional<String> getResourceVersion() throws ValidationServiceException {
		if (!resourceVersion.isPresent()) {
			resourceVersion = reader.getResourceVersion(getJson());
		}
		return resourceVersion;
	}

	/**
	 * Get the event entity link.
	 *
	 * @return the event entity link
	 */
	public String getEntityLink() {
		return entityLink;
	}

	/**
	 * Get collection of attributes
	 *
	 * @param attributes
	 *            the names of the attributes
	 * @return an {@link AttributeValues} object containing the attribute values
	 * @throws ValidationServiceException
	 */
	public AttributeValues getAttributeValues(List<String> attributes) throws ValidationServiceException {
		AttributeValues attributeValues = new AttributeValues();

		if (attributes == null || attributes.isEmpty()) {
			return attributeValues;
		}

		for (String attribute : attributes) {
			attributeValues.put(attribute, reader.getObject(getJson(), attribute));
		}

		return attributeValues;
	}

	@Override
	public String toString() {
		return json;
	}

}