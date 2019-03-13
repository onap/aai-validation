/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright (c) 2018-2019 AT&T Intellectual Property. All rights reserved.
 * Copyright (c) 2018-2019 European Software Marketing Ltd.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.aai.validation.exception;

import java.text.MessageFormat;

/**
 * Error text formatting.
 *
 */
public enum ValidationServiceError {

    //@formatter:off

	// Rule Configuration exceptions. Range 100..199
	RULES_FILE_ERROR("VS-100", "Error reading rules configuration file(s) {0}"),
	RULE_UNEXPECTED_TOKEN("VS-101", "Token {0} unexpected in rules configuration file."),
    RULES_NOT_DEFINED("VS-102", "Entity type {0} (Event type {1}) has no rule definitions."),
    
	// Rule exceptions. Range 200..299
	RULE_EXECUTION_ERROR("VS-201", "Error executing rule {0} with arguments {1}"),

	// Validation service processing exceptions. Range 300..399
	OMX_LOAD_ERROR("VS-300", "Validation service failed to load the OXM file."),
	OXM_MISSING_KEY("VS-301", "Validation service failed to retrieve primary key for object of type {0}."),
	MESSAGE_DIGEST_ERROR("VS-302", "Java platform security error. Failed to create Message Digest hashing object {0}."),
	MODEL_RETRIEVAL_ERROR("VS-303", "Validator failed to get the model from external system."),
	MODEL_CACHE_ERROR("VS-304", "Validator failed to retrieve the model from the cache. {0}"),
	MODEL_NOT_FOUND("VS-305", "Model with UUID {0} not found."),

	// Event publishing exceptions. Range 400..499
	EVENT_CLIENT_PUBLISHER_INIT_ERROR("VS-400", "Error while initialising the Event Publisher Client."),
	EVENT_CLIENT_SEND_ERROR("VS-401", "Error while sending a message to the event bus."),
	EVENT_CLIENT_INCORRECT_NUMBER_OF_MESSAGES_SENT("VS-402", "Publisher client returned a result of {0} messages sent."),
	EVENT_CLIENT_CLOSE_ERROR("VS-403", "Error while closing the Event Publisher Client."),
	EVENT_CLIENT_CLOSE_UNSENT_MESSAGE("VS-404", "Failed to publish message. Error while closing the Event Publisher Client. " +
	                                            "The following message is unsent: {0}. Please check the logs for more information."),
	EVENT_CLIENT_CONSUMER_INIT_ERROR("VS-405", "Error while initialising the Event Consumer Client."),

	// Reader exceptions. Range 500..599
	JSON_READER_PARSE_ERROR("VS-500", "JSON could not be parsed."),
	EVENT_READER_MISSING_PROPERTY("VS-501", "Missing property: {0}"),
	EVENT_READER_TOO_MANY_ENTITIES("VS-502", "Unexpected number or entities."),
	INSTANCE_READER_NO_INSTANCE("VS-503", "Failed to extract instance under path: {0}. JSON payload: {1}"),
	EVENT_READER_PROPERTY_READ_ERROR("VS-504", "Failed to read entity link property. Check event reader configuration properties."),

	// Model-instance mapping exceptions. Range 600..649
	MODEL_INSTANCE_MAPPING_RETRIEVAL_ERROR("VS-600", "Error retrieving model-instance mappings."),
	MODEL_INSTANCE_MAPPING_FILE_IO_ERROR("VS-601", "IO error when reading from the model-instance mapping file {0}."),
	MODEL_PARSE_ERROR("VS-602", "Validator failed to parse the model provided. Source:\n{0}"),
	MODEL_VALUE_ERROR("VS-603", "Validator failed to access the model value {0} in {1}"),
	INSTANCE_MAPPING_ROOT_ERROR("VS-604", "Missing 'root' property in instance mapping."),

	// REST client exceptions. Range 650..699
	REST_CLIENT_RESPONSE_ERROR("VS-650", "REST client response error: Response Code: {0}, Failure Cause: {1}."),
	REST_CLIENT_RESPONSE_NOT_FOUND("VS-651", "REST client response error: Response Code: {0}, Failure Cause: {1}."),

	// Validation Service configuration exceptions. Range 700..799
	VS_PROPERTIES_LOAD_ERROR("VS-700", "Failed to read property {0} in the validation service properties file."),

	// Miscellaneous exceptions. Range 800..849
	STRING_UTILS_INVALID_REGEX("VS-800", "Invalid regular expression: {0}");

	//@formatter:on

    private String id;
    private String message;

    private ValidationServiceError(String id, String message) {
        this.id = id;
        this.message = message;
    }

    public String getId() {
        return this.id;
    }

    /**
     * @param args
     *            to be formatted
     * @return the formatted error message
     */
    public String getMessage(Object... args) {
        MessageFormat formatter = new MessageFormat("");
        formatter.applyPattern(this.message);
        return formatter.format(args);
    }
}
