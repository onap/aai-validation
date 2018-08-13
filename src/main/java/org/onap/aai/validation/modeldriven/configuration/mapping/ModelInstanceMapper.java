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

import java.util.Objects;
import org.apache.commons.lang3.builder.EqualsBuilder;

/**
 * Maps the configuration of model and instance values to be compared.
 */
public class ModelInstanceMapper {

	/**
	 * Types of mappings.
	 */
	public enum MappingType {
		RELATIONSHIP, ATTRIBUTE
	}
	
	private MappingType mappingType;
	private ValueConfiguration model;
	private ValueConfiguration instance;

	public MappingType getMappingType() {
		return mappingType;
	}

	public void setMappingType(String mappingType) {
		this.mappingType = MappingType.valueOf(mappingType);
	}

	public ValueConfiguration getModel() {
		return model;
	}

	public void setModel(ValueConfiguration model) {
		this.model = model;
	}

	public ValueConfiguration getInstance() {
		return instance;
	}

	public void setInstance(ValueConfiguration instance) {
		this.instance = instance;
	}

	@Override
	public int hashCode() {
		return Objects.hash(instance, mappingType, model);
	}

	@Override
	public boolean equals(Object obj) {
        if (!(obj instanceof ModelInstanceMapper)) {
            return false;
     } else if (obj == this) {
            return true;
     }
        ModelInstanceMapper rhs = (ModelInstanceMapper) obj;
     // @formatter:off
     return new EqualsBuilder()
                  .append(instance, rhs.instance)
                  .append(mappingType, rhs.mappingType)
                  .append(model, rhs.model)
                  .isEquals();
     // @formatter:on
	}

	@Override
	public String toString() {
		return "ModelInstanceMapper [mappingType=" + mappingType + ", model=" + model + ", instance=" + instance + "]";
	}
}