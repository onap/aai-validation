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

import java.util.Objects;
import org.apache.commons.lang3.builder.EqualsBuilder;

/**
 * Describes an identifier for an entity.
 */
public class EntityId {

	private String primaryKey;
	private String value;

	/**
	 * Construct an entity Id
	 */
	public EntityId() {
		// Deliberately empty
	}

	/**
	 * Construct an entity Id (key-value pair)
	 *
	 * @param primaryKey
	 * @param value
	 */
	public EntityId(String primaryKey, String value) {
		this.primaryKey = primaryKey;
		this.value = value;
	}

	public String getPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(String primaryKey) {
		this.primaryKey = primaryKey;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.primaryKey, this.value);
	}

	@Override
	public boolean equals(Object obj) {
        if (!(obj instanceof EntityId)) {
            return false;
     } else if (obj == this) {
            return true;
     }
        EntityId rhs = (EntityId) obj;
     // @formatter:off
     return new EqualsBuilder()
                  .append(primaryKey, rhs.primaryKey)
                  .append(value, rhs.value)
                  .isEquals();
     // @formatter:on
	}

	@Override
	public String toString() {
		return primaryKey + "=" + value;
	}

}
