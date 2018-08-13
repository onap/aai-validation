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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stores a collection of attribute values (retrievable by name).
 */
public class AttributeValues {

	private Map<String, Object> map;

	/**
	 * Instantiates a new empty set of attribute values.
	 */
	public AttributeValues() {
		this.map = new HashMap<>();
	}

	/**
	 * Instantiates a new set of attribute values using the provided properties map.
	 *
	 * @param map
	 *            the attribute name/value pairs
	 */
	public AttributeValues(Map<String, Object> map) {
		this.map = map;
	}

	/**
	 * Instantiates a new set of attribute values comprising one name/value pair.
	 *
	 * @param key
	 *            the attribute name
	 * @param value
	 *            the attribute value
	 */
	public AttributeValues(String key, String value) {
		this();
		this.map.put(key, value);
	}

	@Override
	public String toString() {
		return map.toString();
	}

	/**
	 *
	 * @return the number of attributes stored
	 */
	public int size() {
		return this.map.size();
	}

	/**
	 * Add an attribute name/value pair.
	 *
	 * @param key
	 *            the attribute name
	 * @param value
	 *            the attribute value
	 */
	public void put(String key, Object value) {
		this.map.put(key, value);
	}

	/**
	 * Add an attribute name with a collection of values.
	 *
	 * @param key
	 *            the attribute name
	 * @param valueList
	 *            the collection of attribute values
	 */
	public void put(String key, List<String> valueList) {
		this.map.put(key, valueList);
	}

	/**
	 * Removes the attribute
	 *
	 * @param key
	 *            the attribute name
	 */
	public void remove(String key) {
		this.map.remove(key);

	}

	/**
	 * Overwrites the attribute values, replacing them with the specified attribute to attribute values mappings
	 *
	 * @param map
	 *            the name/values to store
	 */
	public void setMap(Map<String, Object> map) {
		this.map = map;
	}

	/**
	 * Gets an attribute value
	 *
	 * @param attrName
	 *            the attribute name
	 * @return the object
	 */
	public Object get(String attrName) {
		return this.map.get(attrName);
	}

	/**
	 * Converts the attribute values into a report-friendly format
	 *
	 * @return a Map which is a copy of the AttributeValues, formatted for reporting
	 */
	public Map<String, Object> generateReport() {
		return map;
	}

}
