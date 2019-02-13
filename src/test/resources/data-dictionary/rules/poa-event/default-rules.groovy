/*
 * ============LICENSE_START===================================================
 * Copyright (c) 2018-2019 European Software Marketing Ltd.
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

entity {
    name 'POA-EVENT'
    indexing { indices 'default-rules' }
    validation {
        useRule {
            name       'Data-Dictionary validate VF type'
            attributes 'vnfList[*].vfModuleList[*].networkList[*].type'
        }
    }
}

rule {
    name        'Data-Dictionary validate VF type'
    category    'INVALID_VALUE'
    description 'Validate all VF type values against data-dictionary'
    errorText   'VF type [{0}] failed data-dictionary validation: {1}'
    severity    'ERROR'
    attributes  'typeList'
    validate    '''
        List<String> details = new ArrayList<>()
        typeList.each {
            def result = org.onap.aai.validation.ruledriven.rule.builtin.DataDictionary.validate("instance", "vfModuleNetworkType", "type", "$it")
            if (!result.isEmpty()) {
                details.add("$it")
                details.add("$result")
            }
        }
        return new Tuple2(details.isEmpty(), details)
        '''
}
