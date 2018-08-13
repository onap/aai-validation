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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.onap.aai.validation.reader.data.AttributeValues;
import org.onap.aai.validation.ruledriven.configuration.GroovyConfigurationException;
import org.onap.aai.validation.ruledriven.configuration.RuleSection;
import org.onap.aai.validation.ruledriven.rule.GroovyRule;

/**
 * Tests for creating an AuditRule object and then executing the rule expression against fixed attribute values
 *
 * @see GroovyRule
 *
 */
public class TestRuleExecution {

    static {
        System.setProperty("APP_HOME", ".");
    }

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    /**
     * Simple example of a rule using a boolean expression, acting on a single attribute
     *
     * @throws Exception
     */
    @Test
    public void testRuleCreation() throws Exception {
        // This object represents a rule
        GroovyRule rule = buildRule("i", "i == 44");

        // Success
        assertRuleResult(rule, 44, true);

        // Failure
        assertRuleResult(rule, 5, false);
    }

    @Test
    public void testRuleWithMultipleAttributes() throws Exception {
        GroovyRule rule = buildRule(Arrays.asList("i", "j"), "i == 22 && j == 44");

        // Success
        assertRuleResult(rule, Arrays.asList(22, 44), true);

        // Failure
        assertRuleResult(rule, Arrays.asList(5, 5), false);
        assertRuleResult(rule, Arrays.asList(22, 5), false);
        assertRuleResult(rule, Arrays.asList(5, 44), false);
    }

    /**
     * vserver is related to vpe and vserver-name contains me6
     */
    @Test
    public void testConditionalRegExp() throws Exception {
        // !related-to.contains("vpe") || vserver-name =~ "me6"
        String expression = "!related-to.contains(\"vpe\") || vserver-name =~ \"me6\"";
        List<String> attributes = Arrays.asList("related-to", "vserver-name");
        GroovyRule rule = buildRule(attributes, expression);

        // Create some tests, varying the values for each attribute
        Collection<Collection<String>> relatedToTests = new ArrayList<>();
        Collection<Collection<String>> vserverNameTests = new ArrayList<>();

        // These are the related-to values to test
        relatedToTests.add(Collections.<String>emptyList());
        relatedToTests.add(Arrays.asList("vpe"));
        relatedToTests.add(Arrays.asList("vpe", "vces"));
        relatedToTests.add(Arrays.asList("vces"));
        relatedToTests.add(Arrays.asList("other", "vces"));

        // These are the vserver-name values to test
        Collection<String> testNames = new ArrayList<>();
        testNames.add("fred");
        testNames.add("me");
        testNames.add("me7");
        testNames.add("me6");
        testNames.add("123me6");
        testNames.add("me6789");
        testNames.add("123me6789");

        // Additional test for no vserver-name values present
        vserverNameTests.add(Collections.<String>emptyList());

        for (String name : testNames) {
            // Build single element lists containing the test vserver names
            vserverNameTests.add(Arrays.asList(name));

            // Also build multi-element lists containing the test vserver names
            vserverNameTests.add(Arrays.asList(name, "6dummy"));
        }

        // Now run the tests, computing the expected Boolean result each time
        for (Collection<String> relatedToList : relatedToTests) {
            // If no vpe is related, then the result will always be true
            boolean expectedResult = !relatedToList.contains("vpe");

            Map<String, Object> map = new HashMap<>();
            map.put("related-to", relatedToList);

            for (Collection<String> vserverNames : vserverNameTests) {
                // Assign the strings to the attribute vserver-name
                map.put("vserver-name", vserverNames);

                if (expectedResult == false) {
                    for (String name : vserverNames) {
                        if (name.contains("me6")) {
                            expectedResult = true;
                            break;
                        }
                    }
                }

                testWithAttributeValue(rule, map, expectedResult);
            }
        }
    }

    /**
     * generic-vnf.vnf-type should be "BW NFM" and not "bnfm"
     */
    @Test
    public void testStringComparison() throws Exception {
        String attribute = "generic-vnf.vnf-type";
        String expression = "vnf-type == 'BW NFM'";
        GroovyRule rule = buildRule(attribute, expression);
        assertRuleResult(rule, "BW NFM", true);
        assertRuleResult(rule, "bnfm", false);
        assertRuleResult(rule, null, false);
        assertRuleResult(rule, "", false);
        assertRuleResult(rule, "fred", false);
        assertRuleResult(rule, "bob", false);
        assertRuleResult(rule, 5, false);
        assertRuleResult(rule, 1000L, false);
    }

    @Test
    public void testNotEquals() throws Exception {
        GroovyRule rule = buildRule("x", "x != 'foo'");
        assertRuleResult(rule, "", true);
        assertRuleResult(rule, "bar", true);
        assertRuleResult(rule, "foo", false);
        assertRuleResult(rule, "foo\n", true);
        assertRuleResult(rule, 5, true);
        assertRuleResult(rule, 1000L, true);
        assertRuleResult(rule, null, true);
    }

    @Test
    public void testGreaterThan() throws Exception {
        Map<Object, Boolean> tests = new HashMap<>();
        tests.put(-1, false);
        tests.put(0, false);
        tests.put(1, false);
        tests.put(4, false);
        tests.put(5, true);
        tests.put(55, true);
        tests.put(null, false);
        testRuleExpression("i", "i > 4", tests);
    }

    @Test
    public void testValidStringLength() throws Exception {
        String attribute = "clli";
        String expression = "clli.size() == 8 || clli.size() == 11";
        Map<Object, Boolean> tests = new HashMap<>();
        tests.put("", false);
        tests.put("X", false);
        tests.put("1", false);
        tests.put("1234", false);
        tests.put("123456", false);
        tests.put("1234567", false);
        tests.put("12345678", true);
        tests.put("123456789", false);
        tests.put("1234567890", false);
        tests.put("12345678901", true);
        tests.put("1234567890ABC", false);
        tests.put("1234567890123456789012345678901234567890", false);
        testRuleExpression(attribute, expression, tests);
    }

    /**
     * location_clli is 8 or 11 characters and must be characters only
     */
    @Test
    public void testStringLengthAndChars() throws Exception {
        String attribute = "location_clli";
        String expression = "location_clli != null && location_clli.matches('[a-zA-Z]{8,11}')";
        Map<Object, Boolean> tests = new HashMap<>();
        tests.put(null, false);
        tests.put("", false);
        tests.put("X", false);
        tests.put("1234567", false);
        tests.put("12345678", false); // 8 digits
        tests.put("ABCDEFGH", true); // 8 chars
        tests.put("1234567890A", false); // 11 mixed
        tests.put("abcdefghijk", true); // 11 chars
        tests.put("AbCdEfGhIjK", true); // 11 chars
        tests.put("AbCdEfGhIj?", false); // special char
        testRuleExpression(attribute, expression, tests);
    }

    /**
     * incorrect naming, e.g. expecting ae0.x (where x is unit/subinterface number), but receiving ae0.353.3303
     */
    @Test
    public void testRegularExpression() throws Exception {
        String attribute = "unit";
        String expression = "unit.matches('^ae0\\\\.(\\\\d)+')";
        Map<Object, Boolean> tests = new HashMap<>();
        tests.put("", false);
        tests.put("X", false);
        tests.put("ae0", false);
        tests.put("ae0.", false);
        tests.put("ae01", false);
        tests.put("ae012", false);
        tests.put("ae0.1", true);
        tests.put("ae0.354", true);
        tests.put("ae0.354.", false);
        tests.put("ae0.353.3303", false);
        tests.put("ae0.353.3303.1", false);
        testRuleExpression(attribute, expression, tests);
    }

    @Test
    public void testNullStringLength() throws Exception {
        String attribute = "clli";
        String expression = "clli.size() == 8 || clli.size() == 11";
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(containsString("Argument"));
        exception.expectMessage(containsString("null"));
        GroovyRule rule = buildRule(attribute, expression);
        rule.execute((Object[]) null);
    }

    @Test
    public void testStringLengthWithNull() throws Exception {
        String attribute = "a";
        String expression = "a == null || a.size() > 4";
        Map<Object, Boolean> tests = new HashMap<>();
        tests.put("", false);
        tests.put("X", false);
        tests.put("1", false);
        tests.put("1234", false);
        tests.put("123456", true);
        tests.put("1234567", true);
        tests.put("12345678", true);
        tests.put("123456789", true);
        tests.put("1234567890", true);
        tests.put("12345678901", true);
        tests.put("1234567890ABC", true);
        tests.put("1234567890123456789012345678901234567890", true);
        testRuleExpression(attribute, expression, tests);
    }

    /**
     * Prov-status cannot be ACTIVE or NULL (empty)
     */
    @Test
    public void testStringTextWithNull() throws Exception {
        String attribute = "prov-status";
        String expression = "prov-status != null && prov-status != 'ACTIVE'";
        Map<Object, Boolean> tests = new HashMap<>();
        tests.put(null, false);
        tests.put("ACTIVE", false);
        tests.put("", true);
        tests.put("X", true);
        tests.put("1", true);
        tests.put("1234", true);
        tests.put("123456", true);
        tests.put("1234567", true);
        tests.put("12345678", true);
        tests.put("123456789", true);
        tests.put("1234567890", true);
        tests.put("12345678901", true);
        tests.put("1234567890ABC", true);
        tests.put("1234567890123456789012345678901234567890", true);
        testRuleExpression(attribute, expression, tests);
    }

    /**
     * Prov-status cannot be ACTIVE/active or NULL/null or empty or missing
     */
    @Test
    public void testCaseInsensitveStringMatch() throws Exception {
        String attribute = "prov-status";
        String expression =
                "prov-status != null && prov-status.size() > 0 && !prov-status.equalsIgnoreCase('NULL') && !prov-status.equalsIgnoreCase('ACTIVE')";
        Map<Object, Boolean> tests = new HashMap<>();
        tests.put(null, false);
        tests.put("", false);
        tests.put("active", false);
        tests.put("ACTIVE", false);
        tests.put("null", false);
        tests.put("NULL", false);
        tests.put("NVTPROV", true);
        tests.put("1", true);
        tests.put("1234", true);
        testRuleExpression(attribute, expression, tests);
    }

    @Test
    public void testUsingAttributeValuesObject() throws Exception {
        List<String> attributes = Arrays.asList("attr1", "attr2");
        String expression = "attr1.size() == 8 || attr2.size() == 11";
        GroovyRule rule = buildRule(attributes, expression);
        AttributeValues attributeValues = new AttributeValues("attr1", "other");
        attributeValues.put("attr2", "other");
        rule.execute(attributeValues);
    }

    @Test
    public void testMissingMethod() throws Exception {
        String attribute = "clli";
        String expression = "clli.size() == 8 || clli.size() == 11";
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(containsString("Integer"));
        exception.expectMessage(containsString("size()"));
        GroovyRule rule = buildRule(attribute, expression);
        rule.execute(1);
    }

    @Test
    public void testMultipleAttributes() throws Exception {
        List<String> attributes = Arrays.asList("clli", "other");
        String expression = "clli.size() == 8 || other.size() == 11";
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(containsString("Integer"));
        exception.expectMessage(containsString("size()"));
        GroovyRule rule = buildRule(attributes, expression);
        rule.execute(new Integer(1), new Integer(2));
    }

    /**
     * Compare two attributes (using a sub-string match)
     */
    @Test
    public void testAttributeComparison() throws Exception {
        String attribute1 = "heat-stack-id";
        String attribute2 = "vnf-name";

        // Alternative forms of the expression
        Collection<String> expressionsToTest = new ArrayList<>();
        expressionsToTest.add("heat-stack-id?.size() > 10 && heat-stack-id[0..10] == vnf-name");
        expressionsToTest.add("heat-stack-id?.size() > 10 && heat-stack-id.substring(0,11) == vnf-name");
        expressionsToTest.add("heat-stack-id?.size() > 10 && heat-stack-id.take(11) == vnf-name");

        // Create some tests, varying the values for each attribute
        Collection<String> heatStackIds = new ArrayList<>();
        Collection<String> vnfNames = new ArrayList<>();

        heatStackIds.add("123me67890abcdef");
        heatStackIds.add("123me67890a");
        heatStackIds.add("fred");
        heatStackIds.add("");
        heatStackIds.add(null);

        vnfNames.add("123me67890abcdef");
        vnfNames.add("123me67890a");
        vnfNames.add("123me6789");
        vnfNames.add("fred");
        vnfNames.add("bob");
        vnfNames.add("");
        vnfNames.add(null);

        // Build a rule per expression
        for (String expression : expressionsToTest) {
            GroovyRule rule = buildRule(Arrays.asList(attribute1, attribute2), expression);

            // Now run the tests, computing the expected Boolean result each time
            for (String heatStackId : heatStackIds) {
                Map<String, Object> map = new HashMap<>();
                map.put(attribute1, heatStackId);
                for (String vnfName : vnfNames) {
                    map.put(attribute2, vnfName);
                    boolean expectedExpressionResult = // Java equivalent of the expression
                            heatStackId != null && heatStackId.length() > 10
                                    && heatStackId.substring(0, 11).equals(vnfName);
                    testWithAttributeValue(rule, map, expectedExpressionResult);
                }
            }
        }
    }

    /**
     * @param rule
     * @param tests
     */
    public void assertTestResults(GroovyRule rule, Map<Object, Boolean> tests) {
        for (Entry<?, ?> e : tests.entrySet()) {
            Object attributeValue = e.getKey();
            Boolean expectedResult = (Boolean) e.getValue();
            RuleHelper.assertRuleResult(rule, attributeValue, expectedResult);
        }
    }

    private void testWithAttributeValue(GroovyRule rule, Map<String, Object> map, boolean expectedResult) {
        AttributeValues attributeValues = new AttributeValues(map);
        RuleHelper.assertRuleResult(rule, attributeValues, expectedResult);
    }

    private void testRuleExpression(String attributes, String expression, Map<Object, Boolean> tests)
            throws InstantiationException, IllegalAccessException, IOException, GroovyConfigurationException {
        GroovyRule rule = buildRule("testRule", attributes, expression);
        assertTestResults(rule, tests);
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
            throws IOException, InstantiationException, IllegalAccessException, GroovyConfigurationException {
        RuleSection ruleConfig = new RuleSection();
        ruleConfig.setName(name);
        ruleConfig.setAttributes(Collections.singletonList(attribute));
        ruleConfig.setExpression(expression);
        return new GroovyRule(ruleConfig);
    }

    private GroovyRule buildRule(String name, List<String> attributes, String expression)
            throws InstantiationException, IllegalAccessException, IOException, GroovyConfigurationException {
        RuleSection ruleConfig = new RuleSection();
        ruleConfig.setName(name);
        ruleConfig.setAttributes(attributes);
        ruleConfig.setExpression(expression);
        return new GroovyRule(ruleConfig);
    }

    /**
     * Build a simple rule (with a default name) using a RuleConfiguration object
     *
     * @see TestRuleExecution#buildRule(String, String, String)
     *
     * @param attribute a named variable, referenced in the expression
     * @param expression the expression to evaluate (returns a Boolean value)
     * @throws GroovyConfigurationException
     */
    private GroovyRule buildRule(String attribute, String expression)
            throws InstantiationException, IllegalAccessException, IOException, GroovyConfigurationException {
        return buildRule("testRule", attribute, expression);
    }

    private GroovyRule buildRule(List<String> attributes, String expression)
            throws InstantiationException, IllegalAccessException, IOException, GroovyConfigurationException {
        return buildRule("testRule", attributes, expression);
    }

    private void assertRuleResult(GroovyRule rule, Object value, boolean expectedResult) {
        RuleHelper.assertRuleResult(rule, value, expectedResult);
    }

}
