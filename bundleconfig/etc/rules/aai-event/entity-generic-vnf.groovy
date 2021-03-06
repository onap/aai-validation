/*
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
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

entity {
	type 'generic-vnf'
	validation {
		useRule {
			name 'prov-status'
			attributes 'prov-status'
		}
		useRule {
			name 'valid_ipv4_addr'
			attributes 'ipv4-oam-address'
		}
		useRule {
			name 'ipv4_addr_present'
			attributes 'equipment-role', 'l-interfaces.l-interface[*].l3-interface-ipv4-address-list'
		}
	}
}

// If generic-vnf.equipment-role="UCPE" and there is an l-interface - then there must be an IPV4 address related to the l-interface
rule {
	name        'ipv4_addr_present'
	category    'MISSING_REL'
	description 'Validates that ICPE equipment has a related IPv4 address'
	errorText   'UCPE l-interface missing the IPv4 relationship'
	severity    'MINOR'
	attributes  'equipment', 'ipv4'
	validate    'equipment != "UCPE" || ipv4 != null'
}
