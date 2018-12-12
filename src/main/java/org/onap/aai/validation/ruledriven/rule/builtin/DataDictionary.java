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
package org.onap.aai.validation.ruledriven.rule.builtin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import groovy.lang.GString;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.ws.rs.core.MediaType;
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


    private DataDictionary() {
        // intentionally empty
    }

    /**
     * Initializes this class' static variables using MethodInvokingFactoryBean
     * @param props
     */
    public static void setProperties(Properties props) {

        String hostport = props.getProperty("rule.datadictionary.hostport");
        String connectTimeout = props.getProperty("rule.datadictionary.connect.timeout");
        String readTimeout = props.getProperty("rule.datadictionary.read.timeout");
        String uriTemplate = props.getProperty("rule.datadictionary.uri");

        credentials = "Basic " + props.getProperty("rule.datadictionary.credentials");
        urlTemplate = hostport + uriTemplate;
        restClient = new org.onap.aai.restclient.client.RestClient()
                .validateServerHostname(false)
                .connectTimeoutMs(Integer.parseInt(connectTimeout))
                .readTimeoutMs(Integer.parseInt(readTimeout));
    }


    /**
     * Generates a REST request to data-dictionary to validate the given entities.
     * URI: /commonModelElements/[entityType]~[entityName]~1.0/validateInstance
     * Currently only "instance" is supported for entityType.
     *
     * @param entityType
     * @param entityName
     * @param entities
     * @return
     */
    public static String validate(String entityType, String entityName, List<Map<GString, GString>> entities) {

        logger.debug("Executing built-in rule with...");
        for(Map<GString, GString> attributes : entities) {
            logger.debug("   " + attributes.toString());
        }

        if(entities == null || entities.isEmpty()) {
            final String error = "list of instances missing";
            logger.error(ApplicationMsgs.CANNOT_VALIDATE_ERROR, error);
            return error;
        }

        String url = MessageFormat.format(urlTemplate, entityType, entityName);
        Gson gson = new GsonBuilder().create();
        String payload = gson.toJson(new Request(entities));
        OperationResult result = post(url, payload);

        if(result.getResultCode() != 200 && result.getResultCode() != 204) {
            String error = ValidationServiceError.REST_CLIENT_RESPONSE_ERROR.getMessage(result.getResultCode(), result.getFailureCause());
            logger.error(ApplicationMsgs.CANNOT_VALIDATE_ERROR, error);
            return result.getFailureCause();
        }
        return "";
    }


    /**
     * Posts the payload to the URL
     * @param url
     * @param payload
     * @return
     */
    private static OperationResult post(String url, String payload) {
        MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
        headers.put("x-authorization", Arrays.asList(credentials));
        return restClient.post(url, payload, headers, MediaType.APPLICATION_JSON_TYPE, MediaType.APPLICATION_JSON_TYPE);
    }


    /**
     * JSON serializable class representing an instance sent to data-dictionary.
     */
    public static class Request {
        @Expose
        @SerializedName("instance")
        private List<Object> instances;

        public Request(List<Map<GString, GString>> groovyInstances) {
            instances = new ArrayList<>();
            for(Map<GString, GString> groovyEntry : groovyInstances) {
                instances.add(createInstance(groovyEntry));
            }
        }

        public List<Object> getInstance() {
            return instances;
        }

        /**
         * Creates an instance entry; converts Groovy's GString into a java String
         * @param groovyMap
         * @return
         */
        private Map<String, String> createInstance(Map<GString, GString> groovyMap) {
            Map<String, String> newMap = new HashMap<>();
            for(Map.Entry<GString, GString> groovyEntry : groovyMap.entrySet()) {
                newMap.put(groovyEntry.getKey().toString(), groovyEntry.getValue().toString());
            }
            return newMap;
        }
    }
}
