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
package org.onap.aai.validation.modeldriven;

import java.util.Objects;
import org.apache.commons.lang3.builder.EqualsBuilder;

/**
 * Bean representing a model ID.
 *
 * The modelIdAttribute field defines the attribute in the model that holds the modelId
 */
public class ModelId {

    public static final String ATTR_MODEL_NAME_VERSION_ID = "model-name-version-id";
    public static final String ATTR_MODEL_ID = "model-id";

    private String modelIdAttribute;
    private String id;

    /**
     * @param modelIdAttribute The name of the attribute that holds the model ID.
     * @param modelId The model ID.
     */
    public ModelId(String modelIdAttribute, String modelId) {
        super();
        this.modelIdAttribute = modelIdAttribute;
        this.id = modelId;
    }

    public String getModelIdAttribute() {
        return modelIdAttribute;
    }

    public void setModelIdAttribute(String modelIdAttribute) {
        this.modelIdAttribute = modelIdAttribute;
    }

    public String getModelId() {
        return id;
    }

    public void setModelId(String modelId) {
        this.id = modelId;
    }

    public boolean isEmpty() {
        return modelIdAttribute == null || id == null || modelIdAttribute.isEmpty() || id.isEmpty();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.modelIdAttribute);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ModelId)) {
            return false;
        } else if (obj == this) {
            return true;
        }
        ModelId rhs = (ModelId) obj;
        // @formatter:off
        return new EqualsBuilder()
                  .append(id, rhs.id)
                  .append(modelIdAttribute, rhs.modelIdAttribute)
                  .isEquals();
        // @formatter:on
    }
}
