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

rule {
	name        'prov-status'
	category    'INVALID_VALUE'
	description 'prov-status value restricted to one of PREPROV, NVTPROV, PROV, CAPPED, DECOM, RETIRED'
	errorText   'Invalid prov-status value. Must be PREPROV, NVTPROV, PROV, CAPPED, DECOM, or RETIRED'
	severity    'CRITICAL'
	attributes  'status'
	validate    '''switch (status) {
                           case "PREPROV":
                           case "NVTPROV":
                           case "PROV":
                           case "CAPPED":
                           case "DECOM":
                           case "RETIRED":
                                         return true
                           default: return false
                }'''
}

// The following are used by both vce and newvce

rule {
	name        'vnf-name'
	category    'INVALID_NAME'
	description 'Invalid naming convention'
	errorText   'Invalid name - attribute does not match xxxxxnnnvbc (where x = alphanumeric and n = numeric)'
	severity    'MINOR'
	attributes  'name'
	validate    'name != null && name.matches("[a-z,0-9]{5}[0-9]{3}vbc")'
}

rule {
	name        'vnf-type'
	category    'INVALID_VALUE'
	description 'Invalid value'
	errorText   'Invalid value - attribute must equal esx-vce'
	severity    'MINOR'
	attributes  'name'
	validate    'name != null && name.matches("esx-vce")'
}

rule {
	name        'heat-stack-id equals first 11 bytes of vnf-name'
	category    'INVALID_VALUE'
	description 'The value of heat-stack-id must equal the first 11 bytes of vnf-name'
	errorText   'Invalid value - the value of heat-stack-id must equal the first 11 bytes of vnf-name'
	severity    'MINOR'
	attributes  'heatstackid', 'vnfname'
	validate    '''def firstEleven = { str -> str ? str.take(11) : null }
                   heatstackid.equals(firstEleven(vnfname))'''
}
