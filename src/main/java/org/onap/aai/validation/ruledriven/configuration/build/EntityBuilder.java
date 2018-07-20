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

import java.util.Properties;
import org.onap.aai.validation.ruledriven.configuration.EntitySection;
import org.onap.aai.validation.ruledriven.configuration.RuleSection;

/**
 * Builder for entity config section
 *
 */
public class EntityBuilder extends ContentBuilder {

	/**
	 * Create an empty entity section
	 */
	public EntityBuilder() {
		super("entity");
	}

	/**
	 * @param auditConfiguration
	 */
	public EntityBuilder(EntitySection auditConfiguration) {
		this();
		appendValue("name", auditConfiguration.getName());

		for (RuleSection rule : auditConfiguration.getRules()) {
			addContent(new ValidationBuilder(rule).toString());
		}
	}

	/**
	 * Add an empty validation section to this entity
	 *
	 * @return
	 */
	public ValidationBuilder validation() {
		ValidationBuilder item = new ValidationBuilder();
		addContent(item);
		return item;
	}

	/**
	 * @param props
	 * @return
	 */
	public ValidationBuilder validation(Properties props) {
		ValidationBuilder item = validation();
		item.appendProperties(props);
		return item;
	}

}