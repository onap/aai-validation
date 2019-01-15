/**
 * ============LICENSE_START===================================================
 * Copyright (c) 2018-2019 European Software Marketing Ltd.
 * ============================================================================
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
 * ============LICENSE_END=====================================================
 */
package org.onap.aai.validation.controller;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.inject.Inject;
import org.onap.aai.validation.Validator;
import org.onap.aai.validation.config.ValidationControllerConfig;
import org.onap.aai.validation.exception.ValidationServiceException;
import org.onap.aai.validation.logging.ApplicationMsgs;
import org.onap.aai.validation.logging.LogHelper;
import org.onap.aai.validation.publisher.MessagePublisher;
import org.onap.aai.validation.reader.EventReader;
import org.onap.aai.validation.reader.data.Entity;
import org.onap.aai.validation.result.ValidationResult;
import org.onap.aai.validation.result.ValidationResultBuilder;
import org.onap.aai.validation.result.Violation;
import org.onap.aai.validation.util.JsonUtil;

/**
 * Controls the execution (of validation of an event) for the various validation service components.
 */
public class ValidationController {

    private static LogHelper applicationLogger = LogHelper.INSTANCE;

    public static final String VALIDATION_ERROR_SEVERITY = "CRITICAL";

    private static final String VALIDATION_ERROR_CATEGORY = "CANNOT_VALIDATE";
    private static final String VALIDATION_ERROR_VIOLATIONTYPE = "NONE";

    /**
     * Result of the Controller executing validation of a single event. Either there is a set of ValidationResults or
     * instead an exception was handled.
     */
    public class Result {
        /**
         * A successful validation will produce a set of results.
         */
        Optional<List<ValidationResult>> validationResults = Optional.empty();

        /**
         * For an unsuccessful validation, we will record the error details.
         */
        private String errorText;

        /**
         * @return whether or not we have a set of validation results
         */
        public boolean validationSuccessful() {
            return validationResults.isPresent();
        }

        public List<ValidationResult> getValidationResults() {
            return validationResults.orElse(Collections.emptyList());
        }

        /**
         * @return a JSON string representing the first ValidationResult, or an empty string when there are no results
         */
        public String getValidationResultAsJson() {
            List<ValidationResult> resultsList = getValidationResults();
            if (resultsList.isEmpty()) {
                return "";
            } else {
                // Only one Validation Result is returned (as only one is expected)
                return JsonUtil.toJson(resultsList.get(0));
            }
        }

        public Optional<String> getErrorText() {
            return Optional.ofNullable(errorText);
        }

        private void handleException(String event, Exception rootException) {
            try {
                Entity entity = eventReader.getEntity(event);
                if (!entity.getIds().isEmpty() && eventReader.getEntityType(event).isPresent()
                        && entity.getResourceVersion().isPresent()) {
                    ValidationResult validationResult = new ValidationResultBuilder(eventReader, event).build();
                    // @formatter:off
					validationResult.addViolation(new Violation.Builder(entity)
						.category(VALIDATION_ERROR_CATEGORY)
						.severity(VALIDATION_ERROR_SEVERITY)
						.violationType(VALIDATION_ERROR_VIOLATIONTYPE)
						.errorMessage(rootException.getMessage())
						.build());
					// @formatter:on

                    validationResults = Optional.of(Collections.singletonList(validationResult));
                    publishValidationResults(validationResults);
                }
            } catch (Exception e) {
                errorText = e.getMessage();
                applicationLogger.error(ApplicationMsgs.CANNOT_VALIDATE_HANDLE_EXCEPTION_ERROR, e, event);
            }
        }
    }

    /**
     * Status Report for the Controller
     *
     */
    public class StatusReport {

        private Temporal reportTime = LocalDateTime.now();
        private long upTime = ChronoUnit.SECONDS.between(startTime, reportTime);
        private long upTimeDays = ChronoUnit.DAYS.between(startTime, reportTime);

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("Started at ");
            sb.append(startTime).append('\n').append("Up time ");
            if (upTimeDays > 0) {
                sb.append(upTimeDays).append(" days ");
            }
            sb.append(LocalTime.MIDNIGHT.plusSeconds(upTime).format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            sb.append('\n').append(formatStats(stats));
            return sb.toString();
        }

        /**
         * @return formatted statistics
         */
        private String formatStats(Statistics stats) {
            StringBuilder sb = new StringBuilder();
            formatStats(stats, "info", sb, "Info Service");
            formatStats(stats, "http", sb, "Validation REST API");
            formatStats(stats, "topic", sb, "Events Consumed");
            if (stats.reportedThrowable != null) {
                StringWriter sw = new StringWriter();
                stats.reportedThrowable.printStackTrace(new PrintWriter(sw));
                sb.append("Exception reported: ").append(sw.toString());
            }
            return sb.toString();
        }

        private void formatStats(Statistics stats, String eventSource, StringBuilder sb, String heading) {
            sb.append("\n").append(heading).append("\n");
            if (stats.keyValues(eventSource).isEmpty()) {
                sb.append("total=0\n");
            } else {
                for (String key : stats.keyValues(eventSource)) {
                    sb.append(key).append("=").append(stats.messageCount(eventSource, key)).append("\n");
                }
            }
        }
    }

    private ValidationControllerConfig validationControllerConfig;
    private EventReader eventReader;
    private Validator ruleDrivenValidator;
    private Validator modelDrivenValidator;
    private MessagePublisher messagePublisher;
    private LocalDateTime startTime;
    private Statistics stats;

    /**
     * Record of actions taken by the Controller
     *
     */
    private class Statistics {

        private Map<String, SortedMap<String, Integer>> sourceMap = new HashMap<>();
        private Throwable reportedThrowable;

        /**
         * Increment the message count for the composite key <eventSource, key>
         *
         * @param eventSource the source of the event - used for statistics reporting purposes
         * @param key the statistic to increment by one
         */
        private void incrementEventCount(String eventSource, String key) {
            Map<String, Integer> messagesConsumed = getMessageCountsMap(eventSource);
            int count = messagesConsumed.getOrDefault(key, 0);
            messagesConsumed.put(key, count + 1);
        }

        private Map<String, Integer> getMessageCountsMap(String eventSource) {
            return sourceMap.computeIfAbsent(eventSource, k -> new TreeMap<>());
        }

        /**
         * @param eventSource the source of the event
         * @return List<String> the keys for the specified eventSource
         */
        private List<String> keyValues(String eventSource) {
            Map<String, Integer> messagesConsumed = getMessageCountsMap(eventSource);
            return new ArrayList<>(messagesConsumed.keySet());
        }

        /*
         * return the count for the supplied event source and statistic key
         */
        private int messageCount(String eventSource, String key) {
            Map<String, Integer> messagesConsumed = getMessageCountsMap(eventSource);
            return messagesConsumed.getOrDefault(key, 0);
        }

    }

    /**
     * Constructs a new validation controller with the injected parameters.
     *
     * @param validationControllerConfig the configuration parameters for validation controllers
     * @param eventReader an object that can read events
     * @param ruleDrivenValidator a validator for validating rules
     * @param modelDrivenValidator a validator for validating model
     * @param messagePublisher an instance of a publisher for messages
     */
    @Inject
    public ValidationController(ValidationControllerConfig validationControllerConfig, EventReader eventReader,
            Validator ruleDrivenValidator, Validator modelDrivenValidator, MessagePublisher messagePublisher) {
        this.startTime = LocalDateTime.now();
        this.validationControllerConfig = validationControllerConfig;
        this.eventReader = eventReader;
        this.ruleDrivenValidator = ruleDrivenValidator;
        this.modelDrivenValidator = modelDrivenValidator;
        this.messagePublisher = messagePublisher;
        this.stats = new Statistics();
    }

    /**
     * @throws ValidationServiceException if an error occurs initialising the controller
     */
    public void initialise() throws ValidationServiceException {
        ruleDrivenValidator.initialise();
        modelDrivenValidator.initialise();
    }

    /**
     * Validates the event and publishes the results of the validation onto the topic configured in the message
     * publisher.
     *
     * @param event the event to be validated
     * @param eventSource the source of the event
     * @return Result a result containing either the set of ValidationResults or an error message
     */
    public Result execute(String event, String eventSource) {
        Result result = new Result();
        try {
            stats.incrementEventCount(eventSource, "total");
            if (isEndEvent(event)) {
                applicationLogger.debug("Event has not been processed. End event type was detected. Event :" + event);
                stats.incrementEventCount(eventSource, "end");
            } else if (isValidationCandidate(event)) {
                result.validationResults = dispatchEvent(event, eventSource);
                publishValidationResults(result.validationResults);
            } else {
                stats.incrementEventCount(eventSource, "filtered");
                applicationLogger.info(ApplicationMsgs.FILTERED_EVENT,event);
            }
        } catch (Exception e) {
            applicationLogger.error(ApplicationMsgs.CANNOT_VALIDATE_ERROR, e, event);
            stats.incrementEventCount(eventSource, "errored");
            result.handleException(event, e);
        }
        return result;
    }

    private void publishValidationResults(Optional<List<ValidationResult>> validationResults) {
        if (validationResults.isPresent()) {
            for (ValidationResult validationResult : validationResults.get()) {
                try {
                    messagePublisher.publishMessage(validationResult.toJson());
                } catch (ValidationServiceException e) {
                    applicationLogger.error(ApplicationMsgs.MESSAGE_PUBLISH_ERROR, e, validationResult.toString());
                }
            }
        }
    }

    private Optional<List<ValidationResult>> dispatchEvent(String event, String eventSource)
            throws ValidationServiceException {
        List<ValidationResult> validationResults = null;
        Optional<String> eventType = eventReader.getEventType(event);

        applicationLogger.debug("Event consumed: " + event);

        if (eventType.isPresent()) {
            if (isRuleDriven(eventType.get())) {
                validationResults = ruleDrivenValidator.validate(event);
                stats.incrementEventCount(eventSource, "rule");
            } else if (isModelDriven(eventType.get())) {
                validationResults = modelDrivenValidator.validate(event);
                stats.incrementEventCount(eventSource, "model");
            } else {
                applicationLogger.info(ApplicationMsgs.INVALID_EVENT_TYPE, event);
                stats.incrementEventCount(eventSource, "invalid");
            }
        } else {
            applicationLogger.info(ApplicationMsgs.MISSING_EVENT_TYPE, event);
            stats.incrementEventCount(eventSource, "missing event type");
        }

        return Optional.ofNullable(validationResults);
    }

    private Boolean isRuleDriven(String eventType) {
        return validationControllerConfig.getEventTypeRule().contains(eventType);
    }

    private Boolean isModelDriven(String eventType) {
        return validationControllerConfig.getEventTypeModel().contains(eventType);
    }

    private boolean isEndEvent(String event) throws ValidationServiceException {
        Optional<String> eventType = eventReader.getEventType(event);

        return eventType.isPresent() && "END-EVENT".equalsIgnoreCase(eventType.get());
    }

    private Boolean isDomainValid(String event) throws ValidationServiceException {
        Optional<String> eventDomain = eventReader.getEventDomain(event);

        // Domain is optional in Event Header
        return !eventDomain.isPresent()
                || validationControllerConfig.getEventDomain().equalsIgnoreCase(eventDomain.get());
    }

    private Boolean isNotExcludedAction(String event) throws ValidationServiceException {
        Optional<String> eventAction = eventReader.getEventAction(event);

        // Action is optional in Event Header
        return !eventAction.isPresent()
                || !validationControllerConfig.getExcludedEventActions().contains(eventAction.get());
    }


    private Boolean isValidationCandidate(String event) throws ValidationServiceException {
        return isDomainValid(event) && isNotExcludedAction(event);
    }

    /**
     * @return a formatted string containing status information
     */
    public StatusReport statusReport() {
        return new StatusReport();
    }

    /**
     * Record a Throwable which will then be added to the status reporting text.
     *
     * @param t a Throwable to be reported (on demand)
     */
    public void recordThrowable(Throwable t) {
        stats.reportedThrowable = t;
    }

    public void incrementInfoCount() {
        stats.incrementEventCount("info", "total");
    }

}
