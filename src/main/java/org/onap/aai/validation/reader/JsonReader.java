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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.ReadContext;
import com.jayway.jsonpath.spi.json.GsonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.GsonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.onap.aai.validation.exception.ValidationServiceError;
import org.onap.aai.validation.exception.ValidationServiceException;

/**
 * Reads JSON objects. Supported by the JayWay JsonPath library.
 */
public class JsonReader {

	private Configuration jsonPathConfig;

	/**
	 * Initialise the JSON reader.
	 */
	public JsonReader() {
		setJsonProvider();
		this.jsonPathConfig = Configuration.builder().options(Option.SUPPRESS_EXCEPTIONS).build();
	}

	/**
	 * Parse the JSON.
	 *
	 * @param json
	 *            the JSON object
	 * @return a {@link ReadContext} the parsed JSON.
	 * @throws ValidationServiceException
	 */
	public DocumentContext parse(String json) throws ValidationServiceException {
		DocumentContext document = null;
		try {
			document = JsonPath.using(jsonPathConfig).parse(json);
		} catch (Exception e) {
			throw new ValidationServiceException(ValidationServiceError.JSON_READER_PARSE_ERROR, e);
		}
		return document;
	}

	/**
	 * Gets values from JSON objects.
	 *
	 * @param json
	 *            the JSON object
	 * @param path
	 *            the path to property values. The format must comply with the JayWay JsonPath definition.
	 * @return a List of values found by evaluating the path.
	 * @throws ValidationServiceException
	 */
	public List<String> get(String json, String path) throws ValidationServiceException {
		return getAsList(parse(json), path);
	}

	/**
	 * Gets values from JSON objects. Used in combination with {@link JsonReader#parse(String)} it reduces the number of
	 * times the JSON document is parsed.
	 *
	 * @param document
	 *            a {@link DocumentContext} object with the parsed JSON
	 * @param path
	 *            the path to property values. The format must comply with the JayWay JsonPath definition.
	 * @return a List of values found by evaluating the path, or an empty list if no values were found
	 */
	public List<String> getAsList(DocumentContext document, String path) {
		List<String> result = new ArrayList<>();
		JsonElement jsonElement = document.read(path);
		if (jsonElement != null) {
			if (jsonElement.isJsonPrimitive()) {
				result.add(jsonElement.getAsString());
			} else if (jsonElement.isJsonObject()) {
				result.add(jsonElement.getAsJsonObject().toString());
			} else if (jsonElement.isJsonArray()) {
				for (JsonElement obj : jsonElement.getAsJsonArray()) {
					Object object = jsonElementToObject(obj);
					result.add(object == null ? null : object.toString());
				}
			}
		}
		return result;
	}

	/**
	 * Get the value(s) from the specified JSON document
	 *
	 * @param document
	 *            a {@link DocumentContext} object with the parsed JSON
	 * @param path
	 *            the path to property value(s). The format must comply with the JayWay JsonPath definition.
	 * @return either all the values found by evaluating the path (e.g. as an array), or a String object (only) where
	 *         the path evaluates to a single primitive value
	 */
	public Object getObject(DocumentContext document, String path) {
		return jsonElementToObject(document.read(path));
	}

	/**
	 * Convert the JSON element to a String or Array where possible, otherwise return the JSON object.
	 *
	 * @param jsonElement
	 * @return the jsonElement converted to a Java Object
	 */
	private Object jsonElementToObject(JsonElement jsonElement) {
		if (jsonElement == null) {
			return null;
		} else if (jsonElement.isJsonPrimitive()) {
			return jsonElement.getAsString();
		} else if (jsonElement.isJsonObject()) {
			return jsonElement.getAsJsonObject();
		} else if (jsonElement.isJsonArray()) {
			// Convert to a List for simplified handling within rules
			return jsonArrayToList(jsonElement.getAsJsonArray());
		} else {
			return jsonElement;
		}
	}

	private List<Object> jsonArrayToList(JsonArray jsonArray) {
		List<Object> result = new ArrayList<>();
		for (JsonElement obj : jsonArray) {
			result.add(jsonElementToObject(obj));
		}
		return result;
	}

	/**
	 * @param document
	 * @param path
	 * @return a JsonElement from the document
	 */
	public JsonElement getJsonElement(DocumentContext document, String path) {
		return document.read(path);
	}

	private void setJsonProvider() {
		Configuration.setDefaults(new Configuration.Defaults() {
			private final JsonProvider jsonProvider = new GsonJsonProvider();
			private final MappingProvider mappingProvider = new GsonMappingProvider();

			@Override
			public JsonProvider jsonProvider() {
				return jsonProvider;
			}

			@Override
			public MappingProvider mappingProvider() {
				return mappingProvider;
			}

			@Override
			public Set<Option> options() {
				return EnumSet.noneOf(Option.class);
			}
		});
	}
}
