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
package org.onap.aai.validation.config;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Properties;
import javax.inject.Inject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.aai.validation.config.TopicConfig.Topic;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@Import(TopicPropertiesConfig.class)
@TestPropertySource(locations = {"classpath:test-application.properties"})
@ContextConfiguration(locations = {"classpath:topic-config/test-validation-service-beans.xml"})
public class TestTopicConfig {

    static {
        System.setProperty("APP_HOME", ".");
    }

    @Inject
    private TopicConfig topicConfig;

    @Inject
    private Properties topicProperties;

    @Test
    public void testGetTopicProperties() throws Exception {
        assertThat(topicProperties.getProperty("aai-event.name"), is("aai-event"));
        assertThat(topicProperties.getProperty("aai-data-export.name"), is("aai-data-export"));
    }

    @Test
    public void testGetConsumerTopicsFromTopicConfig() throws Exception {
        assertThat(topicConfig.getConsumerTopicNames(), containsInAnyOrder("aai-event", "aai-data-export"));
    }

    @Test
    public void testGetConsumerTopicConfigurationObjects() throws Exception {
        Topic eventTopic = new TopicConfig("aai-event", "aai-data-integrity").new Topic();
        eventTopic.setName("aai-event");
        eventTopic.setHost("event-dummy-host");
        eventTopic.setUsername("event-dummy-username");
        eventTopic.setPassword("event-dummy-password");
        eventTopic.setConsumerGroup("event-dummy-consumer-group");
        eventTopic.setConsumerId("event-dummy-consumer-id");
        eventTopic.setTransportType("event-dummy-transport-type");
        eventTopic.setProtocol("event-dummy-protocol-type");

        Topic exportTopic = new TopicConfig("aai-data-export", "aai-data-integrity").new Topic();
        exportTopic.setName("aai-data-export");
        exportTopic.setHost("export-dummy-host");
        exportTopic.setUsername("export-dummy-username");
        exportTopic.setPassword("export-dummy-password");
        exportTopic.setConsumerGroup("export-dummy-consumer-group");
        exportTopic.setConsumerId("export-dummy-consumer-id");
        exportTopic.setTransportType("export-dummy-transport-type");

        List<Topic> consumerTopics = topicConfig.getConsumerTopics();

        assertThat(consumerTopics, containsInAnyOrder(eventTopic, exportTopic));
    }

    @Test
    public void testGetPublisherTopicConfigurationObjects() throws Exception {
        Topic integrityTopic = new TopicConfig("aai-data-export", "aai-data-integrity").new Topic();
        integrityTopic.setName("aai-data-integrity");
        integrityTopic.setHost("integrity-dummy-host");
        integrityTopic.setPartition("integrity-dummy-partition");
        integrityTopic.setUsername("integrity-dummy-username");
        integrityTopic.setPassword("integrity-dummy-password");
        integrityTopic.setTransportType("integrity-dummy-transport-type");
        integrityTopic.setProtocol("http");

        List<Topic> publisherTopics = topicConfig.getPublisherTopics();

        assertThat(publisherTopics, containsInAnyOrder(integrityTopic));
    }
}
