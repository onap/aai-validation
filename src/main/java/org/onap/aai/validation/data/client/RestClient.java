/**
 * ============LICENSE_START===================================================
 * Copyright (c) 2018-2019 European Software Marketing Ltd.
 * ============================================================================
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
 * ============LICENSE_END=====================================================
 */
package org.onap.aai.validation.data.client;

import java.util.Arrays;
import java.util.UUID;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import org.onap.aai.restclient.client.OperationResult;
import org.onap.aai.validation.config.RestConfig;
import org.onap.aai.validation.exception.ValidationServiceError;
import org.onap.aai.validation.exception.ValidationServiceException;

/**
 * REST client capable of establishing secured requests.
 */
public class RestClient {
    private RestConfig restConfig;
    private org.onap.aai.restclient.client.RestClient aaiRestClient;
    private MultivaluedMap<String, String> headers;

    private static final String ACCEPT = "application/json";
    private static final String HEADER_X_FROM_APP_ID = "validation-service";
    private static final String APP_CONFIG_HOME = System.getProperty("CONFIG_HOME");

    /**
     * Constructs a new rest client with the injected parameters.
     *
     * @param restConfig
     */
    @Inject
    public RestClient(RestConfig restConfig) {
        this.restConfig = restConfig;
        initialiseRestClient();
    }

    /** Initialises the REST client */
    private void initialiseRestClient() {
        // @formatter:off
        aaiRestClient = new org.onap.aai.restclient.client.RestClient()
            .validateServerHostname(false)
            .validateServerCertChain(true)
            .clientCertFile(APP_CONFIG_HOME + restConfig.getKeyStorePath())
            .clientCertPassword(restConfig.getKeyStorePassword())
            .trustStore(APP_CONFIG_HOME + restConfig.getTrustStorePath())
            .connectTimeoutMs(restConfig.getConnectionTimeout())
            .readTimeoutMs(restConfig.getReadTimeout());
        // @formatter:on

        headers = new MultivaluedHashMap<>();
        headers.put("Accept", Arrays.asList(ACCEPT));
        headers.put("X-FromAppId", Arrays.asList(HEADER_X_FROM_APP_ID));
        headers.put("X-TransactionId", Arrays.asList(UUID.randomUUID().toString()));
    }

    /**
     * Invokes the REST URL and returns the payload string.
     *
     * @param uriPath
     * @param mediaType
     * @return The payload of the REST URL call as a string.
     * @throws ValidationServiceException
     */
    public String get(String uriPath, String mediaType) throws ValidationServiceException {
        // Construct URI
        String uri = restConfig.getProtocol() + "://" + restConfig.getHost() + ":" + restConfig.getPort() + uriPath;

        OperationResult result = aaiRestClient.get(uri, headers, MediaType.valueOf(mediaType));

        if (result.getResultCode() == 200) {
            return result.getResult();
        } else if (result.getResultCode() == 404) {
            throw new ValidationServiceException(ValidationServiceError.REST_CLIENT_RESPONSE_NOT_FOUND,
                    result.getResultCode(), result.getFailureCause());
        } else {
            throw new ValidationServiceException(ValidationServiceError.REST_CLIENT_RESPONSE_ERROR,
                    result.getResultCode(), result.getFailureCause());
        }
    }

    /**
     * POSTs a request.
     *
     * @param url
     * @param payload
     * @return The payload of the REST URL call as a string.
     * @throws GapServiceException
     */
    public String post(String url, String payload) throws ValidationServiceException {
        OperationResult result = aaiRestClient.post(url, payload, headers, MediaType.APPLICATION_JSON_TYPE,
                MediaType.APPLICATION_JSON_TYPE);
        if (result.getResultCode() == 200) {
            return result.getResult();
        } else {
            throw new ValidationServiceException(ValidationServiceError.REST_CLIENT_RESPONSE_ERROR,
                    result.getResultCode(), result.getFailureCause());
        }
    }
}
