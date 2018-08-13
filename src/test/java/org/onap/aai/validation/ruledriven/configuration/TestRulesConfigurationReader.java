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
package org.onap.aai.validation.ruledriven.configuration;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.onap.aai.validation.ruledriven.configuration.EntitySection;
import org.onap.aai.validation.ruledriven.configuration.GroovyConfigurationException;
import org.onap.aai.validation.ruledriven.configuration.RuleSection;
import org.onap.aai.validation.ruledriven.configuration.SettingsSection;
import org.onap.aai.validation.ruledriven.configuration.build.EntityBuilder;
import org.onap.aai.validation.ruledriven.configuration.build.RuleBuilder;
import org.onap.aai.validation.ruledriven.configuration.build.ValidationBuilder;
import org.onap.aai.validation.test.util.RandomString;

/**
 * Tests for the Groovy classes that read and parse the rules configuration
 *
 */
public class TestRulesConfigurationReader {

    static {
        System.setProperty("APP_HOME", ".");
    }

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    /**
     * Exception expected by Junit. This is modified per test.
     */
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    private static final String INVALID_TOKEN = "audif";

    @Test
    public void testEmptyConfiguration() throws Exception {
        ConfigFileBuilder builder = new ConfigFileBuilder(testFolder);
        Collection<EntitySection> entitys = builder.loadConfiguration();
        assertThat(entitys.size(), is(0));
    }

    /**
     * Test for reporting of an unknown token with various syntax formatting
     *
     * @throws Exception
     */
    @Test
    public void testInvalidTopLevelItem() throws Exception {
        testInvalidToken(INVALID_TOKEN);
        testInvalidToken(INVALID_TOKEN + " { }");
        testInvalidToken("{ " + INVALID_TOKEN + " }");
        testInvalidToken(INVALID_TOKEN + " { ");
        testInvalidToken(INVALID_TOKEN + " } ");
        testInvalidToken("{} " + INVALID_TOKEN);
        testInvalidToken("\"" + INVALID_TOKEN);
    }

    @Test
    public void testInvalidentityItem1() throws Exception {
        testInvalidToken("entity { invalid }", "invalid");
    }

    @Test
    public void testInvalidentityItem2() throws Exception {
        testInvalidToken("entity { invalid item }", "item");
    }

    @Test
    public void testInvalidentityItem3() throws Exception {
        testInvalidToken("entity { name name\n fred fred}", "name");
    }

    @Test
    public void testInvalidQuery() throws Exception {
        testInvalidToken("entity { name 'name'\n joe}", "joe");
    }

    @Test
    public void testGenericRuleWithZeroAttributes() throws Exception {
        ConfigFileBuilder builder = new ConfigFileBuilder(testFolder);
        EntityBuilder entity = builder.entity();
        entity.appendValue("name", "entity");
        ValidationBuilder validation = entity.validation(buildProperties("type"));
        validation.useRule("ruleZ");
        builder.rule("ruleZ");

        exception.expect(GroovyConfigurationException.class);
        exception.expectMessage(containsString("attributes"));
        builder.loadConfiguration();
    }

    @Test
    public void testInvalidUseRule() throws Exception {
        testInvalidToken("entity { validation { useRule { foo } } }", "foo");
    }

    @Test
    public void testMissingRule() throws Exception {
        ConfigFileBuilder builder = new ConfigFileBuilder(testFolder);
        builder.entity().validation().useRule("missingRule");
        exception.expect(GroovyConfigurationException.class);
        exception.expectMessage(containsString("missingRule"));
        builder.loadConfiguration();
    }

    @Test
    public void testDuplicateRule() throws Exception {
        ConfigFileBuilder builder = new ConfigFileBuilder(testFolder);
        builder.rule("duplicateRule");
        builder.rule("duplicateRule");
        exception.expect(GroovyConfigurationException.class);
        exception.expectMessage(containsString("duplicateRule"));
        builder.loadConfiguration();
    }

    @Test
    public void testSimpleentity() throws Exception {
        String name = RandomString.generate();

        ConfigFileBuilder builder = new ConfigFileBuilder(testFolder);
        Properties entity = new Properties();
        entity.put("name", name);
        builder.entity(entity);

        List<EntitySection> entities = builder.loadConfiguration();

        assertThat(entities.size(), is(1));
        EntitySection entityConf = entities.get(0);
        assertThat(entityConf.getName(), is(equalTo(name)));
    }

    /**
     * The Settings section is not tested elsewhere
     *
     * @throws Exception
     */
    @Test
    public void testGlobalSettings() throws Exception {
        SettingsSection settings = new SettingsSection();
        settings.toString();
    }

    @Test
    public void testentityWithRuleUsingStrings() throws Exception {
        String name = RandomString.generate();
        String description = RandomString.generate();
        String severity = RandomString.generate();
        List<String> attributes = Arrays.asList(RandomString.generate());
        String validate = randomValidationExpression();

        Properties rule = new Properties();
        rule.put("name", name);
        rule.put("description", description);
        rule.put("severity", severity);
        rule.put("attributes", attributes);
        rule.put("validate", validate);

        String type = RandomString.generate();

        Properties validation = new Properties();
        validation.put("type", type);
        validation.put("rule", rule);

        ConfigFileBuilder builder = new ConfigFileBuilder(testFolder);
        Properties entity = new Properties();
        entity.put("validation", validation);
        builder.entity(entity);

        List<EntitySection> entities = builder.loadConfiguration();

        assertThat(entities.size(), is(1));
        EntitySection entityConf = entities.get(0);
        Collection<RuleSection> rules = entityConf.getRules();
        assertThat(rules.size(), is(1));

        RuleSection ruleConfig = rules.iterator().next();
        assertThat(ruleConfig.getName(), is(equalTo(name)));
        assertThat(ruleConfig.getDescription(), is(equalTo(description)));
        assertThat(ruleConfig.getSeverity(), is(equalTo(severity)));
        assertThat(ruleConfig.getAttributes(), is(equalTo(attributes)));
        assertThat(ruleConfig.getExpression(), is(equalTo(validate)));
        assertThat(ruleConfig.getType(), is(equalTo(type)));
    }

    @Test
    public void testentityWithRuleUsingBuilder() throws Exception {
        String name = RandomString.generate();
        String description = RandomString.generate();
        String severity = RandomString.generate();
        List<String> attributes = Arrays.asList(RandomString.generate());
        String validate = randomValidationExpression();
        String type = RandomString.generate();

        ConfigFileBuilder builder = new ConfigFileBuilder(testFolder);
        ValidationBuilder validation = builder.entity().validation();
        validation.appendValue("type", type);

        RuleBuilder rule = validation.rule(name);
        rule.appendValue("description", description);
        rule.appendValue("severity", severity);
        rule.appendValue("attributes", attributes);
        rule.appendValue("validate", validate);

        List<EntitySection> entitys = builder.loadConfiguration();

        assertThat(entitys.size(), is(1));
        EntitySection entityConf = entitys.get(0);
        Collection<RuleSection> rules = entityConf.getRules();
        assertThat(rules.size(), is(1));

        RuleSection ruleConfig = rules.iterator().next();
        assertThat(ruleConfig.getName(), is(equalTo(name)));
        assertThat(ruleConfig.getDescription(), is(equalTo(description)));
        assertThat(ruleConfig.getSeverity(), is(equalTo(severity)));
        assertThat(ruleConfig.getAttributes(), is(equalTo(attributes)));
        assertThat(ruleConfig.getExpression(), is(equalTo(validate)));
        assertThat(ruleConfig.getType(), is(equalTo(type)));
    }

    @Test
    public void testentityWithEmbeddedRule() throws Exception {
        Properties rule = buildProperties("name", "description", "severity", "validate");
        rule.put("attributes", Arrays.asList(RandomString.generate()));

        Properties validation = buildProperties("type");
        validation.put("rule", rule);

        ConfigFileBuilder builder = new ConfigFileBuilder(testFolder);
        Properties entity = new Properties();
        entity.put("validation", validation);
        builder.entity(entity);

        List<EntitySection> entitys = builder.loadConfiguration();

        assertThat(entitys.size(), is(1));
        EntitySection entityConf = entitys.get(0);
        Collection<RuleSection> rules = entityConf.getRules();
        assertThat(rules.size(), is(1));

        RuleSection ruleConfig = rules.iterator().next();
        assertThatConfigMatchesProperties(ruleConfig, rule, validation);
    }

    @Test
    public void testEmbeddedRuleUsingString() throws Exception {
        ConfigFileBuilder builder = new ConfigFileBuilder(testFolder);
        builder.appendLine("entity {");
        builder.appendLine("	type 'newvce'");
        builder.appendLine("	validation {");
        builder.appendLine("		rule {");
        builder.appendLine("			name		'vnfName'");
        builder.appendLine(
                "			description	'Validate that the vnf-name attribute matches the expected string format'");
        builder.appendLine("			severity	'MAJOR'");
        builder.appendLine("			attributes	'vnf-name'");
        builder.appendLine("			validate	'vnf-name.matchesPattern(xxxxnnnvbc)'");
        builder.appendLine("		}");
        builder.appendLine("	}");
        builder.appendLine("}");
        List<EntitySection> entitys = builder.loadConfiguration();

        assertThat(entitys.size(), is(1));
        EntitySection entityConf = entitys.get(0);
        assertThat(entityConf.getName(), is(equalTo("newvce")));

        Collection<RuleSection> rules = entityConf.getRules();
        assertThat(rules.size(), is(1));
        RuleSection ruleConfig = rules.iterator().next();

        assertThat(ruleConfig.getType(), is(equalTo("newvce")));
        assertThat(ruleConfig.getName(), is(equalTo("vnfName")));
        assertThat(ruleConfig.getDescription(),
                is(equalTo("Validate that the vnf-name attribute matches the expected string format")));
        assertThat(ruleConfig.getSeverity(), is(equalTo("MAJOR")));
        List<String> expectedList = new ArrayList<String>();
        expectedList.add("vnf-name");
        assertThat(ruleConfig.getAttributes(), is(equalTo(expectedList)));
        assertThat(ruleConfig.getExpression(), is(equalTo("vnf-name.matchesPattern(xxxxnnnvbc)")));
    }

    @Test
    public void testGenericRuleUsingStrings() throws Exception {
        ConfigFileBuilder builder = new ConfigFileBuilder(testFolder);
        builder.appendLine("entity {");
        builder.appendLine("	type 'newvce'");
        builder.appendLine("	validation {");
        builder.appendLine("		useRule {");
        builder.appendLine("			name 'genericRule'");
        builder.appendLine("			attributes 'clli'");
        builder.appendLine("		}");
        builder.appendLine("	}");
        builder.appendLine("}");
        builder.appendLine("rule {");
        builder.appendLine("	name 'genericRule'");
        builder.appendLine("	description 'The field (attribute) must be less than 50 characters in length'");
        builder.appendLine("	severity 'MINOR'");
        builder.appendLine("	validate 'attribute.size() < 50'");
        builder.appendLine("}");

        List<EntitySection> entitys = builder.loadConfiguration();
        assertThat(entitys.size(), is(1));
        EntitySection entityConf = entitys.get(0);
        assertThat(entityConf.getName(), is(equalTo("newvce")));

        Collection<RuleSection> rules = entityConf.getRules();
        assertThat("Number of rules", rules.size(), is(1));
        RuleSection ruleConfig = rules.iterator().next();

        assertThat(ruleConfig.getType(), is(equalTo("newvce")));
        assertThat(ruleConfig.getName(), is(equalTo("genericRule")));
        assertThat(ruleConfig.getDescription(),
                is(equalTo("The field (attribute) must be less than 50 characters in length")));
        assertThat(ruleConfig.getSeverity(), is(equalTo("MINOR")));
        List<String> expectedList = new ArrayList<String>();
        expectedList.add("clli");
        assertThat(ruleConfig.getAttributes(), is(equalTo(expectedList)));
        assertThat(ruleConfig.getExpression(), is(equalTo("attribute.size() < 50")));
    }

    @Test
    public void testMultipleGenericRules() throws Exception {
        ConfigFileBuilder builder = new ConfigFileBuilder(testFolder);
        EntityBuilder entity = builder.entity();
        ValidationBuilder validation = entity.validation(buildProperties("type"));
        validation.useRule("rule1");
        validation.useRule("rule2").appendValue("attributes", "attr");
        builder.rule("rule1").appendValue("attributes", "field").appendValue("validate", "true");
        builder.rule("rule2").appendValue("validate", "true");

        List<EntitySection> entitys = builder.loadConfiguration();

        assertThat(entitys.size(), is(1));
        EntitySection entityConf = entitys.get(0);
        Collection<RuleSection> rules = entityConf.getRules();
        assertThat(rules.size(), is(2));

        Iterator<RuleSection> iterator = rules.iterator();
        RuleSection ruleConfig = iterator.next();
        assertThat(ruleConfig.getName(), is(equalTo("rule1")));
        assertThat(ruleConfig.getAttributes(), is(Collections.singletonList("field")));

        ruleConfig = iterator.next();
        assertThat(ruleConfig.getName(), is(equalTo("rule2")));
        assertThat(ruleConfig.getAttributes(), is(Collections.singletonList("attr")));
    }

    @Test
    public void testGenericRuleWithMultipleAttributes() throws Exception {
        ConfigFileBuilder builder = new ConfigFileBuilder(testFolder);
        EntityBuilder entity = builder.entity();
        ValidationBuilder validation = entity.validation(buildProperties("type"));
        validation.useRule("rule").appendValue("attributes", Arrays.asList("attr1", "attr2"));
        builder.rule("rule").appendValue("attributes", Arrays.asList("field1", "field2")).appendValue("validate",
                "true");

        List<EntitySection> entities = builder.loadConfiguration();

        assertThat(entities.size(), is(1));
        EntitySection entityConf = entities.get(0);
        Collection<RuleSection> rules = entityConf.getRules();
        assertThat(rules.size(), is(1));

        Iterator<RuleSection> iterator = rules.iterator();
        RuleSection ruleConfig = iterator.next();
        assertThat(ruleConfig.getName(), is(equalTo("rule")));
        assertThat(ruleConfig.getAttributes(), is(Arrays.asList("attr1", "attr2")));
    }

    private String randomValidationExpression() {
        String validate = RandomString.generate();
        while (validate.matches("^\\d+[a-fA-F]+\\D+.*") || validate.matches("0x\\D+.*")) {
            validate = RandomString.generate();
        }
        return validate;
    }

    private void testInvalidToken(String configText) throws IOException {
        testInvalidToken(configText, INVALID_TOKEN);
    }

    private void testInvalidToken(String configText, String invalidToken) throws IOException {
        ConfigFileBuilder builder = new ConfigFileBuilder(testFolder);
        builder.addContent(configText);
        assertConfigurationException(invalidToken);
        builder.loadConfiguration();
    }

    private void assertConfigurationException(String invalidToken) {
        exception.expect(GroovyConfigurationException.class);
        exception.expectMessage(containsString(invalidToken));
        exception.expect(ConfigurationExceptionMatcher.hasInvalidToken(invalidToken));
        exception.expect(ConfigurationExceptionMatcher.configTextContains(invalidToken));
    }

    private Properties buildProperties(String... strings) {
        Properties props = new Properties();
        for (String str : strings) {
            props.put(str, randomValidationExpression());
        }
        return props;
    }

    private void assertThatConfigMatchesProperties(RuleSection ruleConfig, Properties rule, Properties validation) {
        assertMatch(ruleConfig.getName(), rule, "name");
        assertMatch(ruleConfig.getDescription(), rule, "description");
        assertMatch(ruleConfig.getSeverity(), rule, "severity");
        assertThat(ruleConfig.getAttributes(), is(equalTo(rule.get("attributes"))));
        assertMatch(ruleConfig.getExpression(), rule, "validate");
        assertMatch(ruleConfig.getType(), validation, "type");
        assertMatch(ruleConfig.getObjectId(), validation, "objectId");
    }

    private void assertMatch(String value, Properties props, String key) {
        assertThat(value, is(equalTo(props.getProperty(key))));
    }
}
