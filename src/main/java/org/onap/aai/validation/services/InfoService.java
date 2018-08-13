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

import org.onap.aai.validation.controller.ValidationController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Information Service for the Validation Controller. Return status details to the caller.
 * 
 */
@RestController
@RequestMapping("/services/validation-service/v1/core/core-service")
public class InfoService {

    @Autowired
    private ValidationController validationController;

    public ValidationController getValidationController() {
        return validationController;
    }

    public void setValidationController(ValidationController validationController) {
        this.validationController = validationController;
    }

    /**
     * @param format is an optional setting - html requests an HTML format
     * @return a formatted status report
     */
    @RequestMapping(value = "/info", method = RequestMethod.GET, produces = "text/plain")
    @ResponseBody
    public String getInfo() {
        validationController.incrementInfoCount();
        return "Status: Up\n" + validationController.statusReport().toString() + "\n";
    }

}
