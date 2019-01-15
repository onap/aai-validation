/*
 * ============LICENSE_START===================================================
 * Copyright (c) 2018-2019 European Software Marketing Ltd.
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.apache.commons.lang.StringUtils;
import org.onap.aai.validation.Validator;
import org.onap.aai.validation.config.RuleIndexingConfig;
import org.onap.aai.validation.exception.ValidationServiceError;
import org.onap.aai.validation.exception.ValidationServiceException;
import org.onap.aai.validation.logging.ApplicationMsgs;
import org.onap.aai.validation.logging.LogHelper;
import org.onap.aai.validation.reader.EventReader;
import org.onap.aai.validation.reader.OxmReader;
import org.onap.aai.validation.reader.data.AttributeValues;
import org.onap.aai.validation.reader.data.Entity;
import org.onap.aai.validation.result.ValidationResult;
import org.onap.aai.validation.result.ValidationResultBuilder;
import org.onap.aai.validation.result.Violation;
import org.onap.aai.validation.result.Violation.ViolationType;
import org.onap.aai.validation.ruledriven.configuration.EntitySection;
import org.onap.aai.validation.ruledriven.configuration.GroovyConfigurationException;
import org.onap.aai.validation.ruledriven.configuration.RulesConfigurationLoader;
import org.onap.aai.validation.ruledriven.rule.Rule;
import org.onap.aai.validation.ruledriven.rule.RuleResult;

/**
 * Validator using explicit rules
 *
 */
public class RuleDrivenValidator implements Validator {

    private static LogHelper applicationLogger = LogHelper.INSTANCE;

    private static final String RULES_CONFIG_FILE_SUFFIX = ".groovy";

    private Path configurationPath;
    private OxmReader oxmReader;
    private EventReader eventReader;
    private Optional<RuleIndexingConfig> ruleIndexingConfig;
    // Map of event type name against RuleManager for that event type
    private Map<String, RuleManager> ruleManagers;


    /**
     * Construct a Validator that is configured using rule files
     *
     * @param configurationPath
     *        path to the Groovy rules files
     * @param oxmReader
     *        required for validating entity types
     * @param eventReader
     *        a reader for extracting entities from each event to be validated
     */
    public RuleDrivenValidator(final Path configurationPath, final OxmReader oxmReader, final EventReader eventReader,
            final RuleIndexingConfig ruleIndexingConfig) {
        this.configurationPath = configurationPath;
        this.oxmReader = oxmReader;
        this.eventReader = eventReader;
        this.ruleIndexingConfig = Optional.ofNullable(ruleIndexingConfig);
        this.ruleManagers = null;
    }

    @Override
    public void initialise() throws ValidationServiceException {
        ruleManagers = new HashMap<>();
        for (String eventType : getSupportedEventTypes()) {
            ruleManagers.put(eventType.toLowerCase(Locale.getDefault()), loadRulesConfiguration(eventType));
        }
        validateRulesConfiguration();
    }

    private RuleManager loadRulesConfiguration(String eventType) throws ValidationServiceException {
        StringBuilder rulesText = new StringBuilder();
        try (Stream<Path> paths = Files.find(configurationPath.resolve(eventType), 1,
                (path, basicFileAttributes) -> path.toFile().getName().matches(".*\\" + RULES_CONFIG_FILE_SUFFIX));) {
            paths.forEach(appendFileContent(rulesText));
        } catch (IOException e) {
            throw new ValidationServiceException(ValidationServiceError.RULES_FILE_ERROR,
                    configurationPath.toAbsolutePath(), e);
        }

        try {
            return RulesConfigurationLoader.loadConfiguration(rulesText.toString());
        } catch (GroovyConfigurationException e) {
            throw new ValidationServiceException(ValidationServiceError.RULES_FILE_ERROR, e,
                    configurationPath.toAbsolutePath() + File.separator + "*" + RULES_CONFIG_FILE_SUFFIX);
        }
    }

    private void validateRulesConfiguration() throws ValidationServiceException {
        for (RuleManager ruleManager : ruleManagers.values()) {
            for (EntitySection entity : ruleManager.getEntities()) {
                if (ruleIndexingConfig.isPresent() && ruleIndexingConfig.get().skipOxmValidation(entity.getName())) {
                    continue;
                }
                if (oxmReader != null && oxmReader.getPrimaryKeys(entity.getName()).isEmpty()) {
                    throw new ValidationServiceException(ValidationServiceError.OXM_MISSING_KEY,
                            entity.getName() + " defined in " + configurationPath.toAbsolutePath() + File.separator
                                    + "*" + RULES_CONFIG_FILE_SUFFIX);
                }
            }
        }
    }

    /**
     * Helper method to expose the configured rules. This simplifies testing of the validator.
     *
     * @param entityType
     * @param eventType
     * @return the Optional rules defined for this entityType
     */
    public Optional<List<Rule>> getRulesForEntity(String entityType, String eventType) {
        return ruleManagers.get(eventType.toLowerCase(Locale.getDefault())).getRulesForEntity(entityType);
    }

    /*
     * (non-Javadoc)
     * @see org.onap.aai.validation.Validator#validate(java.lang.String)
     */
    @Override
    public List<ValidationResult> validate(String event) throws ValidationServiceException {
        List<ValidationResult> validationResults = new ArrayList<>();

        Entity entity = getEventReader().getEntity(event);
        Optional<String> eventType = eventReader.getEventType(event);
        List<Rule> rules = getRulesToApply(entity, eventType).orElseThrow(
                () -> new ValidationServiceException(ValidationServiceError.RULES_NOT_DEFINED, eventType.orElse(null)));

        ValidationResult validationResult = new ValidationResultBuilder(eventReader, event).build();
        Violation.Builder builder = new Violation.Builder(entity);

        for (Rule rule : rules) {
            AttributeValues attributeValues = entity.getAttributeValues(rule.getAttributePaths());

            // Execute the rule for this particular set of attribute values.
            RuleResult result = null;
            try {
                result = rule.execute(attributeValues);
            } catch (IllegalArgumentException e) {
                throw new ValidationServiceException(ValidationServiceError.RULE_EXECUTION_ERROR, e, rule,
                        attributeValues);
            }

            applicationLogger.debug(String.format("%s|%s|\"%s\"|%s", entity.getType(), entity.getIds(), rule.getName(),
                    result.getSuccess() ? "pass" : "fail"));

            if (!result.getSuccess()) {
                String errorMessage =
                        MessageFormat.format(rule.getErrorMessage(), result.getErrorArguments().toArray());

                //@formatter:off
                Violation violation = builder
                        .category(rule.getErrorCategory())
                        .severity(rule.getSeverity())
                        .violationType(ViolationType.RULE)
                        .validationRule(rule.getName())
                        .violationDetails(attributeValues.generateReport())
                        .errorMessage(errorMessage)
                        .build();
                //@formatter:on

                validationResult.addViolation(violation);
            }
        }
        validationResults.add(validationResult);

        return validationResults;
    }

    private Optional<List<Rule>> getRulesToApply(Entity entity, Optional<String> eventType)
            throws ValidationServiceException {
        Optional<List<Rule>> rules = Optional.empty();
        if (eventType.isPresent()) {
            Optional<RuleManager> ruleManager = getRuleManager(eventType.get().toLowerCase(Locale.getDefault()));
            if (ruleManager.isPresent()) {
                if (ruleIndexingConfig.isPresent() && ruleIndexingConfig.get().getIndexedEvents() != null
                        && ruleIndexingConfig.get().getIndexedEvents().contains(eventType.get())) {
                    rules = getRulesByIndex(entity, eventType.get(), ruleManager.get());
                } else {
                    rules = ruleManager.get().getRulesForEntity(entity.getType());
                }
            }
        }
        return rules;
    }

    private Optional<List<Rule>> getRulesByIndex(Entity entity, String eventType, RuleManager ruleManager) {
        String rulesKey = generateKey(entity, eventType);
        applicationLogger.debug(String.format("Retrieving indexed rules for key '%s'", rulesKey));

        final Optional<List<Rule>> entityRules = ruleManager.getRulesForEntity(rulesKey);
        final boolean rulesDefined = entityRules.filter(l -> !l.isEmpty()).isPresent();

        if (!rulesDefined && ruleIndexingConfig.isPresent()) {
            final String defaultIndexKey = ruleIndexingConfig.get().getDefaultIndexKey();
            if (StringUtils.isEmpty(defaultIndexKey)) {
                return ruleManager.getRulesForEntity(RuleManager.generateKey(new String[] { defaultIndexKey }));
            } else {
                applicationLogger.debug("Default index value not configured, unable to get rules");
                applicationLogger.error(ApplicationMsgs.CANNOT_VALIDATE_ERROR, eventType);
            }
        }

        return entityRules;
    }

    private String generateKey(Entity entity, String eventType) {
        if (!ruleIndexingConfig.isPresent() || ruleIndexingConfig.get().getIndexAttributes() == null
                || ruleIndexingConfig.get().getIndexAttributes().isEmpty()) {
            applicationLogger.debug(String.format(
                    "Event '%s' is configured to use indexed rules but indexing attributes are not configured",
                    eventType));
            return "";
        }
        try {
            AttributeValues attributeValues = entity.getAttributeValues(ruleIndexingConfig.get().getIndexAttributes());
            applicationLogger
                    .debug("Generating index using attributes: " + attributeValues.generateReport().toString());
            Collection<Object> values = attributeValues.generateReport().values();
            return RuleManager.generateKey(values.stream().toArray(String[]::new));
        } catch (ValidationServiceException e) {
            applicationLogger.debug("Failed to retrieve index key attributes from event: " + e.getMessage());
            applicationLogger.error(ApplicationMsgs.CANNOT_VALIDATE_ERROR, e, eventType);
            return "";
        }
    }

    private EventReader getEventReader() {
        return this.eventReader;
    }

    private Optional<RuleManager> getRuleManager(String eventType) throws ValidationServiceException {
        if (ruleManagers == null) {
            initialise();
        }
        return Optional.ofNullable(ruleManagers.get(eventType));
    }

    /**
     * Read the text content of the specified Path and append this to the specified String
     *
     * @param sb
     *        StringBuilder for the rule configuration text
     * @return a Consumer function that appends file content
     */
    private Consumer<? super Path> appendFileContent(StringBuilder sb) {
        return path -> {
            try {
                for (String line : Files.readAllLines(path)) {
                    sb.append(line).append("\n");
                }
            } catch (IOException e) {
                applicationLogger.error(ApplicationMsgs.READ_FILE_ERROR, e, path.toString());
            }
        };
    }

    private Collection<String> getSupportedEventTypes() {
        String[] list = configurationPath.toFile().list((current, name) -> new File(current, name).isDirectory());
        return list == null ? Collections.emptyList() : Arrays.asList(list);
    }
}
