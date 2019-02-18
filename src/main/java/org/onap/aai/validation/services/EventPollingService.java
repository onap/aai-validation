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
package org.onap.aai.validation.services;

import com.google.common.collect.Iterables;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import org.onap.aai.event.client.DMaaPEventConsumer;
import org.onap.aai.validation.config.TopicConfig;
import org.onap.aai.validation.config.TopicConfig.Topic;
import org.onap.aai.validation.controller.ValidationController;
import org.onap.aai.validation.exception.ValidationServiceError;
import org.onap.aai.validation.exception.ValidationServiceException;
import org.onap.aai.validation.factory.DMaaPEventConsumerFactory;
import org.onap.aai.validation.logging.ApplicationMsgs;
import org.onap.aai.validation.logging.LogHelper;
import org.onap.aai.validation.logging.LogHelper.MdcParameter;
import org.springframework.stereotype.Service;

/**
 * Event Polling Service
 *
 */
@Service
public class EventPollingService implements Runnable {

    private static final LogHelper applicationLogger = LogHelper.INSTANCE;

    private List<DMaaPEventConsumer> consumers;

    private ValidationController validationController;

    /**
     * Instantiates an EventPollingService instance using the supplied configuration.
     *
     * @param topicConfig
     * @throws ValidationServiceException
     */
    @Inject
    public EventPollingService(TopicConfig topicConfig) throws ValidationServiceException {
        consumers = new ArrayList<>();
        DMaaPEventConsumerFactory factory = new DMaaPEventConsumerFactory();
        for (Topic topic : topicConfig.getConsumerTopics()) {
            try {
                consumers.add(factory.createEventConsumer(topic.getHost(), topic.getName(), topic.getUsername(),
                        topic.getPassword(), topic.getConsumerGroup(), topic.getConsumerId(), topic.getTransportType(),
                        topic.getProtocol()));
            } catch (MalformedURLException e) {
                throw new ValidationServiceException(ValidationServiceError.EVENT_CLIENT_CONSUMER_INIT_ERROR, e);
            }
        }
    }

    @Override
    public void run() {
        applicationLogger.info(ApplicationMsgs.POLL_EVENTS);
        try {
            for (DMaaPEventConsumer consumer : consumers) {
                for (String event : consumeEvents(consumer)) {
                    // The event does not have a transaction ID so create one for logging purposes
                    applicationLogger.setContextValue(MdcParameter.REQUEST_ID, UUID.randomUUID().toString());
                    validationController.execute(event, "topic");
                }
            }
        } catch (Exception e) {
            // This could be a temporary issue, so the exception is swallowed
            applicationLogger.error(ApplicationMsgs.INVOKE_EVENT_CONSUMER_ERROR, e);
        } catch (Throwable t) { // NOSONAR
            // E.g. We may catch an IllegalArgumentException caused by invalid configuration
            applicationLogger.error(ApplicationMsgs.INVOKE_EVENT_CONSUMER_ERROR, t);

            // Add these details to the status report available via the controller
            validationController.recordThrowable(t);

            // For non IO exceptions, rethrow in order to cause the executor to stop scheduling
            throw t;
        } finally {
            applicationLogger.clearContextValue(MdcParameter.REQUEST_ID);
        }
    }

    /* Getters and Setters for Spring injection */

    public ValidationController getValidationController() {
        return validationController;
    }

    public void setValidationController(ValidationController validationController) throws ValidationServiceException {
        this.validationController = validationController;
        // Instruct the validation controller to load and validate its configuration
        this.validationController.initialise();
    }

    private Iterable<String> consumeEvents(DMaaPEventConsumer consumer) throws Exception {
        applicationLogger.clearContextValue(MdcParameter.REQUEST_ID);
        applicationLogger.debug("Querying consumer " + consumer);
        Iterable<String> events = consumer.consume();
        applicationLogger.info(ApplicationMsgs.NUMBER_OF_MESSAGES_CONSUMED, Integer.toString(Iterables.size(events)));
        return events;
    }
}
