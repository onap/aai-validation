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

import java.util.List;
import java.util.Optional;
import org.onap.aai.validation.exception.ValidationServiceException;
import org.onap.aai.validation.reader.data.EntityId;

/**
 * Interface for extracting values from an entity (in JSON format).
 *
 */
public interface EntityReader {

	/**
	 * Return the value found at the supplied path.
	 *
	 * @param json
	 *            the JSON representation of the entity
	 * @param path
	 *            specifier of the path to the value within the JSON entity
	 * @return either a primitive object (e.g. String, Integer) or a JSON element
	 * @throws ValidationServiceException
	 */
	Object getObject(String json, String path) throws ValidationServiceException;

	/**
	 * @param json
	 *            the JSON representation of the entity
	 * @param type
	 *            the type of the entity
	 * @return the key value(s) identifying the entity
	 * @throws ValidationServiceException
	 */
	List<EntityId> getIds(String json, String type) throws ValidationServiceException;

	/**
	 * @param json
	 *            the JSON representation of the entity
	 * @return the resource version of the entity (if present)
	 * @throws ValidationServiceException
	 */
	Optional<String> getResourceVersion(String json) throws ValidationServiceException;

}
