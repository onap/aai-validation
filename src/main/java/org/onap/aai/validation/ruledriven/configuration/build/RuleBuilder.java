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
package org.onap.aai.validation.ruledriven.configuration.build;

import org.onap.aai.validation.ruledriven.configuration.RuleSection;

/**
 * Builder for rule config section.
 */
public class RuleBuilder extends ContentBuilder {

	/**
	 * Instantiates a new rule builder.
	 */
	public RuleBuilder() {
		super("rule");
	}

	/**
	 * Instantiates a new rule builder using the existing rule configuration
	 *
	 * @param ruleConfig
	 *            the rule configuration to clone
	 * @param indent
	 *            the indent/prefix for the section
	 */
	public RuleBuilder(RuleSection ruleConfig, String indent) {
		this();
		this.indent = indent;
		if (ruleConfig.isGeneric()) {
			appendLine(indent + "\t// Generic Rule");
		}
		appendValue("name", ruleConfig.getName());
		appendValue("category", ruleConfig.getCategory());
		appendValue("description", ruleConfig.getDescription());
		appendValue("severity", ruleConfig.getSeverity());
		if (ruleConfig.isGeneric()) {
			appendLine(indent + "\t// Passing " + ruleConfig.getAttributes());
		}
		appendValue("attributes", ruleConfig.getExpressionFieldNames());
		appendValue("validate", ruleConfig.getExpression());
	}

}