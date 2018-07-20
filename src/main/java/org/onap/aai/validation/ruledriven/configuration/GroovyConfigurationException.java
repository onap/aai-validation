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

import groovy.lang.MissingMethodException;
import groovy.lang.MissingPropertyException;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.onap.aai.validation.exception.BaseValidationServiceException;
import org.onap.aai.validation.exception.ValidationServiceError;

/**
 * Configuration load/parse exceptions
 *
 */
@SuppressWarnings("serial")
public class GroovyConfigurationException extends BaseValidationServiceException {

	private String invalidToken; // NOSONAR
	private String configText; // NOSONAR

	/**
	 * @param text
	 */
	public GroovyConfigurationException(String text) {
		super(ValidationServiceError.RULE_UNEXPECTED_TOKEN.getId(), ValidationServiceError.RULE_UNEXPECTED_TOKEN.getId() + ", " + text);
	}

	/**
	 * @param text
	 * @param configuration
	 */
	public GroovyConfigurationException(String text, String configuration) {
		super(text);
		this.configText = configuration;
	}

	/**
	 * @param e
	 *            exception for a missing method
	 * @param configuration
	 */
	public GroovyConfigurationException(MissingMethodException e, String configuration) {
		super(ValidationServiceError.RULE_UNEXPECTED_TOKEN.getId(), "Invalid keyword " + e.getMethod(), e);
		setInvalidToken(e.getMethod());
		this.configText = configuration;
	}

	/**
	 * @param e
	 * @param configuration
	 */
	public GroovyConfigurationException(MissingPropertyException e, String configuration) {
		super(ValidationServiceError.RULE_UNEXPECTED_TOKEN.getId(), "Invalid keyword " + e.getProperty(), e);
		setInvalidToken(e.getProperty());
		this.configText = configuration;
	}

	/**
	 * @param e
	 */
	public GroovyConfigurationException(CompilationFailedException e) {
		super(ValidationServiceError.RULE_UNEXPECTED_TOKEN.getId(), ValidationServiceError.RULE_UNEXPECTED_TOKEN.getId() + ", "
				+ ValidationServiceError.RULE_UNEXPECTED_TOKEN.getMessage() + "; Caused by: " + e.getMessage(), e);
	}

	/**
	 * @param e
	 * @param configuration
	 */
	public GroovyConfigurationException(MultipleCompilationErrorsException e, String configuration) {
		super(ValidationServiceError.RULE_UNEXPECTED_TOKEN.getId(), "", e);
		this.configText = configuration;
	}

	public String getInvalidToken() {
		return invalidToken;
	}

	public void setInvalidToken(String token) {
		this.invalidToken = token;
	}

	public String getConfigText() {
		return configText;
	}

}
