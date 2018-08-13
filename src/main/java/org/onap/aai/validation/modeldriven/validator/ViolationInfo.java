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
package org.onap.aai.validation.modeldriven.validator;

import java.text.MessageFormat;
import org.onap.aai.validation.controller.ValidationController;

/**
 * Defines and formats the violation information.
 */
public enum ViolationInfo {
	//@formatter:off
	NO_MODEL           ("NO_MODEL",        ValidationController.VALIDATION_ERROR_SEVERITY, "No model ID=[{0}]",                              "The model [{0}] could not be found"),
	MISSING_ATTR       ("MISSING_ATTR",    ValidationController.VALIDATION_ERROR_SEVERITY, "{0} {1}=[{2}]",                                  "Attribute [{0}] is missing in the object instance"),
	UNEXPECTED_ATTR    ("UNEXPECTED_ATTR", ValidationController.VALIDATION_ERROR_SEVERITY, "{0} {1}=[{2}]",                                  "Attribute [{0}] should not be present in the object instance"),
	MISSING_REL        ("MISSING_REL",     ValidationController.VALIDATION_ERROR_SEVERITY, "entityId=[{0}] entityType=[{1}], {2} {3}=[{4}]", "Entity {0} of type [{1}] must be related to [{2}]"),
	UNEXPECTED_REL     ("UNEXPECTED_REL",  ValidationController.VALIDATION_ERROR_SEVERITY, "entityId=[{0}] entityType=[{1}], {2} {3}=[{4}]", "Entity {0} of type [{1}] must not be related to [{2}]");
	//@formatter:on

	private String category;
	private String severity;
	private String violationDetails;
	private String errorMessage;

	/**
	 * @param category
	 * @param severity
	 * @param violationDetails
	 * @param errorMessage
	 */
	private ViolationInfo(String category, String severity, String violationDetails, String errorMessage) {
		this.category = category;
		this.severity = severity;
		this.violationDetails = violationDetails;
		this.errorMessage = errorMessage;
	}

	/**
	 * @return
	 */
	public String getCategory() {
		return this.category;
	}

	/**
	 * @return
	 */
	public String getSeverity() {
		return this.severity;
	}

	/**
	 * @param args
	 * @return
	 */
	public String getViolationDetails(Object... args) {
		return formatter(this.violationDetails, args);
	}

	/**
	 * @param args
	 * @return
	 */
	public String getErrorMessage(Object... args) {
		return formatter(this.errorMessage, args);
	}

	private String formatter(String violationInfo, Object... args) {
		MessageFormat formatter = new MessageFormat("");
		formatter.applyPattern(violationInfo);
		return formatter.format(args);
	}
}