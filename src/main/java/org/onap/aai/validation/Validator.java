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
package org.onap.aai.validation;

import java.util.List;
import org.onap.aai.validation.exception.ValidationServiceException;
import org.onap.aai.validation.result.ValidationResult;

/**
 * Validator (e.g. model-driven or rule-based)
 *
 */
public interface Validator {

    /**
     * This method should be called (once) before validate() to ensure that all configuration is correctly loaded.
     * 
     * @throws ValidationServiceException
     */
    public void initialise() throws ValidationServiceException;

    /**
     * Validate the entity or entities found in the event.
     *
     * @param event JSON containing the entity or entities to validate
     * @return a list of validation results
     * @throws ValidationServiceException
     */
    public List<ValidationResult> validate(String event) throws ValidationServiceException;
}
