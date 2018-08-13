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
	type 'vserver'
	validation {
		useRule {name 'vserver is related to 0 or 1 image' }
		useRule {name 'vserver is related to 0 or 1 flavor' }
		useRule {name 'vserver is related to 1 pserver' }
		useRule {name 'vserver is related to vpe and vserver-name matches naming convention' }
		useRule {name 'vserver is related to vce and vserver-name matches naming convention' }
		useRule {
			name 'vserver related to TRINITY image and generic-vnf.vnf-name matches naming convention'
			attributes 'relationship-list.relationship[*]'
		}
		useRule {
			name  'vserver is related to a TRINITY image and vserver-name matches naming convention'
			attributes 'relationship-list.relationship[*]', 'vserver-name'
		}
		useRule {
			name  'vserver is related to a vnf (vce or newvce or vpe or generic-vnf)'
			attributes 'relationship-list.relationship[*].related-to'
		}
	}
}

rule {
	name        'vserver related to TRINITY image and generic-vnf.vnf-name matches naming convention'
	category    'INVALID_NAME'
	description 'Validates that if vserver is related to an image named TRINITY, then the related generic-vnf name matches naming convention'
	errorText   'Invalid name - if vserver is related to an image named TRINITY, then the related generic-vnf name must match xxxxnnnnv (where x = character and n = number)'
	severity    'MINOR'
	attributes  'relationships'
	validate    '''
                     def getStringProperty = { jsonObject, propertyName -> jsonObject.get(propertyName)?.getAsString() } 

					 vnf_name = relationships.findAll    { getStringProperty(it, "related-to") == "generic-vnf" }
                                             .findAll    { it."related-to-property" != null }
                                             .collect    { it."related-to-property".get(0) }
						    				 .find       { getStringProperty(it, "property-key") == "generic-vnf.vnf-name" }
										     .findResult { getStringProperty(it, "property-value") }

	  	 			 relatedToTrinity = relationships.findAll    { getStringProperty(it, "related-to") == "image" }
													 .findAll    { it."related-to-property" != null }
										             .collect    { it."related-to-property".get(0) }
									    			 .findAll    { getStringProperty(it, "property-key") == "image.image-name" }
									    	 	     .find       { getStringProperty(it, "property-value")?.startsWith("TRINITY") }

                     // If (and only if) related to TRINITY then check the generic-vnf name
                     return !relatedToTrinity || vnf_name?.matches("[a-z]{4}[0-9]{4}v")
                     '''
}

rule {
	name        'vserver is related to a vnf (vce or newvce or vpe or generic-vnf)'
	category    'MISSING_REL'
	description 'Validates that a vserver is related to a vnf (vce or newvce or vpe or generic-vnf)'
	errorText   'Missing relationship - a vserver must be related to a vnf (vce or newvce or vpe or generic-vnf)'
	severity    'MINOR'
	attributes  'related-to'
	validate    'related-to != null && (related-to.contains("vce") || related-to.contains("newvce") || related-to.contains("vpe") || related-to.contains("generic-vnf"))'
}

rule {
	name        'vserver is related to 1 pserver'
	category    'MISSING_REL'
	description 'Validates that a vserver is related to 1 pserver (and not more than 1 pserver)'
	errorText   'Missing relationship - vserver must be related to 1 pserver'
	severity    'MINOR'
	attributes  'relationship-list.relationship[*].related-to'
	validate    'related-to != null && related-to.count("pserver") == 1'
}

rule {
	name        'vserver is related to 0 or 1 image'
	category    'MISSING_REL'
	description 'Validates that a vserver is either not related to an image or related to only 1 image'
	errorText   'Missing relationship - vserver must be related to 0 or 1 image'
	severity    'MINOR'
	attributes  'relationship-list.relationship[*].related-to'
	validate    'related-to == null || related-to.count("image") <= 1'
}

rule {
	name        'vserver is related to 0 or 1 flavor'
	category    'MISSING_REL'
	description 'Validates that a vserver is either not related to a flavor or related to only 1 flavor'
	errorText   'Missing relationship - vserver must be related to 0 or 1 flavor'
	severity    'MINOR'
	attributes  'relationship-list.relationship[*].related-to'
	validate    'related-to == null || related-to.count("flavor") <= 1'
}

rule {
	name        'vserver is related to vpe and vserver-name matches naming convention'
	category    'INVALID_NAME'
	description 'Validates that if a vserver is related to a vpe then vserver-name must contain me6'
	errorText   'Invalid name - if vserver is related to vpe then vserver-name must contain me6'
	severity    'MINOR'
	attributes  'relationship-list.relationship[*].related-to', 'vserver-name'
	validate    '!related-to.contains("vpe") || vserver-name =~ "me6"'
}

rule {
	name        'vserver is related to vce and vserver-name matches naming convention'
	category    'INVALID_NAME'
	description 'Validates that if a vserver is related to a vce then vserver-name must match naming convention'
	errorText   'Invalid name - if vserver is related to vce then vserver-name must match xxxxxxxxvbcnnceb (where x = alphanumeric and n = numeric)'
	severity    'MINOR'
	attributes  'relationship-list.relationship[*].related-to', 'vserver-name'
	validate    '!related-to.contains("vce") || vserver-name =~ "[a-z0-9]{8}vbc[0-9]{2}ceb"'
}



rule {
	name          'vserver is related to a TRINITY image and vserver-name matches naming convention'
	category      'INVALID_NAME'
	description   'Validates that if vserver is related to an image named TRINITY, then the vserver name matches naming convention'
	errorText     'Invalid name - if vserver is related to an image named TRINITY, then the vserver name must match xxxxnnnnvmnnn (where x = character and n = number)'
	severity      'MINOR'
	attributes    'relationships', 'vservername'
	validate      '''
                  def getStringProperty = { jsonObject, propertyName -> jsonObject.get(propertyName)?.getAsString() } 

   	 			  relatedToTrinity = relationships.findAll    { getStringProperty(it, "related-to") == "image" }
												  .findAll    { it."related-to-property" != null }
												  .collect    { it."related-to-property".get(0) }
								    			  .findAll    { getStringProperty(it, "property-key") == "image.image-name" }
								    	 	      .find       { getStringProperty(it, "property-value").startsWith("TRINITY") }

	              // If (and only if) related to TRINITY then check the vserver name
	              return !relatedToTrinity || vservername != null && vservername ==~ "[a-z]{4}[0-9]{4}vm[0-9]{3}"
	              '''
}

