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
	type 'complex'
	validation {
		useRule {
			name 'CLLI'
			attributes 'physical-location-id'
		}
		useRule {
			name 'not AAI default'
			attributes 'street1'
		}
		useRule {
			name 'not AAI default'
			attributes 'city'
		}
		useRule {
			name 'not AAI default'
			attributes 'state'
		}
		useRule {
			name 'not AAI default'
			attributes 'postal-code'
		}
		useRule {
			name 'not AAI default'
			attributes 'region'
		}
		useRule {
			name 'not AAI default'
			attributes 'country'
		}
		useRule {
			name 'critical not AAI default'
			attributes 'physical-location-type'
		}
		useRule {
			name 'length five or null'
			attributes 'complex-name'
		}
		useRule {name 'complex is related to availability zone' }
		useRule {name 'complex is related to 1 oam-network' }
		useRule {
			name 'if a customer is related to an oam-network then oam-network.network-name must match naming convention'
			attributes 'relationship-list.relationship[*]'
		}
	}
}

rule {
	name        'CLLI'
	category    'FIELD_LENGTH'
	description 'Field must be 8 or 11 characters long'
	errorText   'Invalid length - field must be 8 or 11 characters long'
	severity    'CRITICAL'
	attributes  'field'
	validate    'field.size() == 8 || field.size() == 11'
}

rule {
	name        'not AAI default'
	category    'FIELD_LENGTH'
	description 'Invalid length - field must not be AAIDEFAULT or null'
	errorText   'Invalid Value - must not be AAIDEFAULT or null'
	severity    'MINOR'
	attributes  'field'
	validate    'field != null && field.size() > 0 && !field.equalsIgnoreCase("AAIDEFAULT")'
}

rule {
	name        'length five or null'
	category    'FIELD_LENGTH'
	description 'Field must be 5 characters long or null'
	errorText   'Invalid Length - field must be 5 characters long or null'
	severity    'MINOR'
	attributes  'field'
	validate    'field == null || field.size() == 5'
}

rule {
	name        'critical not AAI default'
	category    'INVALID_VALUE'
	description 'Field must not be AAIDEFAULT or null'
	errorText   'Invalid Value - must not be AAIDEFAULT or null'
	severity    'CRITICAL'
	attributes  'field'
	validate    'field != null && field.size() > 0 && !field.equalsIgnoreCase("AAIDEFAULT")'
}

rule {
	name        'complex is related to availability zone'
	category    'MISSING_REL'
	description 'Validates that a complex is related to an availability zone'
	errorText   'Missing relationship - a complex must be related to an availability zone'
	severity    'CRITICAL'
	attributes  'relationship-list.relationship[*].related-to'
	validate    'related-to != null && related-to.contains("availability-zone")'
}

rule {
	name        'complex is related to 1 oam-network'
	category    'MISSING_REL'
	description 'Validates that a complex is related to 1 oam-network (and not more than 1 oam-network)'
	errorText   'Missing relationship - complex must be related to 1 oam-network'
	severity    'MAJOR'
	attributes  'relationship-list.relationship[*].related-to'
	validate    'related-to != null && related-to.count("oam-network") == 1'
}

rule {
	name          'if a customer is related to an oam-network then oam-network.network-name must match naming convention'
	category      'INVALID_NAME'
	description   'validates that if a customer is related to an oam-network then oam-network.network-name must match naming convention'
	errorText     'Invalid name - if a customer is related to an oam-network then network-name must start with VLAN'
	severity      'MINOR'
	attributes    'relationships'
	validate      '''
                   def getStringProperty = { jsonObject, propertyName -> jsonObject.get(propertyName).getAsString() } 

				   relatedToOamNetwork = relationships.findAll { getStringProperty(it, "related-to") == "oam-network" }

	  	 		   networkNameIsValid = relationships.findAll    { getStringProperty(it, "related-to") == "oam-network" }
										             .collect    { it."related-to-property".get(0) }
									    			 .findAll    { getStringProperty(it, "property-key") == "oam-network.network-name" }
									    	 	     .find       { getStringProperty(it, "property-value").startsWith("VLAN") }
                	
					return !relatedToOamNetwork || networkNameIsValid
              '''
}
