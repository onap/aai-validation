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
package org.onap.aai.validation.ruledriven.rule;

import groovy.lang.Tuple2;
import java.util.Collections;
import java.util.List;

/**
 * Encapsulates the results of rule execution
 *
 */
public class RuleResult {

    private Boolean success = true;
    private List<String> errorArguments = Collections.emptyList();

    /**
     * Creates an instance of this class using the groovy object returned by rule execution.
     *
     * Valid object types:
     *    Boolean: true = success; false = fail
     *    Number: 0 = success; non-zero = fail
     *    Tuple2: contains rule result and argument list
     *       - tuple's "first" contains a boolean representing the results of rule execution
     *       - tuple's "second" contains a list of strings used to expand rule error text
     *
     * @param groovyResult
     */
    public RuleResult(Object groovyResult) {
        if (groovyResult instanceof Number) {
            success = !((Number)groovyResult).equals(0);
        } else if (groovyResult instanceof Tuple2) {
            handleTuple(groovyResult);
        } else {
            success = (Boolean)groovyResult;
        }
    }

    @SuppressWarnings("unused")
    private RuleResult() {
        // intentionally empty
    }

    /**
     * Returns the results of rule execution, i.e. success or fail
     * @return
     */
    public Boolean getSuccess() {
        return success;
    }

    /**
     * Returns the list of arguments used to expand rule error text.
     *
     * For example, this errorText in a rule definition:
     *    'Error found with "{0}" in "{1}"; value "{2}" is not a valid MAC address'
     *
     * used with the following runtime argument list:
     *    ["macaddr", "tenants.tenant.vservers.vserver.l-interfaces.l-interface", "02:fd:59:3"]
     *
     * would display:
     *    Error found with "macaddr" in "tenants.tenant.vservers.vserver.l-interfaces.l-interface"; value "02:fd:59:3" is not a valid MAC address
     *
     * @return a list of strings; will not return null
     */
    public List<String> getErrorArguments() {
        return errorArguments;
    }

    /**
     * Handles a Tuple2 object returned by a groovy rule.
     * The tuple's "first" contains a boolean representing the results of rule execution.
     * The tuple's "second" contains a list of strings used to expand rule error text.
     * @param tupleObject
     */
    private void handleTuple(Object tupleObject) {
        @SuppressWarnings("unchecked")
        Tuple2<Boolean, List<String>> tuple = (Tuple2<Boolean, List<String>>)tupleObject;
        success = tuple.getFirst();
        errorArguments = (tuple.getSecond() == null) ? Collections.emptyList() : tuple.getSecond();
    }
}
