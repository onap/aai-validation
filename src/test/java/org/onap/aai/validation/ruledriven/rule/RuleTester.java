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

import org.onap.aai.validation.reader.data.AttributeValues;

/**
 * Helper class for testing rules.
 *
 */
public class RuleTester {

    private Rule rule;
    private AttributeValues attributeValues;

    /**
     * @param trinityRule
     * @param attributeValues
     */
    public RuleTester(Rule trinityRule, AttributeValues attributeValues) {
        this.rule = trinityRule;
        this.attributeValues = attributeValues;
    }

    /**
     * @param expectedResult
     */
    public void test(boolean expectedResult) {
        RuleHelper.assertRuleResult(rule, attributeValues, expectedResult);
    }
}
