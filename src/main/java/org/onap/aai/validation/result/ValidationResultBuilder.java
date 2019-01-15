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
package org.onap.aai.validation.result;

import org.onap.aai.validation.exception.ValidationServiceException;
import org.onap.aai.validation.reader.EventReader;
import org.onap.aai.validation.reader.data.Entity;

public class ValidationResultBuilder {

    private final EventReader eventReader;
    private final String event;
    private Entity entity;

    public ValidationResultBuilder(EventReader eventReader, String event) {
        this.eventReader = eventReader;
        this.event = event;
    }

    public ValidationResultBuilder(Entity entity) {
        this.eventReader = null;
        this.event = null;
        this.entity = entity;
    }

    public static ValidationResult fromJson(String json) {
        return ValidationResultImpl.fromJson(json);
    }

    public ValidationResult build() throws ValidationServiceException {
        return new ValidationResultImpl(getEntity());
    }

    private Entity getEntity() throws ValidationServiceException {
        if (entity == null) {
            entity = eventReader.getEntity(event);
        }
        return entity;
    }
}
