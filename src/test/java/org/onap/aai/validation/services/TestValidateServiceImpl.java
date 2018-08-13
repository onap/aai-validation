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
package org.onap.aai.validation.services;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.net.URISyntaxException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import javax.security.auth.x500.X500Principal;
import javax.ws.rs.core.MultivaluedHashMap;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.aai.auth.AAIMicroServiceAuth;
import org.onap.aai.validation.controller.ValidationController;
import org.onap.aai.validation.services.ValidateServiceImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

public class TestValidateServiceImpl {

    static {
        System.setProperty("APP_HOME", ".");
    }

    private ValidationController mockValidationController;
    private AAIMicroServiceAuth mockAaiMicroServiceAuth;

    @Before
    public void setUp() {
        mockValidationController = Mockito.mock(ValidationController.class);
        mockAaiMicroServiceAuth = Mockito.mock(AAIMicroServiceAuth.class);
    }

    @Test
    public void validateConstructor() {
        ValidateServiceImpl validateServiceImpl =
                new ValidateServiceImpl(mockValidationController, mockAaiMicroServiceAuth);
        assertNotNull(validateServiceImpl);
        assertThat(validateServiceImpl.toString(), is(notNullValue()));
    }

    @Test
    public void testValidateEventWithoutHeaderFailure() {
        ValidateServiceImpl validateServiceImpl =
                new ValidateServiceImpl(mockValidationController, mockAaiMicroServiceAuth);
        ResponseEntity<String> response = validateServiceImpl.validate("testEvent");
        assertThat(response.getStatusCode(), is(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    /**
     * Create a (mocked) HTTPS request and invoke the Babel generate artifacts API
     * 
     * @param request for the Babel Service
     * @return the Response from the HTTP API
     * @throws URISyntaxException
     */
    @Test
    public void testRequestWithHeaders() throws URISyntaxException {
        // Create mocked request headers map
        MultivaluedHashMap<String, String> headersMap = new MultivaluedHashMap<>();
        headersMap.put("X-TransactionId", createSingletonList("transaction-id"));
        headersMap.put("X-FromAppId", createSingletonList("app-id"));
        headersMap.put("Host", createSingletonList("hostname"));

        HttpHeaders headers = Mockito.mock(HttpHeaders.class);
        for (Entry<String, List<String>> entry : headersMap.entrySet()) {
            Mockito.when(headers.get(entry.getKey())).thenReturn(entry.getValue());
        }

        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setSecure(true);
        servletRequest.setScheme("https");
        servletRequest.setServerPort(9501);
        servletRequest.setServerName("localhost");
        servletRequest.setRequestURI("/services/validation-service/v1/app/validate");

        X509Certificate mockCertificate = Mockito.mock(X509Certificate.class);
        Mockito.when(mockCertificate.getSubjectX500Principal())
                .thenReturn(new X500Principal("CN=test, OU=qa, O=Test Ltd, L=London, ST=London, C=GB"));

        servletRequest.setAttribute("javax.servlet.request.X509Certificate", new X509Certificate[] {mockCertificate});
        servletRequest.setAttribute("javax.servlet.request.cipher_suite", "");

        ValidateServiceImpl service = new ValidateServiceImpl(mockValidationController, mockAaiMicroServiceAuth);
        service.validate(headers, servletRequest, "testEvent");
    }

    private List<String> createSingletonList(String listItem) {
        return Collections.<String>singletonList(listItem);
    }
}
