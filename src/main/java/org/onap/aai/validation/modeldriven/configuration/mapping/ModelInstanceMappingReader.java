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
package org.onap.aai.validation.modeldriven.configuration.mapping;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.onap.aai.validation.exception.ValidationServiceError;
import org.onap.aai.validation.exception.ValidationServiceException;
import org.onap.aai.validation.util.JsonUtil;

/**
 * Read mapping files
 */
public class ModelInstanceMappingReader {

	private String mappingFile;

	/**
	 * @param mappingFile
	 */
	public ModelInstanceMappingReader(String mappingFile) {
		this.mappingFile = mappingFile;
	}

	/**
	 * Returns a list of model and object instance paths that will be used for comparing the corresponding model and
	 * instance elements. The mappings are defined in the configuration file model-instance-mapping.json_conf and follows the
	 * JSON notation.
	 *
	 * @return a List of {@link ModelInstanceMapper} beans which represents all the mappings defined in the file
	 *         model-instance-mapping.json_conf.
	 * @throws ValidationServiceException
	 */
	public List<ModelInstanceMapper> getMappings() throws ValidationServiceException {
		List<ModelInstanceMapper> mappings = new ArrayList<>();

		try {
			JSONArray mappingsArray = new JSONArray(mappingFile);

			for (int i = 0; i < mappingsArray.length(); i++) {
				JSONObject jsonObject = mappingsArray.getJSONObject(i);
				ModelInstanceMapper mapping = JsonUtil.fromJson(jsonObject.toString(), ModelInstanceMapper.class);
				mappings.add(mapping);
			}
		} catch (JSONException e) {
			throw new ValidationServiceException(ValidationServiceError.MODEL_INSTANCE_MAPPING_RETRIEVAL_ERROR, e);
		}

		return mappings;
	}
}