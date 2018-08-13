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

entity {
	type 'pserver'
	validation {
		useRule {
			name 'equip-type'
			attributes 'equip-type'
		}
		useRule {name 'pserver is related to 1 complex' }
		useRule {
			name 'pserver inv-status attribute allowed values'
			attributes 'inv-status'
		}
	}
}

rule {
	name        'equip-type'
	category    'INVALID_VALUE'
	description 'Invalid value - equip-type must not be toa or hitachi - an empty value is ok'
	errorText   'Invalid value - attribute must not have a value of toa or hitachi'
	severity    'MINOR'
	validate    '''     equip-type == null ||
                       (!equip-type.equalsIgnoreCase("toa") &&
                        !equip-type.equalsIgnoreCase("hitachi"))
                '''
}

rule {
	name        'pserver is related to 1 complex'
	category    'MISSING_REL'
	description 'Validates that a pserver is related to 1 complex'
	errorText   'Missing relationship - pserver must be related to 1 complex'
	severity    'MINOR'
	attributes  'relationship-list.relationship[*].related-to'
	validate    'related-to != null && related-to.count("complex") == 1'
}

rule {
	name        'pserver inv-status attribute allowed values'
	category    'INVALID_VALUE'
	description 'inv-status value restricted to one of Deployed, In Service, Not Specified, Pending Delete, Planned, Planned Modify'
	errorText   'Invalid inv-status value. Must be Deployed, In Service, Not Specified, Pending Delete, Planned, Planned Modify'
	severity    'CRITICAL'
	attributes  'status'
	validate    '''switch (status) {
						   case null:
                           case "Deployed":
                           case "In Service":
                           case "Not Specified":
                           case "Pending Delete":
                           case "Planned":
                           case "Planned Modify":
                                    return true
                           default: return false
                }'''
}
