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

package org.onap.aai.validation.ruledriven.rule.builtin;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import org.onap.aai.cl.api.Logger;
import org.onap.aai.restclient.client.OperationResult;
import org.onap.aai.validation.exception.ValidationServiceError;
import org.onap.aai.validation.logging.ApplicationMsgs;
import org.onap.aai.validation.logging.LogHelper;

public class DataDictionary {

    private static final Logger logger = LogHelper.INSTANCE;

    private static String credentials;
    private static String urlTemplate;
    private static org.onap.aai.restclient.client.RestClient restClient;

    public enum COMMON_MODEL_ELEMENT_TYPE {
        INSTANCE, ATTRIBUTE, UNSUPPORTED;
    }

    private DataDictionary() {
        // intentionally empty
    }

    /**
     * Initializes this class' static variables using MethodInvokingFactoryBean
     *
     * @param props
     */
    public static void setProperties(Properties props) {

        String hostport = props.getProperty("rule.datadictionary.hostport");
        String connectTimeout = props.getProperty("rule.datadictionary.connect.timeout");
        String readTimeout = props.getProperty("rule.datadictionary.read.timeout");
        String uriTemplate = props.getProperty("rule.datadictionary.uri");

        credentials = "Basic " + props.getProperty("rule.datadictionary.credentials");
        urlTemplate = hostport + uriTemplate;
        restClient = new org.onap.aai.restclient.client.RestClient() //
                .validateServerHostname(false) //
                .connectTimeoutMs(Integer.parseInt(connectTimeout)) //
                .readTimeoutMs(Integer.parseInt(readTimeout));
    }

    /**
     * Generates a REST request to data-dictionary to validate the given attributes.
     *
     * URI: /commonModelElements/[commonModelElementId]/validateInstance where commonModelElementId is defined as:
     * [commonModelElementType]~[commonModelElementName]~[commonModelElementVersion]
     *
     * Supported commonModelElementType: instance attribute
     *
     * Examples: /commonModelElements/instance~nfValuesCatalog~1.0/validateInstance
     * /commonModelElements/attribute~nfRole~1.0/validateInstance
     *
     * @param commonModelElementType
     *            "instance" or "attribute"
     * @param commonModelElementName
     *            name of common model element
     * @param attributeName
     * @param attributeValue
     * @return
     */
    public static String validate(String commonModelElementType, String commonModelElementName, String attributeName,
            String attributeValue) {

        COMMON_MODEL_ELEMENT_TYPE cmeType = COMMON_MODEL_ELEMENT_TYPE.UNSUPPORTED;
        try {
            cmeType = COMMON_MODEL_ELEMENT_TYPE.valueOf(commonModelElementType.toUpperCase());
        } catch (IllegalArgumentException e) {
            final String error = "unsupported commonModelElementType: " + commonModelElementType;
            logger.error(ApplicationMsgs.CANNOT_VALIDATE_ERROR, error);
            return error;
        }

        if (attributeValue == null || attributeValue.isEmpty()) {
            final String error = "element value missing";
            logger.error(ApplicationMsgs.CANNOT_VALIDATE_ERROR, error);
            return error;
        }

        logger.debug("Executing built-in rule with: '" + commonModelElementType + "', '" + commonModelElementName
                + "'; attribute: " + attributeName + "=" + attributeValue);

        Gson gson = new GsonBuilder().create();
        String payload = gson.toJson(new Request(cmeType, attributeName, attributeValue));

        String url = MessageFormat.format(urlTemplate, commonModelElementType, commonModelElementName);
        OperationResult result = post(url, payload);

        if (result.getResultCode() == 500) {
            // network unreachable; log a warning and return success
            logger.warn(ApplicationMsgs.EVENT_CLIENT_CLOSE_UNSENT_MESSAGE,
                    ValidationServiceError.REST_CLIENT_RESPONSE_ERROR.getMessage(result.getResultCode(),
                            result.getFailureCause()));
            return "";
        }

        if (result.getResultCode() != 200 && result.getResultCode() != 204) {
            String error = ValidationServiceError.REST_CLIENT_RESPONSE_ERROR.getMessage(result.getResultCode(),
                    result.getFailureCause());
            logger.error(ApplicationMsgs.CANNOT_VALIDATE_ERROR, error);
            return result.getFailureCause();
        }
        return "";
    }

    /**
     * Posts the payload to the URL
     *
     * @param url
     * @param payload
     * @return
     */
    private static OperationResult post(String url, String payload) {
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.put("x-authorization", Arrays.asList(credentials));
        return restClient.post(url, payload, headers, MediaType.APPLICATION_JSON_TYPE, MediaType.APPLICATION_JSON_TYPE);
    }

    /**
     * JSON serializable class representing an instance sent to data-dictionary.
     */
    public static class Request {
        @Expose
        @SerializedName("instance")
        private Object instance;

        public Request(COMMON_MODEL_ELEMENT_TYPE cmeType, String attributeName, String attributeValue) {
            switch (cmeType) {
                case INSTANCE:
                    instance = Collections.singletonList(ImmutableMap.of(attributeName, attributeValue));
                    break;
                case ATTRIBUTE:
                    instance = attributeValue;
                    break;
                default:
                    logger.error(ApplicationMsgs.CANNOT_VALIDATE_ERROR, "unsupported commonModelElementType");
                    break;
            }
        }

        public Object getInstance() {
            return instance;
        }
    }
}
