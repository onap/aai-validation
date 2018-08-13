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
package org.onap.aai.validation.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.util.Map;

/**
 * Wrapper to hide the actual JSON API.
 */
public class JsonUtil {

	// General purpose. Excludes null values.
	private static final Gson gsonDefault = new Gson();

	// Only serialises fields with the Expose annotation
	private static final Gson gsonForAnnotatedClasses = GsonUtil.createGson();

	/**
	 * Purely a static class.
	 */
	private JsonUtil() {
		// Deliberately empty
	}

	/**
	 * Serialise the annotated object to a JSON string.
	 *
	 * @param obj
	 *            the object to serialise
	 * @return the JSON representation of the object
	 */
	public static String toJson(Object obj) {
		return gsonForAnnotatedClasses.toJson(obj);
	}

	/**
	 * Deserialise the annotated object from a JSON string.
	 *
	 * @param <T>
	 *            the type of the object
	 * @param json
	 *            the JSON string
	 * @param classOfT
	 *            the class of type T
	 * @return a new object instance
	 */
	public static <T> T toAnnotatedClassfromJson(String json, Class<T> classOfT) {
		return gsonForAnnotatedClasses.fromJson(json, classOfT);
	}

	/**
	 * Deserialise the object from a JSON string.
	 *
	 * @param <T>
	 *            the type of the object
	 * @param json
	 *            the JSON string
	 * @param classOfT
	 *            the class of type T
	 * @return a new object instance
	 */
	public static <T> T fromJson(String json, Class<T> classOfT) {
		return gsonDefault.fromJson(json, classOfT);
	}

	/**
	 * To json element.
	 *
	 * @param map
	 *            the map
	 * @return the json element
	 */
	public static JsonElement toJsonElement(Map<String, Object> map) {
		return gsonForAnnotatedClasses.toJsonTree(map).getAsJsonObject();
	}

}
