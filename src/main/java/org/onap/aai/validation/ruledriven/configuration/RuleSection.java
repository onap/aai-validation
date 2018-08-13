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
import java.util.Arrays;
import java.util.List;
import org.onap.aai.validation.ruledriven.configuration.build.RuleBuilder;

/**
 * Rules Configuration: rule {} section.
 */
public class RuleSection {

	private String name;
	private boolean isGenericRule;
	private String description;
	private String category;
	private String errorMessage;
	private String type;
	private String objectId;
	private String severity;
	private List<String> attributes = new ArrayList<>();
	private List<String> fields = new ArrayList<>();
	private String expression;

	/**
	 * Rules may be defined within an entity {} section.
	 */
	public RuleSection() {
		isGenericRule = false;
	}

	/**
	 * @param attribute
	 */
	public void addAttribute(String attribute) {
		if (this.attributes == null) {
			this.attributes = new ArrayList<>();
		}
		this.attributes.add(attribute);
	}

	private void addAttributeMapping(String field) {
		if (this.fields == null) {
			this.fields = new ArrayList<>();
		}
		this.fields.add(field);
	}

	private void addAttributeNames(List<String> fieldNames) {
		for (String attribute : fieldNames) {
			addAttributeMapping(attribute);
		}
	}

	private void addAttributes(List<String> attributes) {
		for (String attribute : attributes) {
			addAttribute(attribute);
		}
	}

	/**
	 * Make a copy of a generically defined rule so that we can tailor it for our specific entity.
	 *
	 * @param genericRule
	 */
	public void copyFrom(RuleSection genericRule) {
		setCategory(genericRule.getCategory());
		setDescription(genericRule.getDescription());
		setSeverity(genericRule.getSeverity());
		setExpression(genericRule.getExpression());
		setErrorMessage(genericRule.getErrorMessage());
		if (genericRule.getAttributes() != null) {
			if (getAttributes().isEmpty()) {
				addAttributes(genericRule.getAttributes());
			} else {
				// Map the attributes
				addAttributeNames(genericRule.getAttributes());
			}
		}

		if (getAttributes().isEmpty()) {
			throw new IllegalArgumentException("No attributes defined");
		}
	}

	public List<String> getAttributes() {
		return new ArrayList<>(attributes);
	}

	public String getCategory() {
		return category;
	}

	public String getDescription() {
		return description;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public String getExpression() {
		return expression;
	}

	public List<String> getExpressionFieldNames() {
		if (fields.size() < attributes.size()) {
			return attributes;
		} else {
			return fields;
		}
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public String getObjectId() {
		return objectId;
	}

	public String getSeverity() {
		return severity;
	}

	public boolean isGeneric() {
		return isGenericRule;
	}

	public void setAttributes(List<String> attributes) {
		this.attributes = attributes;
	}

	public void setAttributes(String[] attributes) {
		this.attributes = new ArrayList<>(Arrays.asList(attributes));
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public void setIsGeneric(boolean isGenericRule) {
		this.isGenericRule = isGenericRule;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setObject(String object) {
		this.type = object;
	}

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	public void setSeverity(String severity) {
		this.severity = severity;
	}

	@Override
	public String toString() {
		return new RuleBuilder(this, "").toString();
	}

}