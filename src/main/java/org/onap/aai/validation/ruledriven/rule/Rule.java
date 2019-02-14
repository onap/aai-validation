/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright (c) 2018-2019 AT&T Intellectual Property. All rights reserved.
 * Copyright (c) 2018-2019 European Software Marketing Ltd.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */
package org.onap.aai.validation.ruledriven.rule;

import java.util.List;
import org.onap.aai.validation.reader.data.AttributeValues;

/**
 * A rule that accepts one or more attributes and returns a Boolean result (when executed)
 */
public interface Rule {

    /**
     * Gets the name of the rule
     *
     * @return the name
     */
    String getName();

    /**
     * Gets the error message.
     *
     * @return the error message
     */
    String getErrorMessage();

    /**
     * Gets the error category.
     *
     * @return the error category
     */
    String getErrorCategory();

    /**
     * Gets the severity.
     *
     * @return the severity
     */
    String getSeverity();

    /**
     * Gets the paths to the attributes to pass to the rule
     *
     * @return the attribute paths
     */
    List<String> getAttributePaths();

    /**
     * Execute the rule.
     *
     * @param values
     *            the attribute values to pass to the rule
     * @return a RuleResult instance representing the rule evaluation (meaning success/failure)
     */
    RuleResult execute(AttributeValues values);

    /**
     * Execute the rule.
     *
     * @param values
     *            the attribute values to pass to the rule
     * @return a RuleResult instance representing the rule evaluation (meaning success/failure)
     */
    RuleResult execute(Object... values);

}
