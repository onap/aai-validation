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
package org.onap.aai.validation.ruledriven;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import org.onap.aai.validation.ruledriven.configuration.EntitySection;
import org.onap.aai.validation.ruledriven.configuration.GroovyConfigurationException;
import org.onap.aai.validation.ruledriven.configuration.RuleSection;
import org.onap.aai.validation.ruledriven.rule.GroovyRule;
import org.onap.aai.validation.ruledriven.rule.Rule;

/**
 * Helper class storing the relationships from entity type to rules. This class constructs the actual rules from the
 * supplied configuration.
 *
 */
public class RuleManager {

    private Map<String, List<Rule>> rulesMap = new LinkedHashMap<>();
    private List<EntitySection> entities;

    /**
     * Create the rules for each type of entity based on the supplied configuration
     *
     * @param entities configuration (all entities)
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws GroovyConfigurationException
     * @throws IOException
     */
    public RuleManager(List<EntitySection> entities)
            throws InstantiationException, IllegalAccessException, GroovyConfigurationException, IOException {
        this.entities = entities;
        for (EntitySection entity : entities) {
            List<Rule> rules = new ArrayList<>();
            for (RuleSection section : entity.getRules()) {
                rules.add(new GroovyRule(section));
            }
            rulesMap.put(entity.getType(), rules);
        }
    }

    public List<EntitySection> getEntities() {
        return entities;
    }

    /**
     * @param entityType
     * @return the rules configured for this entity type
     */
    public List<Rule> getRulesForEntity(String entityType) {
        List<Rule> rules = rulesMap.get(entityType);
        return rules == null ? Collections.emptyList() : rules;
    }

    public static String generateKey(String[] indices) {
        SortedSet<String> sortedIndices = new TreeSet<>();
        Collections.addAll(sortedIndices, indices);
        StringBuilder sb = new StringBuilder();
        Iterator<String> iterator = sortedIndices.iterator();
        while (iterator.hasNext()) {
            sb.append("[");
            sb.append(iterator.next());
            sb.append("]");
        }
        return sb.toString();
    }
}
