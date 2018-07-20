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
	type 'port-group'
	validation { 
		useRule {  
			name 'heat-stack-id' 
		} 
	}
}

/*
 * This rule validates the heat-stack-id attribute value based on the corresponding orchestration-status attribute value.
 * If the orchestration-status is "created", then the heat-stack-id cannot be null or empty, or equal to "heat123".
 * If the orchestration-status is "pending create", then the heat-stack-id must be null or empty, or equal to "heat123".
 */
rule {
	name        'heat-stack-id'
	category    'DEPENDENCY_ERR'
	description 'Validates that the heat-stack-id value is valid for various Orchestration Status values'
	errorText   'The heat-stack-id value is invalid for the current orchestration-status.'
	severity    'CRITICAL'
	attributes  'orchestration-status', 'heat-stack-id'
	validate    '''switch (orchestration-status?.toLowerCase()) {
                           case "created":        return !(heat-stack-id in [null, "", "heat123"])
                           case "pending create": return   heat-stack-id in [null, "", "heat123"]
                           default: orchestration-status != null // true
                   }'''
}
