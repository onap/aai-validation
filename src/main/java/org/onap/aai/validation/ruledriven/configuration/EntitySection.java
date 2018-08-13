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
package org.onap.aai.validation.ruledriven.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import org.onap.aai.validation.ruledriven.configuration.build.EntityBuilder;

/**
 * Rules Configuration: entity {} section.
 */
public class EntitySection {

	private String name;
	private String type;
	private final SortedSet<String> indices = new TreeSet<>();
	private final List<RuleSection> rules = new ArrayList<>();

	/**
	 * Rules are specified within an entity section.
	 */
	public EntitySection() {
		// Deliberately empty - invoked when an entity section is read from the rules DSL
	}

	@Override
	public String toString() {
		return new EntityBuilder(this).toString();
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setType(String entityType) {
		this.type = entityType;
	}

    public void setIndices(List<String> indices) {
        for(String index : indices) {
            this.indices.add(index.trim());
        }
    }

    public void setIndices(String[] indices) {
        for(String index : indices) {
            this.indices.add(index.trim());
        }
    }

    public SortedSet<String> getIndices() {
        return this.indices;
    }

	/**
	 * Adds the rule.
	 *
	 * @param rule
	 *            the rule
	 */
	public void addRule(RuleSection rule) {
		this.rules.add(rule);
	}

	/**
	 * <p>
	 * Getter for property {@link #rules}.
	 * </p>
	 *
	 * @return Value for property <tt>rules</tt>.
	 */
	public List<RuleSection> getRules() {
		return this.rules;
	}

}