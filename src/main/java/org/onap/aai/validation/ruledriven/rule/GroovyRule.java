/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright (c) 2018-2019 AT&T Intellectual Property. All rights reserved.
 * Copyright (c) 2018-2019 European Software Marketing Ltd.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.aai.validation.ruledriven.rule;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import groovy.lang.MetaMethod;
import groovy.lang.MissingMethodException;
import groovy.lang.MissingPropertyException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.runtime.InvokerInvocationException;
import org.onap.aai.cl.api.Logger;
import org.onap.aai.validation.logging.LogHelper;
import org.onap.aai.validation.reader.data.AttributeValues;
import org.onap.aai.validation.ruledriven.configuration.GroovyConfigurationException;
import org.onap.aai.validation.ruledriven.configuration.RuleSection;
import org.onap.aai.validation.util.StringUtils;

/**
 * Rule based on a Groovy script
 *
 */
public class GroovyRule implements Rule {

    private static final Logger applicationLogger = LogHelper.INSTANCE;

    /**
     * Do not allow special characters (e.g. ?) in an attribute name. Hyphens and underscores are accepted.
     */
    private static final Pattern ATTRIBUTE_NAME_WHITELIST = Pattern.compile("^[a-zA-Z0-9-_.\\*\\[\\]]*$");
    private static final Pattern ATTRIBUTE_NAME_BLACKLIST = Pattern.compile("^(|null)$");

    private String errorCategory;
    private String errorMessage;
    private String severity;
    private List<String> attributes;
    private List<String> attributePaths; // where in the JSON entity to read the attributes from

    private String methodName;
    private GroovyObject groovyObject;
    private List<String> originalFields;
    private String originalExpression;
    private String groovyExpression; // NOSONAR stored for debugging purposes
    private boolean ruleIsValid = true;
    private String name;

    /**
     * @param ruleConfig
     * @throws GroovyConfigurationException
     *             if the Groovy expression cannot be compiled
     * @throws IOException
     *             if the Groovy class loader throws an internal exception
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public GroovyRule(RuleSection ruleConfig)
            throws GroovyConfigurationException, IOException, InstantiationException, IllegalAccessException {
        setName(ruleConfig.getName());
        setErrorCategory(ruleConfig.getCategory());
        setErrorMessage(ruleConfig.getErrorMessage());
        setSeverity(ruleConfig.getSeverity());
        setAttributes(ruleConfig.getAttributes());
        setAttributePaths(ruleConfig.getAttributes());

        this.originalFields = new ArrayList<>();
        for (String field : ruleConfig.getExpressionFieldNames()) {
            originalFields.add(StringUtils.stripPrefix(field, "."));
        }

        Class<?> groovyClass = createRule(ruleConfig.getExpressionFieldNames(), ruleConfig.getExpression());

        if (groovyClass != null) {
            groovyObject = (GroovyObject) groovyClass.newInstance();

            for (MetaMethod method : groovyObject.getMetaClass().getMethods()) {
                if (method.getName().startsWith("rule")) {
                    methodName = method.getName();
                }
            }

            try {
                executeWithSampleData();
            } catch (IllegalArgumentException e) { // NOSONAR
                if (e.getCause() instanceof InvokerInvocationException
                        && e.getCause().getCause() instanceof MissingMethodException) {
                    applicationLogger
                            .debug("WARNING: Rule \"" + getName() + "\" does not accept \"1\" for all input values");
                } else {
                    ruleIsValid = false;
                }
            }
        } else {
            ruleIsValid = false;
        }
    }

    public boolean isValid() {
        return ruleIsValid;
    }

    /**
     * Run the rule expression on the specified attribute values
     *
     * @param attributeValues
     * @return
     */
    @Override
    public RuleResult execute(AttributeValues attributeValues) {
        // Obtain the values of each of the attributes to pass into the rule
        List<Object> valueList = new ArrayList<>();
        for (String attrName : this.attributePaths) {
            valueList.add(attributeValues.get(attrName));
        }
        Object[] attrValuesArray = valueList.toArray();
        return execute(attrValuesArray);
    }

    /**
     * Apply the rule to some attribute(s)
     *
     * @param values
     *
     * @param groovyObject
     *            an instance/object of a Groovy class that implements one or more rule methods
     * @return the result of evaluating the expression
     */
    @Override
    public RuleResult execute(Object... values) {
        Object result = null;
        try {
            result = groovyObject.invokeMethod(getRuleMethod(), values);
        } catch (MissingPropertyException | MissingMethodException | InvokerInvocationException e) {
            throw new IllegalArgumentException(e);
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("Argument is null", e);
        }
        return new RuleResult(result);
    }

    @Override
    public String toString() {
        return "GroovyRule \"" + name + "\" " + attributePaths + " -> " + originalFields + " {" + originalExpression
                + "}";
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getErrorCategory() {
        return errorCategory;
    }

    public void setErrorCategory(String errorCategory) {
        this.errorCategory = errorCategory;
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    @Override
    public List<String> getAttributePaths() {
        return attributePaths;
    }

    private String getRuleMethod() {
        return methodName;
    }

    private void setAttributePaths(List<String> attributePaths) {
        this.attributePaths = attributePaths;
    }

    private void setAttributes(List<String> attributes) {
        this.attributes = new ArrayList<>();
        for (String attribute : attributes) {
            // Strip any prefixes containing the . character
            this.attributes.add(attribute.substring(attribute.lastIndexOf('.') + 1));
        }
    }

    /**
     * Create an anonymous Java Class implementing a Groovy Rule method for the supplied attributes and rule expression.
     *
     * @param attributes
     *            the attributes that form the parameters of the Groovy method
     * @param expression
     *            a valid Groovy method expression (implementing a rule)
     * @return the Java Class for accessing the Groovy method
     * @throws GroovyConfigurationException
     *             if the Groovy expression cannot be compiled
     * @throws IOException
     *             if the Groovy class loader throws an internal exception
     */
    private Class<?> createRule(List<String> attributes, String expression)
            throws GroovyConfigurationException, IOException {
        originalExpression = expression;
        groovyExpression = expression;
        String methodParams = "";

        int i = 1;
        for (String attribute : attributes) {
            if (isValidAttributeName(attribute)) {
                String fieldName = "field" + i++;
                methodParams = appendParameter(methodParams, fieldName);
                // Strip any prefixes from the attribute name in case of JayWay expression attributes.
                attribute = StringUtils.stripPrefix(attribute, ".");
                String regex = "\\b" + attribute + "\\b(?!\\()";
                groovyExpression = groovyExpression.replaceAll(regex, fieldName);
            } else {
                ruleIsValid = false;
                return null;
            }
        }

        return loadGroovyClass("def rule(" + methodParams + ") {" + groovyExpression + "}");
    }

    private String appendParameter(String methodParams, String fieldName) {
        StringBuilder newParams = new StringBuilder();
        if (methodParams.length() > 0) {
            newParams.append(methodParams).append(", ");
        }
        newParams.append("Object ").append(fieldName);
        return newParams.toString();
    }

    private boolean isValidAttributeName(String attributeName) {
        if (ATTRIBUTE_NAME_BLACKLIST.matcher(attributeName).matches()) {
            return false;
        }

        if (!ATTRIBUTE_NAME_WHITELIST.matcher(attributeName).matches()) {
            return false;
        }

        // Make sure that the attribute name is NOT purely a number
        try (Scanner sc = new Scanner(attributeName.trim())) {
            if (sc.hasNextInt()) {
                sc.nextInt(); // Consume the integer
                return sc.hasNext(); // Name is valid if there is more content
            }
        }

        return true; // Not an integer
    }

    private void executeWithSampleData() {
        Object[] values = new Object[attributes.size()];
        int i = 0;
        for (String attribute : attributes) {
            if (attribute.contains("[*]")) {
                values[i++] = Arrays.asList("{}");
            } else {
                values[i++] = "1";
            }
        }
        execute(values);
    }

    /**
     * Load and parse a Groovy script to create an anonymous Java Class.
     *
     * @param script
     *            valid Groovy content (for the class)
     * @return the Java Class for accessing the Groovy script
     * @throws GroovyConfigurationException
     *             if the Groovy script cannot be compiled
     * @throws IOException
     *             if the Groovy class loader throws an internal exception
     */
    @SuppressWarnings("rawtypes")
    private static Class loadGroovyClass(String script) throws GroovyConfigurationException, IOException {
        ClassLoader parent = GroovyRule.class.getClassLoader();
        GroovyClassLoader loader = new GroovyClassLoader(parent);
        Class groovyClass;
        try {
            groovyClass = loader.parseClass(script);
        } catch (CompilationFailedException e) {
            throw new GroovyConfigurationException(e);
        } finally {
            loader.close();
        }
        return groovyClass;
    }

}
