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
	name             'vserver related to TRINITY image and generic-vnf.vnf-name matches naming convention'
	category         'INVALID_NAME'
	description      'Validates that if vserver is related to an image named TRINITY, then the related generic-vnf name matches naming convention'
	errorText        'Invalid name - if vserver is related to an image named TRINITY, then the related generic-vnf name must match xxxxnnnnv (where x = character and n = number)'
	severity         'MINOR'
	attributes       'relationships'
	validate         '''
                     def getStringProperty = { jsonObject, propertyName -> jsonObject.get(propertyName).getAsString() } 

					 vnf_name = relationships.findAll    { getStringProperty(it, "related-to") == "generic-vnf" }
                                             .collect    { it."related-to-property".get(0) }
						    				 .find       { getStringProperty(it, "property-key") == "generic-vnf.vnf-name" }
										     .findResult { getStringProperty(it, "property-value") }

	  	 			 relatedToTrinity = relationships.findAll    { getStringProperty(it, "related-to") == "image" }
										             .collect    { it."related-to-property".get(0) }
									    			 .findAll    { getStringProperty(it, "property-key") == "image.image-name" }
									    	 	     .find       { getStringProperty(it, "property-value").startsWith("TRINITY") }
                	
                     // If (and only if) related to TRINITY then check the generic-vnf name
                     return !relatedToTrinity || vnf_name?.matches("[a-z]{4}[0-9]{4}v")
                     '''
}