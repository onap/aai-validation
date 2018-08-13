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
package org.onap.aai.validation.data.client;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import javax.ws.rs.core.MediaType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.aai.validation.config.RestConfig;
import org.onap.aai.validation.data.client.RestClient;
import org.onap.aai.validation.exception.ValidationServiceException;

/**
 * Simple tests for GET and POST failures so as to increase code coverage.
 * 
 * Note that the REST client is not properly initialised.
 *
 */
public class TestRestClient {

    static {
        System.setProperty("AJSC_HOME", ".");
    }

    private static final String TEST_URL = "/aai/v11";
    private RestConfig mockRestConfig;

    @Before
    public void setUp() {
        mockRestConfig = Mockito.mock(RestConfig.class);
        Mockito.when(mockRestConfig.getProtocol()).thenReturn("http");
        Mockito.when(mockRestConfig.getHost()).thenReturn("localhost");
        Mockito.when(mockRestConfig.getPort()).thenReturn(8080);
    }

    @Test
    public void validateConstructor() {
        RestClient restClient = new RestClient(mockRestConfig);
        assertNotNull(restClient);
        assertThat(restClient.toString(), is(notNullValue()));
    }

    @Test(expected = ValidationServiceException.class)
    public void getOperationFailure() throws ValidationServiceException {
        RestClient restClient = new RestClient(mockRestConfig);
        restClient.get(TEST_URL, MediaType.TEXT_PLAIN);
    }

    @Test(expected = ValidationServiceException.class)
    public void postOperationFailure() throws ValidationServiceException {
        RestClient restClient = new RestClient(mockRestConfig);
        restClient.post(TEST_URL, MediaType.TEXT_PLAIN);
    }
}
