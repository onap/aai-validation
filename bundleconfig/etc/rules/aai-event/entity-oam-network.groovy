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
	type 'oam-network'
	validation {
		useRule {
			name 'network-name value'
			attributes 'network-name'
		}
		useRule {
			name 'cvlan-tag equals last four digits of network-name'
			attributes 'cvlan-tag', 'network-name'
		}
		useRule {
			name 'valid_ipv4_oam_gw_addr'
			attributes 'ipv4-oam-gateway-address'
		}
		useRule {
			name 'ipv4_oam_gw_addr_prefix_length'
			attributes 'ipv4-oam-gateway-address-prefix-length'
		}
	}
}

rule {
	name        'network-name value'
	category    'INVALID_VALUE'
	description 'The value of network-name must be VLAN-OAM-1323 or VLAN-OAM-1321'
	errorText   'Invalid value - the value of network-name must be VLAN-OAM-1323 or VLAN-OAM-1321'
	severity    'MINOR'
	attributes  'name'
	validate    'name != null && name.matches("VLAN-OAM-1323|VLAN-OAM-1321")'
}

rule {
	name        'cvlan-tag equals last four digits of network-name'
	category    'INVALID_VALUE'
	description 'The value of cvlan-tag must match the last 4 digits of network-name'
	errorText   'Invalid value - the value of cvlan-tag must match the last 4 digits of network-name'
	severity    'MINOR'
	attributes  'cvlantag', 'networkname'
	validate    '''def lastFour = { str -> str ? str.drop(str.size() - 4) : null }
                      cvlantagStr=String.valueOf(cvlantag)
                      cvlantagStr.equals(lastFour(networkname))'''
}

rule {
	name        'valid_ipv4_oam_gw_addr'
	category    'INVALID_VALUE'
	description 'Validate an IPv4 address'
	errorText   'Invalid value - attribute is not a valid IPv4 address'
	severity    'MAJOR'
	attributes  'ipaddr'
	validate    'ipaddr != null && ipaddr.matches("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])")'
}

rule {
	name        'ipv4_oam_gw_addr_prefix_length'
	category    'INVALID_VALUE'
	description 'Invalid value - field value must be 26'
	errorText   'Invalid Value - field value must be 26'
	severity    'MAJOR'
	attributes  'field'
	validate    'field != null && field == 26'
}
