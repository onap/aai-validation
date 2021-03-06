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

import org.springframework.http.HttpHeaders;

/**
 * Bean to represent the ONAP request/transaction IDs required for EELF logging.
 *
 */
public class RequestHeaders {

    // ONAP request ID a.k.a. transaction ID or correlation ID
    public static final String HEADER_REQUEST_ID = "X-ECOMP-RequestID";
    public static final String HEADER_SERVICE_INSTANCE_ID = "X-ECOMP-ServiceInstanceID";

    private String requestId;
    private String instanceId;

    public RequestHeaders(HttpHeaders headers) {
        requestId = headers.getFirst(RequestHeaders.HEADER_REQUEST_ID);
        instanceId = headers.getFirst(RequestHeaders.HEADER_SERVICE_INSTANCE_ID);
    }

    public String getRequestId() {
        return requestId;
    }

    public String getInstanceId() {
        return instanceId;
    }

    @Override
    public String toString() {
        return "RequestHeaders [requestId=" + requestId + ", instanceId=" + instanceId + "]";
    }

}
