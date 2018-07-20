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

import javax.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Validates an event (containing details of an entity) and synchronously returns the details of the validation
 * result(s) including any violations that were detected. Note that in the current version there is expected to be one
 * and only one validation result per event.
 */
@RestController
@RequestMapping("/services/validation-service/v1/app")
public interface ValidateService { // NOSONAR
    /**
     * Validate an event and, if successful, return the result(s) in JSON format. Note that not every event is validated
     * and so the set of results may be empty. In the event of an error/exception then plain text is returned containing
     * the exception details.
     *
     * <h4>The HTTP message body must be comprised of a single JSON object containing the following members (nested JSON
     * elements)</h4> <B>event-header</B> a JSON object which contains the following members:
     * <ul>
     * <li><B>domain</B> the value must match with the event domain expected by the Validation Service
     * <li><B>action</B> the value must not be present in the list of excluded event actions (default list: DELETE)</li>
     * <li><B>event-type</B> the value must match with one of the expected event types (either for rule-driven or for
     * model-driven validation)</li>
     * <li><B>top-entity-type</B> indicating the type of the entity member (see below)</li>
     * <li><B>entity-type</B> the value must match with an A&AI entity defined by the OXM model. This value identifies
     * the object to be validated</li>
     * <li><B>entity-link</B> the value indicates the source of the entity. This is expected to begin with the text
     * https://host/aai/vX/</li>
     * </ul>
     * <B> entity</B> a JSON object representing the top-level entity. This object must contain the single entity
     * instance to be validated, either
     * <ul>
     * <li>contained by a parent entity of a different type (indicated by top-entity-type)</li>
     * <li>or as a stand-alone JSON object (entity-type will have the same value as top-entity-type)</li>
     * </ul>
     *
     * @param event a JSON object representing an event
     * @return an HTTP Response containing either a JSON array of ValidationResult objects or a plain-text error message
     *
     * @responseMessage 200 Success
     * @responseMessage 400 Bad Request
     * @responseMessage 500 Internal Server Error
     *
     */
    @RequestMapping(value = "/validate", method = RequestMethod.POST, produces = {"application/json", "text/plain"})
    @ResponseBody
    public ResponseEntity<String> validate(@RequestHeader HttpHeaders headers, HttpServletRequest servletRequest,
            @RequestBody String event);

    public ResponseEntity<String> validate(String event);
}
