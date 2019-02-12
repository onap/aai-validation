/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2018-2019 AT&T Intellectual Property. All rights reserved.
 * Copyright © 2018-2019 European Software Marketing Ltd.
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

package org.onap.aai.validation.publisher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.inject.Inject;
import org.onap.aai.event.client.DMaaPEventPublisher;
import org.onap.aai.validation.config.TopicAdminConfig;
import org.onap.aai.validation.config.TopicConfig;
import org.onap.aai.validation.config.TopicConfig.Topic;
import org.onap.aai.validation.exception.ValidationServiceError;
import org.onap.aai.validation.exception.ValidationServiceException;
import org.onap.aai.validation.factory.DMaaPEventPublisherFactory;
import org.onap.aai.validation.logging.ApplicationMsgs;
import org.onap.aai.validation.logging.LogHelper;

/**
 * Event Publisher
 *
 */
public class ValidationEventPublisher implements MessagePublisher {

    private static LogHelper applicationLogger = LogHelper.INSTANCE;

    private List<Topic> publisherTopics;

    private boolean enablePublishing;

    private long retries;

    private long retriesRemaining;

    private DMaaPEventPublisherFactory dMaapFactory;


    /**
     * Instantiates an Event Publisher instance using properties from config file.
     *
     * @param topicConfig
     * @param topicAdminConfig
     */
    @Inject
    public ValidationEventPublisher(TopicConfig topicConfig, TopicAdminConfig topicAdminConfig) {
        enablePublishing = topicAdminConfig.isPublishEnable();
        if (enablePublishing) {
            publisherTopics = topicConfig.getPublisherTopics();
            retries = topicAdminConfig.getPublishRetries();
        }
        dMaapFactory = new DMaaPEventPublisherFactory();
    }

    /**
     * Connect to the event publisher, add the message, and then publish it by closing the publisher.
     */
    @Override
    public void publishMessage(String message) throws ValidationServiceException {
        Collection<String> messages = new ArrayList<>();
        messages.add(message);
        publishMessages(messages);
    }

    /**
     * Connect to the event publisher, adds the messages, and then publish them by closing the publisher.
     */
    @Override
    public void publishMessages(Collection<String> messages) throws ValidationServiceException {
        if (!enablePublishing) {
            return;
        } else {
            applicationLogger.debug("Publishing messages: " + messages);
            for (Topic topic : publisherTopics) {
                retriesRemaining = retries;
                publishMessages(messages, topic);
            }
        }
    }

    private void publishMessages(Collection<String> messages, Topic topic) throws ValidationServiceException {

        DMaaPEventPublisher dMaapEventPublisher = dMaapFactory.createEventPublisher(topic.getHost(), topic.getName(),
                topic.getUsername(), topic.getPassword(), topic.getTransportType());

        try {
            // Add our message to the publisher's queue/bus
            int result = dMaapEventPublisher.sendSync(topic.getPartition(), messages);
            if (result != messages.size()) {
                applicationLogger.warn(ApplicationMsgs.UNSENT_MESSAGE_WARN);
                closeEventPublisher(dMaapEventPublisher);
                retryOrThrow(messages, topic, new ValidationServiceException(
                        ValidationServiceError.EVENT_CLIENT_INCORRECT_NUMBER_OF_MESSAGES_SENT, result));
            }
        } catch (Exception e) {
            applicationLogger.error(ApplicationMsgs.UNSENT_MESSAGE_ERROR);
            closeEventPublisher(dMaapEventPublisher);
            retryOrThrow(messages, topic,
                    new ValidationServiceException(ValidationServiceError.EVENT_CLIENT_SEND_ERROR, e));
        }

        completeMessageSending(dMaapEventPublisher, topic);
    }

    /**
     * Publish the queued messages by closing the publisher.
     *
     * @param eventPublisher
     *            the publisher to close
     * @throws AuditException
     */
    private void completeMessageSending(DMaaPEventPublisher eventPublisher, Topic topic)
            throws ValidationServiceException {
        List<String> unsentMsgs = closeEventPublisher(eventPublisher);

        if (unsentMsgs != null && !unsentMsgs.isEmpty()) {
            // Log the error, as the exception will not be propagated due to the fact that the Cambria Client throws
            // an exception first in a separate thread.
            applicationLogger.error(ApplicationMsgs.EVENT_CLIENT_CLOSE_UNSENT_MESSAGE,
                    ValidationServiceError.EVENT_CLIENT_CLOSE_UNSENT_MESSAGE.getMessage(unsentMsgs));

            retryOrThrow(unsentMsgs, topic, new ValidationServiceException(
                    ValidationServiceError.EVENT_CLIENT_CLOSE_UNSENT_MESSAGE, unsentMsgs));
        }
    }

    private void retryOrThrow(Collection<String> messages, Topic topic, ValidationServiceException exceptionToThrow)
            throws ValidationServiceException {
        if (retriesRemaining <= 0) {
            applicationLogger.warn(ApplicationMsgs.SEND_MESSAGE_ABORT_WARN);
            throw exceptionToThrow;
        } else {
            applicationLogger.warn(ApplicationMsgs.SEND_MESSAGE_RETRY_WARN);
            retriesRemaining--;
            publishMessages(messages, topic);
        }
    }

    private List<String> closeEventPublisher(DMaaPEventPublisher eventPublisher) throws ValidationServiceException {
        try {
            return eventPublisher.closeWithUnsent();
        } catch (Exception e) {
            throw new ValidationServiceException(ValidationServiceError.EVENT_CLIENT_CLOSE_ERROR, e);
        }
    }

    public void setEventPublisherFactory(DMaaPEventPublisherFactory dMaapFactory) {
        this.dMaapFactory = dMaapFactory;
    }

}
