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

import java.util.Locale;

/**
 * Validation service exception base class
 *
 */
public class BaseValidationServiceException extends Exception {

	private static final long serialVersionUID = -6663403070792969748L;

	public static final Locale LOCALE = Locale.US;

	private final String id;

	/**
	 * Default constructor.
	 *
	 * @param id
	 */
	public BaseValidationServiceException(String id) {
		super();
		this.id = id;
	}

	/**
	 * @param id
	 * @param message
	 */
	public BaseValidationServiceException(String id, String message) {
		super(message);
		this.id = id;
	}

	/**
	 * @param id
	 * @param message
	 * @param cause
	 */
	public BaseValidationServiceException(String id, String message, Throwable cause) {
		super(message, cause);
		this.id = id;
	}

	public String getId() {
		return this.id;
	}
}
