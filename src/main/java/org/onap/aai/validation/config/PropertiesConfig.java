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
package org.onap.aai.validation.config;

import java.text.MessageFormat;

/**
 * Base properties configuration class.
 */
public class PropertiesConfig {

    /**
     * Replaces place-holders in property values.
     *
     * @param s
     *        a string with place-holders in the form {n}
     * @param args
     *        values for place-holders
     * @return a formated String with replaced place-holders.
     */
    public String formatter(String s, Object... args) {
        MessageFormat formatter = new MessageFormat("");
        formatter.applyPattern(s);
        return formatter.format(args);
    }
}
