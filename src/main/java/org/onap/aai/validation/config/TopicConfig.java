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
package org.onap.aai.validation.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import javax.annotation.Resource;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Gets the configuration of the topics. The topics are configured using Spring in topic-config-beans.xml.
 */

@Component("TopicConfig")
public class TopicConfig {

    private List<String> consumerTopicNames;

    private List<String> publisherTopicNames;

    @Resource(name = "topicProperties")
    private Properties topicProperties;

    List<Topic> consumerTopics = new ArrayList<>();
    List<Topic> publisherTopics = new ArrayList<>();

    @Autowired
    public TopicConfig (@Value("${consumer.topic.names}") final String consumerNames, @Value("${publisher.topic.names}") final String publisherNames){
        consumerTopicNames = Arrays.asList(consumerNames.split(","));
        publisherTopicNames = Arrays.asList(publisherNames.split(","));
    }

    /**
     * Gets the configuration of topics for consumption.
     *
     * @return a list of topic configurations.
     */
    public List<Topic> getConsumerTopics()
    {
        return populateTopics(consumerTopics, consumerTopicNames);
    }


    /**
     * Gets the configuration of topics for publishing.
     *
     * @return a list of topic configurations.
     */
    public List<Topic> getPublisherTopics() {
        return populateTopics(publisherTopics, publisherTopicNames);
    }

    /**
	 * Populates the topics list with topic objects created from each item in the topicNames list.
	 *
	 * @param topics
	 *            The topic list to populate.
	 * @param topicNames
	 *            The list of topic names to populate the topic list with.
	 * @return The populated topic list.
	 */
	private List<Topic> populateTopics(List<Topic> topics, List<String> topicNames) {
		if (topics.isEmpty()) {
			for (String topicName : topicNames) {
				Topic topicConfig = new Topic();
				topicConfig.setName(getTopicProperties().getProperty(topicName + ".name"));
				topicConfig.setHost(getTopicProperties().getProperty(topicName + ".host"));
				topicConfig.setUsername(getTopicProperties().getProperty(topicName + ".username"));
				topicConfig.setPassword(getTopicProperties().getProperty(topicName + ".password"));
				topicConfig.setPartition(getTopicProperties().getProperty(topicName + ".publisher.partition"));
				topicConfig.setConsumerGroup(getTopicProperties().getProperty(topicName + ".consumer.group"));
				topicConfig.setConsumerId(getTopicProperties().getProperty(topicName + ".consumer.id"));
				topicConfig.setTransportType(getTopicProperties().getProperty(topicName + ".transport.type"));
				topics.add(topicConfig);
			}
		}
		return topics;
	}

    public List<String> getConsumerTopicNames() {
        return consumerTopicNames;
    }

    public void setConsumerTopicNames(List<String> consumerTopicNames) {
        this.consumerTopicNames = consumerTopicNames;
    }

    public List<String> getPublisherTopicNames() {
        return publisherTopicNames;
    }

    public void setPublisherTopicNames(List<String> publisherTopicNames) {
        this.publisherTopicNames = publisherTopicNames;
    }

    public Properties getTopicProperties() {
        return topicProperties;
    }

    public void setTopicProperties(Properties topicProperties) {
        this.topicProperties = topicProperties;
    }

    /**
     * Defines the properties of a single topic for consumption.
     */
    public class Topic {
        private String name;
        private String host;
        private String username;
        private String password;
        private String partition;
        private String consumerGroup;
        private String consumerId;
        private String transportType;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
        	return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getPartition() {
            return partition;
        }

        public void setPartition(String partition) {
            this.partition = partition;
        }

        public String getConsumerGroup() {
            return consumerGroup;
        }

        public void setConsumerGroup(String consumerGroup) {
            this.consumerGroup = consumerGroup;
        }

        public String getConsumerId() {
            return consumerId;
        }

        public void setConsumerId(String consumerId) {
            this.consumerId = consumerId;
        }

        public List<String> getHosts() {
            return Arrays.asList(host.split(","));
        }

        public String getTransportType() {
            return transportType;
        }

        public void setTransportType(String transportType) {
            this.transportType = transportType;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.consumerGroup, this.consumerId, this.host, this.username, this.name, this.partition,
                    this.password, this.transportType);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Topic)) {
                return false;
            } else if (obj == this) {
                return true;
            }
            Topic rhs = (Topic) obj;
         // @formatter:off
	     return new EqualsBuilder()
	                  .append(consumerGroup, rhs.consumerGroup)
	                  .append(consumerId, rhs.consumerId)
	                  .append(host, rhs.host)
	                  .append(username, rhs.username)
	                  .append(name, rhs.name)
	                  .append(partition, rhs.partition)
	                  .append(password, rhs.password)
	                  .append(transportType, rhs.transportType)
	                  .isEquals();
	     // @formatter:on
        }

        @Override
        public String toString() {
            return "Topic [name=" + name + ", host=" + host + ", username=" + username + ", password=" + password + ", partition="
                    + partition + ", consumerGroup=" + consumerGroup + ", consumerId=" + consumerId
                    + ", transportType =" + transportType + "]";
        }
    }
}
