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
package org.onap.aai.validation.ruledriven.rule;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.onap.aai.validation.ruledriven.configuration.GroovyConfigurationException;
import org.onap.aai.validation.ruledriven.configuration.RuleSection;
import org.onap.aai.validation.ruledriven.rule.GroovyRule;

/**
 * Tests for creating an AuditRule object and then executing the rule expression against fixed attribute values
 *
 * @see GroovyRule
 *
 */
public class TestRuleValidation {

    static {
        System.setProperty("AJSC_HOME", ".");
    }

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    /**
     * Test multiple sample expressions each with various different attribute names. Each of the attribute names used in
     * this test is valid for use within a rule.
     *
     * @throws GroovyConfigurationException
     * @throws IOException
     * @throws IllegalAccessException
     * @throws InstantiationException
     *
     * @throws Exception
     */
    @Test
    public void testValidAttributeNames()
            throws InstantiationException, IllegalAccessException, IOException, GroovyConfigurationException {
        testValidExpressions("clli"); // Sample name
        testValidExpressions("c");
        testValidExpressions("i"); // Short attribute name, part of the size() text
        testValidExpressions("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        testValidExpressions("size"); // Does not interfere with .size() method call
        testValidExpressions("s12"); // Digits allowed in the attribute name
        testValidExpressions("s12s");
        testValidExpressions("12s");
        testValidExpressions("12-s"); // Hyphens supported
        testValidExpressions("12_s"); // Underscores supported
        testValidExpressions("if"); // Keyword
        testValidExpressions("return");
        testValidExpressions("A.B.C"); // Dots indicate a JSON Path
    }

    /**
     * Test that the attribute name is sanitised. Each of the following attribute names is invalid for use in the rules
     * configuration.
     *
     * @throws Exception
     */
    @Test
    public void testInvalidAttributeNames() throws Exception {
        testInvalidAttributeName(""); // Zero-length
        testInvalidAttributeName("null"); // Reserved keyword
        testInvalidAttributeName("8"); // This would cause replacement of the value 8 in the expression
        testInvalidAttributeName("size()"); // This would replace the call to size()
        testInvalidAttributeName("s?"); // Special character not supported by the implementation
        testInvalidAttributeName("\"");
        testInvalidAttributeName("s\\");
        testInvalidAttributeName("s/");
        testInvalidAttributeName("s\"");
        testInvalidAttributeName("s'");
        testInvalidAttributeName("==");
    }

    /**
     * A rule cannot support an attribute that is zero-length. This test ensures that this is detected even when the
     * expression itself is valid.
     *
     * @throws Exception
     */
    @Test
    public void testZeroLengthAttribute() throws Exception {
        String attribute = "";
        String expression = "clli.size() == 8 || clli.size() == 11";
        GroovyRule rule = buildRule("testRule", attribute, expression);
        assertThat(rule.toString(), rule.isValid(), is(equalTo(false)));
    }

    @Test
    public void testInvalidRuleSyntax() throws Exception {
        String attribute = "fred";
        String expression = "fred. = 8 || fred.size() == 11";
        exception.expect(GroovyConfigurationException.class);
        exception.expectMessage(containsString("unexpected"));
        buildRule("testRule", attribute, expression);
    }

    @Test
    public void testInvalidAttribute() throws Exception {
        String attribute = "fred";
        String expression = "size.size() == 8 || size.size() == 11";
        GroovyRule rule = buildRule("testRule", attribute, expression);

        assertThat(rule.toString(), rule.isValid(), is(equalTo(false)));
    }

    /**
     * Utility to build and test different expressions using the supplied attribute name
     *
     * @param attribute attribute (field) identifier
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IOException
     * @throws GroovyConfigurationException
     */
    private void testValidExpressions(String attribute)
            throws InstantiationException, IllegalAccessException, IOException, GroovyConfigurationException {
        testValidLengthRule(attribute);
    }

    /**
     * Utility to build and test an expression asserting that the size() of the attribute is 8 or 11
     *
     * @param attribute attribute (field) identifier
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IOException
     * @throws GroovyConfigurationException
     */
    private void testValidLengthRule(String attribute)
            throws InstantiationException, IllegalAccessException, IOException, GroovyConfigurationException {
        String fieldName = attribute.substring(attribute.lastIndexOf(".") + 1);
        String expression = fieldName + ".size() == 8 || " + fieldName + ".size() == 11";
        GroovyRule rule = buildRule("testRule", attribute, expression);

        // Test that the rule is valid
        assertThat(rule.toString(), rule.isValid(), is(equalTo(true)));

        // Now test that the rule actually executes successfully
        Map<Object, Boolean> tests = new HashMap<>();
        tests.put("", false);
        tests.put("X", false);
        tests.put("1234567", false);
        tests.put("12345678", true); // 8 digits
        tests.put("ABCDEFGH", true); // 8 chars
        tests.put("12E4567890A", true); // 11 mixed digits/chars
        tests.put("abcdefghijk", true); // 11 chars
        tests.put("AbCdEfGhIjK", true); // 11 chars mixed case
        tests.put("AbCdEfGhIj?", true); // special char
        testRuleExpression(attribute, expression, tests);
    }

    private void testRuleExpression(String attributes, String expression, Map<Object, Boolean> tests)
            throws InstantiationException, IllegalAccessException, IOException, GroovyConfigurationException {
        GroovyRule rule = buildRule("testRule", attributes, expression);
        assertThat("The rule should be valid", rule.isValid(), is(equalTo(true)));
        for (Entry<?, ?> e : tests.entrySet()) {
            Object attributeValue = e.getKey();
            Boolean expectedResult = (Boolean) e.getValue();
            RuleHelper.assertRuleResult(rule, attributeValue, expectedResult);
        }
    }

    /**
     * Build a simple rule using a RuleConfiguration object
     *
     * @param name the rule name
     * @param attribute a named variable, referenced in the expression
     * @param expression the expression to evaluate (returns a Boolean value)
     * @return
     * @throws IOException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws GroovyConfigurationException
     */
    private GroovyRule buildRule(String name, String attribute, String expression)
            throws InstantiationException, IllegalAccessException, IOException, GroovyConfigurationException {
        RuleSection ruleConfig = new RuleSection();
        ruleConfig.setName(name);
        ruleConfig.setAttributes(Collections.singletonList(attribute));
        ruleConfig.setExpression(expression);
        return new GroovyRule(ruleConfig);
    }

    /**
     * Utility to build a rule and test that the attribute is valid
     *
     * @param attribute attribute (field) identifier
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IOException
     * @throws GroovyConfigurationException
     */
    private void testInvalidAttributeName(String attribute)
            throws InstantiationException, IllegalAccessException, IOException, GroovyConfigurationException {
        String expression = attribute + ".size() == 8 || " + attribute + ".size() == 11";
        GroovyRule rule = buildRule("testRule", attribute, expression);

        // Test that the rule is invalid
        assertThat(rule.toString(), rule.isValid(), is(equalTo(false)));
    }
}
