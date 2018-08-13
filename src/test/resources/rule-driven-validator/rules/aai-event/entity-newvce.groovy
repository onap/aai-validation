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
	type 'newvce'
	validation {
		useRule {
			name 'prov-status'
			attributes 'prov-status'
		}
		useRule {
			name 'vnf-name'
			attributes 'vnf-name'
		}
		useRule {
			name 'vnf-type'
			attributes 'vnf-type'
		}
		useRule {
			name 'valid_ipv4_addr'
			attributes 'ipv4-oam-address'
		}
		useRule {
			name 'heat-stack-id equals first 11 bytes of vnf-name'
			attributes 'heat-stack-id', 'vnf-name'
		}
	}
}
