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
		useRule {
			name 'prov-status'
			attributes 'prov-status'
		}
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
