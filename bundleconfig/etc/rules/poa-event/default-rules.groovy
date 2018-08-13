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
    name 'POA-EVENT'
    indexing {
        indices 'default-rules'
    }
	validation {
		useRule {
			name 'Verify AAI nf-naming-code'
			attributes 'context-list.aai.vf-list[*]'
		}
		useRule {
			name 'port-mirroring-AAI-has-valid-vnfc'
			attributes 'context-list.sdc.vf-list[*]', 'context-list.aai.vf-list[*]'
		}
		useRule {
			name 'port-mirroring-SDC-vnfc-types-missing'
			attributes 'context-list.sdc.vf-list[*]', 'context-list.aai.vf-list[*]'
		}
		useRule {
			name 'port-mirroring-AAI-vnfc-type-exists-in-SDC-SUCCESS'
			attributes 'context-list.sdc.vf-list[*]', 'context-list.aai.vf-list[*]'
		}
	}
}

rule {
	name        'Verify AAI nf-naming-code'
	category    'INVALID_VALUE'
	description 'Validate that nf-naming-code exists and is populated in AAI VNF instance'
	errorText   'The nf-naming-code is not populated in AAI VNF instance'
	severity    'CRITICAL'
	attributes  'vfList'
	validate    '''
				def parsed = new groovy.json.JsonSlurper().parseText(vfList.toString())
				for (vf in parsed) {
					String nfNamingCode = vf."nf-naming-code"
					if (nfNamingCode == null || nfNamingCode.equals("")) {
						return false
					}
				}
				return true
                '''
}

rule {
	name        'port-mirroring-AAI-has-valid-vnfc'
	category    'INVALID_VALUE'
	description 'Validate that each VNFC instance in AAI conforms to a VNFC type defined in SDC model'
	errorText   'AAI VNFC instance includes non-specified type in design SDC model'
	severity    'ERROR'
	attributes  'sdcVfList', 'aaiVfList'
	validate    '''
				def slurper = new groovy.json.JsonSlurper()
				def parsedSdc = slurper.parseText(sdcVfList.toString())
				def parsedAai = slurper.parseText(aaiVfList.toString())

				// gather all SDC nfc-naming-codes
				List<String> sdcNfcNamingCodeList = new ArrayList<>()
				parsedSdc.each {
					for(sdcVnfc in it.vnfc) {
						String sdcNfcNamingCode = sdcVnfc."nfc-naming-code"
						if(sdcNfcNamingCode != null) {
							sdcNfcNamingCodeList.add(sdcNfcNamingCode)
						}
					}
				}

				// check that all SDC nfc-naming-codes exist in AAI
				parsedAai.each {
					for(aaiVnfc in it.vnfc) {
						String aaiNfcNamingCode = aaiVnfc."nfc-naming-code"
						if(aaiNfcNamingCode != null) {
							if(!sdcNfcNamingCodeList.contains(aaiNfcNamingCode)) {
								return false
							}
						}
					}
				}
				return true
                '''
}


rule {
	name        'port-mirroring-SDC-vnfc-types-missing'
	category    'INVALID_VALUE'
	description 'Validate that each VNFC type specified in SDC model exists in AAI'
	errorText   'Design has specified types but not all of them exist in AAI'
	severity    'WARNING'
	attributes  'sdcVfList', 'aaiVfList'
	validate    '''
				def getNfcNamingCodeSet = { parsedEntity ->
					Set<String> namingCodeSet = new HashSet<>()
					parsedEntity.each {
						for(vnfcItem in it."vnfc") {
							println "vnfc: " + vnfcItem
							String namingCode = vnfcItem."nfc-naming-code"
							if(namingCode != null) {
								namingCodeSet.add(namingCode)
							}
						}
					}
					return namingCodeSet
				}

				// gather all unique nfc-naming-codes from AAI and SDC
				def slurper = new groovy.json.JsonSlurper()
				def aaiNfcNamingCodeSet = getNfcNamingCodeSet(slurper.parseText(aaiVfList.toString())) as java.util.HashSet
				def sdcNfcNamingCodeSet = getNfcNamingCodeSet(slurper.parseText(sdcVfList.toString())) as java.util.HashSet
				
				println "AAI: " + aaiNfcNamingCodeSet
				println "SDC: " + sdcNfcNamingCodeSet

				// check that all nfc-naming-codes in SDC exist in AAI
				return aaiNfcNamingCodeSet.containsAll(sdcNfcNamingCodeSet)
                '''
}


rule {
	name        'port-mirroring-AAI-vnfc-type-exists-in-SDC-SUCCESS'
	category    'SUCCESS'
	description 'Verify that every vnfc in sdc has been created in AAI'
	errorText   'Every vnfc type specified in sdc has been created in AAI'
	severity    'INFO'
	attributes  'sdcVfList', 'aaiVfList'
	validate    '''
				def getNfcNamingCodeSet = { parsedEntity ->
					Set<String> namingCodeSet = new HashSet<>()
					parsedEntity.each {
						for(vnfcItem in it."vnfc") {
							String namingCode = vnfcItem."nfc-naming-code"
							if(namingCode != null) {
								namingCodeSet.add(namingCode)
							}
						}
					}
					return namingCodeSet
				}

				// gather all unique nfc-naming-codes from AAI and SDC
				def slurper = new groovy.json.JsonSlurper()
				def aaiNfcNamingCodeSet = getNfcNamingCodeSet(slurper.parseText(aaiVfList.toString())) as java.util.HashSet
				def sdcNfcNamingCodeSet = getNfcNamingCodeSet(slurper.parseText(sdcVfList.toString())) as java.util.HashSet

				// check that all nfc-naming-codes in SDC exist in AAI
				// return false if all SDC naming codes exist in AAI to trigger an INFO violation
				return !aaiNfcNamingCodeSet.containsAll(sdcNfcNamingCodeSet)
				'''
}
