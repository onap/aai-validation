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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.text.MessageFormat;
import org.onap.aai.validation.reader.data.AttributeValues;

public class RuleHelper {

    static void assertRuleResult(Rule rule, AttributeValues values, Boolean expectedResult) {
        assertThat(rule + " failed for values [" + values + "]", rule.execute(values).getSuccess(),
                is(equalTo(expectedResult)));
    }

    static void assertRuleResult(Rule rule, Object value, Boolean expectedResult) {
        assertThat(rule + " failed for value [" + value + "]", rule.execute(value).getSuccess(),
                is(equalTo(expectedResult)));
    }

    static void assertRuleErrorMessage(Rule rule, Object value, String expectedErrorMessage) {
        RuleResult result = rule.execute(value);
        String errorMessage = MessageFormat.format(rule.getErrorMessage(), result.getErrorArguments().toArray());
        assertThat(rule + " failed to validate error message [" + expectedErrorMessage + "]", errorMessage,
                is(equalTo(expectedErrorMessage)));
    }
}
