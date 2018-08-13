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

import com.google.gson.JsonElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.onap.aai.validation.exception.ValidationServiceError;
import org.onap.aai.validation.exception.ValidationServiceException;
import org.onap.aai.validation.modeldriven.validator.InstanceReader;
import org.onap.aai.validation.modeldriven.validator.InstanceReader.InstanceEntity;
import org.onap.aai.validation.reader.data.EntityId;

/**
 * Entity reader implemented using the model-driven instance reader.
 *
 */
public class InstanceEntityReader implements EntityReader {

	private InstanceReader reader;

	/**
	 * @param instanceReader
	 */
	public InstanceEntityReader(InstanceReader instanceReader) {
		this.reader = instanceReader;
	}

	@Override
	public Object getObject(String json, String attribute) throws ValidationServiceException {
		throw new ValidationServiceException(ValidationServiceError.INSTANCE_READER_NO_INSTANCE, "Not implemented");
	}

	@Override
	public List<EntityId> getIds(String json, String type) throws ValidationServiceException {
		List<EntityId> ids = new ArrayList<>();

		InstanceEntity entity = reader.getNamedQueryEntity(json);

		List<String> primaryKeys = reader.getOxmReader().getPrimaryKeys(entity.getEntityType());

		if (primaryKeys != null && !primaryKeys.isEmpty()) {
			for (String pk : primaryKeys) {
				JsonElement jsonValue = entity.getObject().getAsJsonObject().get(pk);
				if (jsonValue != null) {
					ids.add(new EntityId(pk, jsonValue.getAsString()));
				}
			}
		}

		return ids;
	}

	@Override
	public Optional<String> getResourceVersion(String json) throws ValidationServiceException {
		return Optional.of(reader.getResourceVersion(json));
	}

}