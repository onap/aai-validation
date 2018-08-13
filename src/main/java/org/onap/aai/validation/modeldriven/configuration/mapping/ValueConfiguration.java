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
 * Describes a model or instance value that is used in model to instance comparison.
 *
 * @see ModelInstanceMapper
 */
public class ValueConfiguration {

    /** Top level element. From which all navigation starts. */
    private String origin;

    /**
     * Root element from which the value will be extracted. Can be used to recursively navigate the model/instance
     * hierarchy.
     */
    private String root;

    /**
     * Provides validation on the model before the value is extracted. If the filter is not satisfied other root models
     * will be searched.
     */
    private Filter filter;

    /**
     * Path to the model id
     */
    private String id;

    /**
     * Path to a model or instance value
     */
    private String value;

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    public Filter getFilter() {
        return filter;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.filter, this.origin, this.root, this.value, this.id);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ValueConfiguration)) {
            return false;
        } else if (obj == this) {
            return true;
        }
        ValueConfiguration rhs = (ValueConfiguration) obj;
     // @formatter:off
     return new EqualsBuilder()
                  .append(filter, rhs.filter)
                  .append(origin, rhs.origin)
                  .append(root, rhs.root)
                  .append(value, rhs.value)
                  .append(id, rhs.id)
                  .isEquals();
     // @formatter:on
    }

    @Override
    public String toString() {
        return "ValueConfiguration [origin=" + origin + ", root=" + root + ", id=" + id + ", filter=" + filter
                + ", value=" + value + "]";
    }
}
