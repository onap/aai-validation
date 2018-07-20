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
	type 'vce'
	validation {
		useRule {
			name       'prov-status'
			attributes 'prov-status'
		}
		useRule {
			name       'uppercase_alpha'
			attributes 'vnf-name2'
		}
		useRule {
			name       'vnf-name'
			attributes 'vnf-name'
		}
		useRule {
			name       'vnf-type'
			attributes 'vnf-type'
		}
		useRule {
			name       'vce.vpe-id'
			attributes 'vpe-id'
		}
		useRule {
			name       'valid_ipv4_addr'
			attributes 'ipv4-oam-address'
		}
		useRule { name 'heat-stack-id' }
		useRule { name 'vce is related to vserver and service and complex' }
		useRule { name 'vce is related to an availability zone' }
		useRule {
			name       'heat-stack-id equals first 11 bytes of vnf-name'
			attributes 'heat-stack-id', 'vnf-name'
		}
	}
}

rule {
	name        'uppercase_alpha'
	category    'INVALID_NAME'
	description 'naming convention is UPPERCASE (alphanumeric)'
	errorText   'Invalid name - the attribute must be UPPERCASE (alphanumeric)'
	severity    'MINOR'
	attributes  'name'
	validate    'name != null && name.matches("[A-Z,0-9, ]+")'
}

rule {
	name        'vce.vpe-id'
	category    'INVALID_NAME'
	description 'Naming convention must start with VPESAT and end with lowercase 401me6'
	errorText   'Invalid name - attribute must start with VPESAT and end with lowercase 401me6'
	severity    'MINOR'
	attributes  'name'
	validate    'name != null && name.matches("^VPESAT\\\\.*401me6")'
}

rule {
	name        'vce is related to vserver and service and complex'
	category    'MISSING_REL'
	description 'Validates that a vce is related to a vserver and service-instance and complex'
	errorText   'Missing relationship - vce is not related to a vserver and service-instance and complex'
	severity    'MAJOR'
	attributes  'relationship-list.relationship[*].related-to'
	validate    'related-to != null && related-to.contains("vserver") && related-to.contains("service-instance") && related-to.contains("complex")'
}

rule {
	name        'vce is related to an availability zone'
	category    'MISSING_REL'
	description 'Validates that a vce is related to an availability zone'
	errorText   'Missing relationship - vce is not related to an availability zone'
	severity    'MAJOR'
	attributes  'relationship-list.relationship[*].related-to'
	validate    'related-to != null && related-to.contains("availability-zone")'
}
