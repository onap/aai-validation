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
package org.onap.aai.validation.modeldriven.validator;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jayway.jsonpath.DocumentContext;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import javax.inject.Inject;
import org.onap.aai.validation.exception.ValidationServiceError;
import org.onap.aai.validation.exception.ValidationServiceException;
import org.onap.aai.validation.modeldriven.configuration.mapping.ModelInstanceMapper;
import org.onap.aai.validation.modeldriven.configuration.mapping.ModelInstanceMapper.MappingType;
import org.onap.aai.validation.reader.JsonReader;
import org.onap.aai.validation.reader.OxmReader;

/**
 * Reads values from an instance object.
 */
public class InstanceReader {

	private static final String MODEL_NAME = "model-name";
	private static final String[] INVALID_ENTRIES = { "inventory-response-items", "extra-properties", MODEL_NAME };
	private static final String RESOURCE_VERSION = "resource-version";
	private static final String JSON_PATH_MODEL_ID = "$.*.persona-model-id";

	private JsonReader jsonReader;
	private OxmReader oxmReader;
	private JsonParser jsonParser = new JsonParser();

	/**
	 * @param jsonReader
	 * @param oxmReader
	 */
	@Inject
	public InstanceReader(JsonReader jsonReader, OxmReader oxmReader) {
		this.jsonReader = jsonReader;
		this.oxmReader = oxmReader;
	}

	public OxmReader getOxmReader() {
		return oxmReader;
	}

	/**
	 * Gets object instance values.
	 *
	 * @param json
	 *            a Named Query JSON payload
	 * @param mapping
	 *            defines the paths that allow the extraction of values from the object instance. This includes:
	 *            <ul>
	 *            <li>origin: path that serves as the starting point for the instance search</li>
	 *            <li>root: path to underlying instance objects that can be examined by recursively calling the
	 *            getValues method</li>
	 *            </ul>
	 *
	 * @return a {@link Multimap} of instances keyed by their model id.
	 * @throws ValidationServiceException
	 */
	public Multimap<String, String> getValues(String json, ModelInstanceMapper mapping) throws ValidationServiceException {
		Multimap<String, String> values = HashMultimap.create();

		DocumentContext document = jsonReader.parse(json);

		if (MappingType.RELATIONSHIP.equals(mapping.getMappingType())) {
			String rootPath = mapping.getInstance().getRoot();
			if (rootPath == null || rootPath.isEmpty()) {
				throw new ValidationServiceException(ValidationServiceError.INSTANCE_MAPPING_ROOT_ERROR);
			}

			JsonElement jsonElement = jsonReader.getJsonElement(document, rootPath);

			if (jsonElement instanceof JsonArray) {
				JsonArray jsonArray = jsonElement.getAsJsonArray();

				processRelatedObjects(values, jsonArray);
			}
		} else {
			// We are dealing with attributes.
			String valuePath = mapping.getInstance().getValue();
			if (valuePath != null && !valuePath.isEmpty()) {
				List<String> attributes = jsonReader.get(json, valuePath);
				for (String attribute : attributes) {
					values.put(attribute, null);
				}
			}
		}

		return values;
	}

	/**
	 * Gets the instance type, e.g. connector, pserver, etc.
	 *
	 * @param json
	 *            a Named Query JSON payload
	 * @return the type of the entity
	 */
	public String getInstanceType(String json) {
		return getNamedQueryEntity(json).getEntityType();
	}

	/**
	 * Gets the id of the instance. Uses the {@link OxmReader} to identify the property holding the primary key.<br>
	 *
	 * WARNING: Some types of object appear to have more than one primary key. This method uses the first primary key.
	 *
	 * @param json
	 *            a Named Query JSON payload
	 * @return the identifier of the object instance
	 * @throws ValidationServiceException
	 */
	public String getInstanceId(String json) throws ValidationServiceException {
		String instanceId = null;

		InstanceEntity entity = getNamedQueryEntity(json);

		List<String> primaryKeys = oxmReader.getPrimaryKeys(entity.getEntityType());

		if (primaryKeys != null && !primaryKeys.isEmpty()) {
			JsonObject instance = entity.getObject().getAsJsonObject();
			JsonElement primaryKey = instance.get(primaryKeys.get(0));
			instanceId = primaryKey == null ? null : primaryKey.getAsString();
		}

		return instanceId;
	}

	/**
	 * Strips the instance out of its payload wrapping.
	 *
	 * @param json
	 *            a Named Query JSON payload
	 * @param mappings
	 *            the definition of the paths that allow the extraction of the instance from the JSON payload
	 * @return
	 * @throws ValidationServiceException
	 */
	public String getInstance(String json, List<ModelInstanceMapper> mappings) throws ValidationServiceException {
		String origin = mappings.iterator().next().getInstance().getOrigin();
		List<String> jsonList = jsonReader.get(json, origin);

		if (!jsonList.isEmpty()) {
			return jsonList.get(0);
		} else {
			throw new ValidationServiceException(ValidationServiceError.INSTANCE_READER_NO_INSTANCE, origin, json);
		}
	}

	/**
	 * Extracts the entity from a Named Query JSON payload.
	 *
	 * @param json
	 *            a Named Query JSON payload
	 * @return an {@link InstanceEntity} object
	 */
	public InstanceEntity getNamedQueryEntity(String json) {
		return getNamedQueryEntity(jsonParser.parse(json).getAsJsonObject());
	}

	/**
	 * Gets the model identifier of a given entity.
	 *
	 * @param entity
	 *            a JSON entity
	 * @return a model identifier attribute value if the attribute exists else a null is returned.
	 * @throws ValidationServiceException
	 */
	public String getModelId(String entity) throws ValidationServiceException {
		String modelId = null;
		List<String> readResult = jsonReader.get(entity, JSON_PATH_MODEL_ID);
		if (!readResult.isEmpty()) {
			modelId = readResult.get(0);
		}
		return modelId;
	}

	/**
	 * Gets the resource version of the instance.
	 *
	 * @param json
	 *            a Named Query JSON payload
	 * @return the resource version of the object instance
	 */
	public String getResourceVersion(String json) {
		String resourceVersion = null;

		InstanceEntity entity = getNamedQueryEntity(json);

		if (entity != null && entity.getObject() != null && entity.getObject().getAsJsonObject().has(RESOURCE_VERSION)) {
			resourceVersion = entity.getObject().getAsJsonObject().get(RESOURCE_VERSION).getAsString();
		}
		return resourceVersion;
	}

	/**
	 * Gets the model name of the instance.
	 *
	 * @param jsonString
	 *            a Named Query JSON payload
	 * @return the model name of the object instance
	 * @throws ValidationServiceException
	 */
	public String getModelName(String jsonString) {
		JsonObject jsonObject = jsonParser.parse(jsonString).getAsJsonObject();
		return getModelName(jsonObject);
	}

	/**
	 * @param jsonObject
	 * @return
	 */
	private String getModelName(JsonObject jsonObject) {
		for (Entry<String, JsonElement> entry : jsonObject.entrySet()) {
			if (MODEL_NAME.equals(entry.getKey())) {
				return entry.getValue().getAsString();
			}
		}
		return null;
	}

	private void processRelatedObjects(Multimap<String, String> values, JsonArray jsonArray) {
		for (JsonElement relatedObject : jsonArray) {
			JsonObject jsonObject = relatedObject.getAsJsonObject();

			InstanceEntity entity = getNamedQueryEntity(jsonObject);
			if (entity != null) {
				values.put(entity.getModelName() == null ? entity.getEntityType() : entity.getModelName(), jsonObject.toString());
			}
		}
	}

	private InstanceEntity getNamedQueryEntity(JsonObject jsonObject) {
		Set<Entry<String, JsonElement>> entrySet = jsonObject.entrySet();

		String modelName = getModelName(jsonObject);

		for (Entry<String, JsonElement> entry : entrySet) {
			if (!Arrays.asList(INVALID_ENTRIES).contains(entry.getKey())) {
				return new InstanceEntity(entry.getKey(), modelName, entry.getValue().getAsJsonObject(), jsonObject);
			}
		}

		return null;
	}

	/**
	 * An Entity bean for the InstanceReader
	 *
	 */
	public class InstanceEntity {

		private String entityType;
		private String modelName;
		private JsonObject object;
		private JsonObject objectAndGraph;

		/**
		 * @param entityType
		 * @param modelName
		 * @param object
		 * @param objectAndGraph
		 */
		public InstanceEntity(String entityType, String modelName, JsonObject object, JsonObject objectAndGraph) {
			this.entityType = entityType;
			this.modelName = modelName;
			this.object = object;
			this.objectAndGraph = objectAndGraph;
		}

		public String getEntityType() {
			return entityType;
		}

		public String getModelName() {
			return modelName;
		}

		public JsonObject getObject() {
			return object;
		}

		public JsonObject getObjectAndGraph() {
			return objectAndGraph;
		}

		@Override
		public String toString() {
			return "Entity [entityType=" + entityType + ", modelName=" + modelName + ", object=" + object.toString() + ", fullObject="
					+ objectAndGraph.toString() + "]";
		}
	}
}