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
package org.onap.aai.validation.publisher;

import java.util.Collection;
import org.onap.aai.validation.exception.ValidationServiceException;

/**
 * A Publisher of messages.
 *
 */
public interface MessagePublisher {

	/**
	 * Sends a message somewhere.
	 *
	 * @param message
	 *            The String message to send.
	 * @throws ValidationServiceException
	 */
	void publishMessage(String message) throws ValidationServiceException;
	
	/**
	 * Sends a Collection of messages somewhere.
	 *
	 * @param messages
	 *            The String messages to send.
	 * @throws ValidationServiceException
	 */
	void publishMessages(Collection<String> messages) throws ValidationServiceException;

}