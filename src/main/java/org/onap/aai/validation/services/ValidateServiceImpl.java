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

import java.util.Optional;
import java.util.UUID;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import org.onap.aai.auth.AAIMicroServiceAuth;
import org.onap.aai.validation.controller.ValidationController;
import org.onap.aai.validation.controller.ValidationController.Result;
import org.onap.aai.validation.logging.ApplicationMsgs;
import org.onap.aai.validation.logging.LogHelper;
import org.onap.aai.validation.logging.LogHelper.MdcParameter;
import org.onap.aai.validation.logging.LogHelper.StatusCode;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.servlet.HandlerMapping;

/**
 * Validation Service HTTP interface implementation.
 *
 */
public class ValidateServiceImpl implements ValidateService {

    private static final LogHelper applicationLogger = LogHelper.INSTANCE;

    /**
     * This message is returned in the HTTP Response when an event is filtered (deliberately not validated).
     */
    public static final String DEFAULT_MESSAGE_FOR_FILTERED_EVENTS =
            "No validation results available. The action value may have caused the event to be filtered. Otherwise the event type or domain may be invalid.";

    /**
     * Events are passed to the controller which will execute the validation(s).
     */
    private ValidationController controller;
    private AAIMicroServiceAuth aaiMicroServiceAuth;

    /**
     * @param controller
     */
    @Inject
    public ValidateServiceImpl(final ValidationController controller, final AAIMicroServiceAuth aaiMicroServiceAuth) {
        this.controller = controller;
        this.aaiMicroServiceAuth = aaiMicroServiceAuth;
    }

    @Override
    public ResponseEntity<String> validate(@RequestHeader HttpHeaders headers, HttpServletRequest servletRequest,
            @RequestBody String event) {
        applicationLogger.startAudit(headers, servletRequest);
        applicationLogger.info(ApplicationMsgs.MESSAGE_VALIDATION_REQUEST, headers + event);

        String path = (String) servletRequest.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);

        if (applicationLogger.isDebugEnabled()) {
            applicationLogger.debug(String.format(
                    "Received request. Path \"%s\", HttpHeaders \"%s\", ServletRequest \"%s\", Request Body \"%s\"",
                    path, headers, servletRequest.getMethod(), event));
        }

        // Additional name/value pairs according to EELF guidelines
        applicationLogger.setContextValue("Protocol", "https");
        applicationLogger.setContextValue("Method", "POST");
        applicationLogger.setContextValue("Path", path);
        applicationLogger.setContextValue("Query", servletRequest.getQueryString());

        RequestHeaders requestHeaders = new RequestHeaders(headers);
        String requestId = requestHeaders.getRequestId();
        if (requestId == null) {
            requestId = UUID.randomUUID().toString();
            applicationLogger.info(ApplicationMsgs.MISSING_REQUEST_ID, requestId);
            applicationLogger.setContextValue(MdcParameter.REQUEST_ID, requestId);
        }

        ResponseEntity<String> response;

        try {
            boolean authorized =
                    aaiMicroServiceAuth.validateRequest(servletRequest, servletRequest.getMethod(), "validate");

            if (!authorized) {
                response = ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("User not authorized to perform the operation.");
            } else {
                response = validate(event);
            }
        } catch (Exception e) {
            applicationLogger.error(ApplicationMsgs.PROCESS_REQUEST_ERROR, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    "Error while processing request. Please check the validation service logs for more details.\n");
        }

        StatusCode statusDescription;
        int statusCode = response.getStatusCodeValue();
        if (Response.Status.Family.familyOf(statusCode).equals(Response.Status.Family.SUCCESSFUL)) {
            statusDescription = StatusCode.COMPLETE;
        } else {
            statusDescription = StatusCode.ERROR;
        }
        applicationLogger.logAudit(statusDescription, Integer.toString(statusCode),
                response.getStatusCode().getReasonPhrase(), response.getBody());

        return response;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.onap.aai.validation.services.ValidateService#validate(java.lang.String)
     */
    @Override
    public ResponseEntity<String> validate(String event) {

        try {
            // Attempt to validate the event
            Result result = controller.execute(event, "http");

            if (result.validationSuccessful()) {
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                        .body(result.getValidationResultAsJson());
            } else {
                Optional<String> errorText = result.getErrorText();
                if (!errorText.isPresent() || errorText.get().isEmpty()) {
                    errorText = Optional.of(DEFAULT_MESSAGE_FOR_FILTERED_EVENTS);
                } else {
                    applicationLogger.error(ApplicationMsgs.MALFORMED_REQUEST_ERROR, event);
                }
                return ResponseEntity.badRequest().body(errorText.orElse(""));
            }
        } catch (Exception e) {
            // Unchecked runtime exception - this is intended to catch potential programming errors.
            applicationLogger.error(ApplicationMsgs.PROCESS_REQUEST_ERROR, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    "Error while processing request. Please check the validation service logs for more details.\n" + e);
        }
    }

}
