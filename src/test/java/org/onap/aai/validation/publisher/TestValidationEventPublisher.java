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
package org.onap.aai.validation.publisher;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.onap.aai.event.client.DMaaPEventPublisher;
import org.onap.aai.validation.config.TopicAdminConfig;
import org.onap.aai.validation.config.TopicConfig;
import org.onap.aai.validation.config.TopicConfig.Topic;
import org.onap.aai.validation.factory.DMaaPEventPublisherFactory;
import org.onap.aai.validation.publisher.ValidationEventPublisher;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TestValidationEventPublisher {

    static {
        System.setProperty("AJSC_HOME", ".");
        System.setProperty("consumer.topic.names", "poa-rule-validation");
        System.setProperty("publisher.topic.names", "poa-audit-result");
    }

    private DMaaPEventPublisher mockEventPublisher;
    private ValidationEventPublisher validationEventPublisher;
    private List<Topic> topicList = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        TopicConfig mockTopicConfig = Mockito.mock(TopicConfig.class);
        TopicAdminConfig mockTopicAdminConfig = Mockito.mock(TopicAdminConfig.class);
        when(mockTopicAdminConfig.isPublishEnable()).thenReturn(true);

        Topic topic1 = new TopicConfig("poa-rule-validation","poa-audit-result").new Topic();
        topic1.setName("aai-data-integrity");
        topic1.setHost("integrity-dummy-host");
        topic1.setPartition("integrity-dummy-partition");
        topic1.setUsername("integrity-dummy-username");
        topic1.setPassword("integrity-dummy-password");
        topic1.setTransportType("integrity-dummy-transport-type");
        topicList.add(topic1);

        when(mockTopicConfig.getPublisherTopics()).thenReturn(topicList);
        validationEventPublisher = new ValidationEventPublisher(mockTopicConfig, mockTopicAdminConfig);

        mockEventPublisher = Mockito.mock(DMaaPEventPublisher.class);
        when(mockEventPublisher.closeWithUnsent()).thenReturn(new ArrayList<>());

        DMaaPEventPublisherFactory mockEventPublisherFactory = Mockito.mock(DMaaPEventPublisherFactory.class);
        when(mockEventPublisherFactory.createEventPublisher(any(), any(), any(), any(), any()))
                .thenReturn(mockEventPublisher);

        validationEventPublisher.setEventPublisherFactory(mockEventPublisherFactory);
    }

    @Test
    public void testPublishMessages() throws Exception {
        Collection<String> messages = new ArrayList<>();
        messages.add("first test message");
        messages.add("second test message");
        when(mockEventPublisher.sendSync(any(String.class), Mockito.<Collection<String>>any())).thenReturn(2);

        validationEventPublisher.publishMessages(messages);
        verify(mockEventPublisher, times(1)).sendSync(topicList.get(0).getPartition(), messages);
    }

    @Test
    public void testPublishMessage() throws Exception {
        Collection<String> messages = new ArrayList<>();
        messages.add("first test message");
        when(mockEventPublisher.sendSync(any(String.class), Mockito.<Collection<String>>any())).thenReturn(1);

        validationEventPublisher.publishMessage(messages.iterator().next());
        verify(mockEventPublisher, times(1)).sendSync(topicList.get(0).getPartition(), messages);
    }

}
