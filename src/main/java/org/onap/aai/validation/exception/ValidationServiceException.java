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
package org.onap.aai.validation.exception;

/**
 * Validation service exception
 *
 */
public class ValidationServiceException extends BaseValidationServiceException { // NOSONAR

	private static final long serialVersionUID = 883498159309797607L;

	/**
	 * Constructs an exception defined by the Error.
	 *
	 * @param error
	 *            {@link ValidationServiceError} with the error id.
	 */
	public ValidationServiceException(ValidationServiceError error) {
		super(error.getId(), error.getId() + ", " + error.getMessage());
	}

	/**
	 * Constructs an exception defined by the Error. The message is parameterised with the arguments.
	 *
	 * @param error
	 *            {@link ValidationServiceError} with the error id.
	 * @param args
	 *            Arguments for the exception message.
	 */
	public ValidationServiceException(ValidationServiceError error, Object... args) {
		super(error.getId(), error.getId() + ", " + error.getMessage(args));
	}

	/**
	 * Constructs an exception defined by the Error and the underlying Exception. The message is parameterised with the
	 * arguments and enhanced with the underlying Exception message.
	 *
	 * @param error
	 *            {@link ValidationServiceError} with the error id.
	 * @param exception
	 *            Exception thrown by an underlying API.
	 * @param args
	 *            Arguments for the exception message.
	 */
	public ValidationServiceException(ValidationServiceError error, Exception exception, Object... args) {
		super(error.getId(), error.getId() + ", " + error.getMessage(args) + "; Caused by: " + exception.getMessage(), exception);
	}

	/**
	 * Constructs an exception defined by the Error and the underlying Exception. The message is enhanced with the
	 * underlying Exception message.
	 *
	 * @param error
	 *            {@link ValidationServiceError} with the error id.
	 * @param exception
	 *            Exception thrown by an underlying API.
	 */
	public ValidationServiceException(ValidationServiceError error, Exception exception) {
		super(error.getId(), error.getId() + ", " + error.getMessage() + "; Caused by: " + exception.getMessage(), exception);
	}

}
