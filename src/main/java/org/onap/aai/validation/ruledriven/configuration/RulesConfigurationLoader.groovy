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
package org.onap.aai.validation.ruledriven.configuration

import groovy.lang.Closure
import groovy.lang.DelegatesTo
import groovy.lang.ExpandoMetaClass
import java.io.File
import java.util.List
import org.onap.aai.validation.ruledriven.RuleManager

class RulesConfigurationLoader {

	static RuleManager loadConfiguration(File dsl) {
		return loadConfiguration(dsl.text)
	}

	static RuleManager loadConfiguration(String dsl) throws GroovyConfigurationException {
		SettingsSection globalConfiguration = new SettingsSection()
		def List<EntitySection> entities = []
		def List<RuleSection> rules = []
		Script dslScript

		try {
			dslScript = new GroovyShell().parse(dsl)
		} catch (org.codehaus.groovy.control.MultipleCompilationErrorsException e) {
			throw new GroovyConfigurationException(e, dsl)
		}

		dslScript.metaClass = createEMC(dslScript.class, { ExpandoMetaClass emc ->

			emc.settings = { Closure cl ->
				cl.delegate = new SettingsDelegate(globalConfiguration)
				cl.resolveStrategy = Closure.DELEGATE_FIRST
				cl()
			}

			emc.entity = { Closure cl ->
				EntitySection entityConfiguration = new EntitySection()
				cl.delegate = new EntityDelegate(entityConfiguration)
				cl.resolveStrategy = Closure.DELEGATE_FIRST
				cl()
				entities.add(entityConfiguration)
			}

			emc.rule = { Closure cl ->
				RuleSection ruleConfiguration = new RuleSection()
				cl.delegate = new RuleDelegate(ruleConfiguration)
				cl.resolveStrategy = Closure.DELEGATE_FIRST
				cl()
				rules.add(ruleConfiguration)
			}
		})

		try {
			dslScript.run()
		} catch (MissingMethodException | MissingPropertyException e) {
			throw new GroovyConfigurationException(e, dsl)
		}

		loadGenericRules(entities, rules)
		checkForDuplicateRules(rules)

		return new RuleManager(entities)
	}

	static void loadGenericRules(List<EntitySection> entities, List<RuleSection> rules) {
		for (entity in entities) {
			for (rule in entity.getRules()) {
				if (rule.isGeneric()) {
					def namedRule = rules.find() { item ->
						item.getName() == rule.getName()
					}
					if (namedRule == null) {
						throw new GroovyConfigurationException("rule '" + rule.getName() + "' is not defined")
					}
					try {
						rule.copyFrom(namedRule)
					} catch (IllegalArgumentException e) {
						throw new GroovyConfigurationException("rule '" + rule.getName() + "' has no attributes defined, referenced by entity '" + entity.getName() + "'")
					}
				}
				if (rule.getExpression() == null) {
					throw new GroovyConfigurationException("rule '" + rule.getName() + "' does not have an expression defined")
				}
			}
		}
	}

	static void checkForDuplicateRules(List<RuleSection> rules) {
		def duplicates = rules.countBy{ rule -> rule.name }.grep{ it.value > 1 }.collect{ it.key }

		rules.each { rule ->
			if (rule.name in duplicates) {
				throw new GroovyConfigurationException("Generic rule '" + rule.name + "' is duplicated")
			}
		}
	}

	static ExpandoMetaClass createEMC(Class scriptClass, Closure cl) {
		ExpandoMetaClass emc = new ExpandoMetaClass(scriptClass, false)
		cl(emc)
		emc.initialize()
		return emc
	}
}

// Parse the settings {} block
class SettingsDelegate {
	private SettingsSection configuration

	SettingsDelegate(SettingsSection configuration) {
		this.configuration = configuration
	}

	void environment(String environment) {
		this.configuration.setEnvironment environment
	}
}

// Parse an entity {} block
class EntityDelegate {
	private EntitySection configuration

	EntityDelegate(EntitySection configuration) {
		this.configuration = configuration
	}

	void name(String name) {
		this.configuration.setName name
	}

	void type(String name) {
		if (!configuration.name) configuration.name = name
		configuration.type = name
		configuration.getRules().each { rule ->
			rule.setObject configuration.type
		}
	}

    def indexing(@DelegatesTo(strategy=Closure.DELEGATE_FIRST, value=IndexingDelegate) Closure cl) {
        cl.delegate = new IndexingDelegate(configuration)
        cl.resolveStrategy = Closure.DELEGATE_FIRST
        cl()
    }

	def validation(@DelegatesTo(strategy=Closure.DELEGATE_FIRST, value=ValidationDelegate) Closure cl) {
		cl.delegate = new ValidationDelegate(configuration)
		cl.resolveStrategy = Closure.DELEGATE_FIRST
		cl()
	}

	void methodMissing(String name, Object args) {
		throw new MissingMethodException(name, this.class, args as Object[])
	}

	def propertyMissing(String name) {
		throw new MissingMethodException(name, this.class)
	}
}


// Parse an indexing {} block within an entity block
class IndexingDelegate {
    private EntitySection configuration

    IndexingDelegate(EntitySection configuration) {
        this.configuration = configuration
    }

    void indices(String... indices) {
        this.configuration.setIndices indices
        def index = RuleManager.generateKey(indices)
        this.configuration.type = index
        this.configuration.getRules().each { rule ->
            rule.setObject configuration.type
        }
    }

    void methodMissing(String name, Object args) {
        throw new MissingMethodException(name, this.class, args as Object[])
    }

    def propertyMissing(String name) {
        throw new MissingMethodException(name, this.class)
    }
}

// Parse a validation {} block within an entity block
class ValidationDelegate {
	private EntitySection configuration
	private String id

	ValidationDelegate(EntitySection configuration) {
		this.configuration = configuration
	}

	void useRule(@DelegatesTo(strategy=Closure.DELEGATE_FIRST, value=UseRuleDelegate) Closure cl) {
		cl.delegate = new UseRuleDelegate(configuration, configuration.type, id)
		cl.resolveStrategy = Closure.DELEGATE_FIRST
		cl()
	}

	void rule(@DelegatesTo(strategy=Closure.DELEGATE_FIRST, value=RuleDelegate) Closure cl) {
		RuleSection ruleConfiguration = new RuleSection()
		ruleConfiguration.setObject configuration.type
		ruleConfiguration.setObjectId this.id
		cl.delegate = new RuleDelegate(ruleConfiguration)
		cl.resolveStrategy = Closure.DELEGATE_FIRST
		cl()
		configuration.addRule(ruleConfiguration)
	}

	void methodMissing(String name, Object args) {
		throw new MissingMethodException(name, this.class, args as Object[])
	}
}

// Parse a rule {} block
class RuleDelegate {
	private RuleSection configuration

	RuleDelegate(RuleSection configuration) {
		this.configuration = configuration
	}

	void name(String name) {
		this.configuration.setName name
	}

	void category(String category) {
		this.configuration.setCategory category
	}

	void description(String description) {
		this.configuration.setDescription description
	}

	void errorText(String text) {
		this.configuration.setErrorMessage text
	}

	void severity(String severity) {
		this.configuration.setSeverity severity
	}

	void attributes(String... attributesList) {
		this.configuration.setAttributes attributesList
	}

	void validate(String validate) {
		this.configuration.setExpression validate
	}

	void methodMissing(String name, Object args) {
		throw new MissingMethodException(name, this.class, args as Object[])
	}

	def propertyMissing(String name) {
		throw new MissingMethodException(name, this.class)
	}
}

class UseRuleDelegate {
	private EntitySection configuration
	private String objectName
	private String id
	private RuleSection ruleConfig

	UseRuleDelegate(EntitySection configuration, String objectName, String id) {
		this.configuration = configuration
		this.objectName = objectName
		this.id = id
	}

	void name(String name) {
		ruleConfig = new RuleSection()
		ruleConfig.setIsGeneric true;
		ruleConfig.setName name;
		ruleConfig.setObject this.objectName
		ruleConfig.setObjectId this.id
		this.configuration.addRule ruleConfig
	}

	void attributes(String[] attributes) {
		if (ruleConfig.attributes.empty) {
			attributes.each {ruleConfig.addAttribute it}
		}
	}

	def propertyMissing(String name) {
		throw new MissingMethodException(name, this.class)
	}
}
