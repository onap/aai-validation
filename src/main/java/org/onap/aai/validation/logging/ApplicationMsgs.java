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
package org.onap.aai.validation.logging;

import com.att.eelf.i18n.EELFResourceManager;
import org.onap.aai.cl.eelf.LogMessageEnum;

/**
 * Application messages
 */
public enum ApplicationMsgs implements LogMessageEnum {

    /**
     * Add message keys here.
     */
    // @formatter:off
	MESSAGE_VALIDATION_REQUEST,
    MESSAGE_AUDIT,
    MESSAGE_METRIC,
   	MESSAGE_PUBLISH_ERROR,
	OXM_LOAD_ERROR,
	OXM_MISSING_KEY_ERROR,
    MISSING_REQUEST_ID,
	CANNOT_VALIDATE_ERROR,
	CANNOT_VALIDATE_HANDLE_EXCEPTION_ERROR,
	POLL_EVENTS,
	NUMBER_OF_MESSAGES_CONSUMED,
	INVOKE_EVENT_CONSUMER_ERROR,
	READ_FILE_ERROR,
	STARTUP_SERVLET_INIT,
	POLLING_INTERVAL_CONFIG_NOT_PRESENT,
	POLLING_FOR_EVENTS,
	POLLING_DISABLED,
	STARTUP_SERVLET_INIT_SUCCESS,
	UNSENT_MESSAGE_WARN,
	UNSENT_MESSAGE_ERROR,
	EVENT_CLIENT_CLOSE_UNSENT_MESSAGE,
	SEND_MESSAGE_ABORT_WARN,
	SEND_MESSAGE_RETRY_WARN,
	FILE_ARG_NULL_ERROR,
	LOAD_PROPERTIES,
	FILE_LOAD_INTO_MAP,
	FILE_LOAD_INTO_MAP_ERROR,
	CREATE_PROPERTY_MAP_ERROR,
	FILE_MONITOR_BLOCK_ERROR,
	READ_FILE_STREAM_ERROR,
	STRING_UTILS_INVALID_REGEX,
	MALFORMED_REQUEST_ERROR,
	PROCESS_REQUEST_ERROR,
    INVALID_EVENT_TYPE,
    MISSING_EVENT_TYPE,
    FILTERED_EVENT;
	// @formatter:on

    /**
     * Static initializer to ensure the resource bundles for this class are loaded... Here this application loads
     * messages from three bundles
     */
    static {
        EELFResourceManager.loadMessageBundle("validation-service-logging-resources");
    }
}
