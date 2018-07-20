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
package org.onap.aai.validation.itest;

import javax.inject.Inject;
import org.junit.Before;
import org.onap.aai.validation.config.TopicConfig;
import org.onap.aai.validation.config.TopicConfig.Topic;
import org.onap.aai.validation.itest.util.TopicUtils;
import org.springframework.test.context.ContextConfiguration;

//@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:itest-validation-service-beans.xml"})
public class ITestValidationService {

    enum TestData {
        // @formatter:off
		VSERVER ("rule-driven-validator/test_events/vserver-create-event.json"),
		VSERVER_INVALID_DOMAIN ("events/vserver-create-event-invalid-domain.json"),
		VSERVER_INVALID_EVENTTYPE ("events/vserver-create-event-invalid-eventtype.json");

		private String filename;
		TestData(String filename) {this.filename = filename;}
		public String getFilename() {return this.filename;}
		// @formatter:on
    }


    @Inject
    private TopicConfig topicConfig;

    @Before
    public void setUp() throws Exception { // NOSONAR
        // Consume any existing messages on the Event Bus before running each test.
        for (Topic topic : topicConfig.getConsumerTopics()) {
            TopicUtils.consumeEventBusMessage(topic);
        }

        for (Topic topic : topicConfig.getPublisherTopics()) {
            TopicUtils.consumeEventBusMessage(topic);
        }
    }
}
