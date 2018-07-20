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
package org.onap.aai.validation.itest.util;

import org.onap.aai.event.client.DMaaPEventConsumer;
import org.onap.aai.event.client.DMaaPEventPublisher;
import org.onap.aai.validation.config.TopicConfig.Topic;
import com.google.common.collect.Iterators;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TopicUtils {

    TopicUtils() {
        // Deliberately empty
    }

    /**
     * Queries the topics for the specified seconds until the matching string message is found. Returns true if a
     * message is found, false if not found.
     *
     * @param maxSeconds
     * @param regex
     * @param topics
     * @return
     * @throws Exception
     */
    public static boolean checkEverySecondForMessageOnTopics(int maxSeconds, String regex, List<Topic> topics)
            throws Exception {
        for (int i = 0; i < maxSeconds; i++) {
            TimeUnit.SECONDS.sleep(1);

            Collection<String> messages = new ArrayList<>();
            for (Topic topic : topics) {
                Iterators.addAll(messages, consumeEventBusMessage(topic).iterator());
            }
            // Check that the messages have been consumed.
            for (String message : messages) {
                if (message.matches(regex)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @param maxSeconds
     * @param topics
     * @return
     * @throws Exception
     */
    public static boolean checkEverySecondForNoMessageOnTopics(int maxSeconds, List<Topic> topics) throws Exception {
        boolean noMessageOnTopic = true;
        // any string pattern
        if (checkEverySecondForMessageOnTopics(maxSeconds, "(.*)", topics)) {
            noMessageOnTopic = false;
        }
        return noMessageOnTopic;
    }

    /**
     * @param topic
     * @return
     * @throws Exception
     */
    public static Iterable<String> consumeEventBusMessage(Topic topic) throws Exception {
        DMaaPEventConsumer consumer = new DMaaPEventConsumer(topic.getHost(), topic.getName(), topic.getUsername(),
                topic.getPassword(), topic.getConsumerGroup(), topic.getConsumerId(),
                DMaaPEventConsumer.DEFAULT_MESSAGE_WAIT_TIMEOUT, DMaaPEventConsumer.DEFAULT_MESSAGE_LIMIT,
                topic.getTransportType());
        return consumer.consume();
    }

    /**
     * @param messages
     * @param topic
     * @return
     * @throws Exception
     */
    public static int publishEventBusMessage(Collection<String> messages, Topic topic) throws Exception { // NOSONAR
        DMaaPEventPublisher publisher = new DMaaPEventPublisher(topic.getHost(), topic.getName(), topic.getUsername(),
                topic.getPassword(), DMaaPEventPublisher.DEFAULT_BATCH_SIZE, DMaaPEventPublisher.DEFAULT_BATCH_AGE,
                DMaaPEventPublisher.DEFAULT_BATCH_DELAY, topic.getTransportType());
        int sent = publisher.sendSync(topic.getPartition(), messages);
        publisher.close();
        return sent;
    }
}
