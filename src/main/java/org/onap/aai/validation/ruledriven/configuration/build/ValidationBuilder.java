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
 * Builder for validation config section
 *
 */
public class ValidationBuilder extends ContentBuilder {
	/**
	 * Create an empty validation section
	 */
	public ValidationBuilder() {
		super("validation");
		indent = "\t";
	}

	/**
	 * Create a validation section using the existing rule configuration
	 *
	 * @param ruleConfig
	 */
	public ValidationBuilder(RuleSection ruleConfig) {
		this();
		appendValue("object", ruleConfig.getType());
		appendValue("objectId", ruleConfig.getObjectId());
		if (ruleConfig.isGeneric()) {
			addContent(new UseRuleBuilder(ruleConfig, indent + "\t").toString());
		} else {
			addContent(new RuleBuilder(ruleConfig, indent + "\t").toString());
		}
	}

	/**
	 * Add a useRule section
	 *
	 * @param name
	 * @return the new useRule section
	 */
	public UseRuleBuilder useRule(String name) {
		UseRuleBuilder item = new UseRuleBuilder();
		item.indent = "\t\t";
		item.appendValue("name", name);
		addContent(item);
		return item;
	}

	/**
	 * Add a rule section comprising the specified name item
	 *
	 * @param name
	 * @return
	 */
	public RuleBuilder rule(String name) {
		RuleBuilder item = new RuleBuilder();
		item.indent = "\t\t";
		item.appendValue("name", name);
		addContent(item);
		return item;
	}
}