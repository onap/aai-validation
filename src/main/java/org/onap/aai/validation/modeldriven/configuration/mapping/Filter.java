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
import java.util.Objects;
import org.apache.commons.lang3.builder.EqualsBuilder;

/**
 * Defines a path and value that will be used to validate a particular model.
 * @see ValueConfiguration and ModelInstanceMapper
 */
public class Filter {

	private String path;
	private List<String> valid = new ArrayList<>();

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public List<String> getValid() {
		return valid;
	}

	public void setValid(List<String> valid) {
		this.valid = valid;
	}

	@Override
	public int hashCode() {
		return Objects.hash(path, valid);
	}

	@Override
	public boolean equals(Object obj) {
        if (!(obj instanceof Filter)) {
            return false;
     } else if (obj == this) {
            return true;
     }
        Filter rhs = (Filter) obj;
     // @formatter:off
     return new EqualsBuilder()
                  .append(path, rhs.path)
                  .append(valid, rhs.valid)
                  .isEquals();
     // @formatter:on
	}
	
	@Override
	public String toString() {
		return "Filter [path=" + path + ", valid=" + valid + "]";
	}
}
