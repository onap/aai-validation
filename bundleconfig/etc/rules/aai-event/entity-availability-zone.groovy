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
	type 'availability-zone'
	validation {
		useRule {
			name 'availability-zone name matches naming convention'
			attributes 'availability-zone-name'
		}
		useRule {
			name 'hypervisor-type matches naming convention'
			attributes 'hypervisor-type'
		}
		useRule {
			name 'operational-state must be operationalState'
			attributes 'operational-state'
		}
		useRule { name 'availability zone is related to dvs-switch' }
		useRule { name 'availability zone is related to a complex' }
		useRule {
			name  'availability-zone must be related to a service-capability and service-capability.service-type matches naming convention'
			attributes 'relationship-list.relationship[*]'
		}
	}
}

rule {
	name        'availability-zone name matches naming convention'
	category    'INVALID_NAME'
	description 'Naming convention must match xxxxx-esx-aznn'
	errorText   'Invalid name - attribute must match xxxxx-esx-aznn (where x = alphanumeric and n = numeric)'
	severity    'MINOR'
	attributes  'name'
	validate    'name != null && name.matches("[a-z0-9]{5}-esx-az[0-9]{2}")'
}

rule {
	name        'hypervisor-type matches naming convention'
	category    'INVALID_NAME'
	description 'Naming convention must match the string esx'
	errorText   'Invalid name - attribute must match the string esx'
	severity    'CRITICAL'
	attributes  'name'
	validate    'name != null && name.matches("esx")'
}

rule {
	name        'operational-state must be operationalState'
	category    'INVALID_VALUE'
	description 'The value of operational-state must be operationalState'
	errorText   'Invalid value - attribute must be set to operationalState'
	severity    'CRITICAL'
	attributes  'opvalue'
	validate    'opvalue != null && opvalue.matches("operationalState")'
}

rule {
	name        'availability zone is related to dvs-switch'
	category    'MISSING_REL'
	description 'Validates that an availability zone is related to a dvs-switch'
	errorText   'Missing relationship - availability zone is not related to a dvs-switch'
	severity    'MAJOR'
	attributes  'relationship-list.relationship[*].related-to'
	validate    'related-to != null && related-to.contains("dvs-switch")'
}

rule {
	name        'availability zone is related to a complex'
	category    'MISSING_REL'
	description 'Validates that an availability zone is related to a complex'
	errorText   'Missing relationship - availability zone is not related to a complex'
	severity    'CRITICAL'
	attributes  'relationship-list.relationship[*].related-to'
	validate    'related-to != null && related-to.contains("complex")'
}

rule {
	name          'availability-zone must be related to a service-capability and service-capability.service-type matches naming convention'
	category      'INVALID_NAME'
	description   'Validates that an availability-zone is related to a service-capability and service-capability.service-type matches naming convention'
	errorText     'Invalid name - availability-zone must be related to a service-capability and service-capability.service-type must be set to SDN-ETHERNET-INTERNET'
	severity      'CRITICAL'
	attributes    'relationships'
	validate      '''
                  def getStringProperty = { jsonObject, propertyName -> jsonObject.get(propertyName)?.getAsString() } 

				  if (!relationships.find { getStringProperty(it, "related-to") == "service-capability" }) { return true }
	
				  return relationships.findAll { getStringProperty(it, "related-to") == "service-capability" }
                                      .findAll { it."related-to-property" != null }
									  .collect { it."relationship-data".get(0) }
					    			  .findAll { getStringProperty(it, "relationship-key") == "service-capability.service-type" }
					    	 	      .find    { getStringProperty(it, "relationship-value") == "SDN-ETHERNET-INTERNET" }
	              '''
}
