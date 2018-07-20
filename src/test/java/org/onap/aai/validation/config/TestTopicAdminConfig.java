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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import javax.inject.Inject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.aai.validation.config.TopicAdminConfig;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/topic-admin-config/test-validation-service-beans.xml"})
public class TestTopicAdminConfig {

    static {
        System.setProperty("AJSC_HOME", ".");
    }

    @Inject
    private TopicAdminConfig topicAdminConfig;

    @Test
    public void testTopicAdminConfigPopulation() throws Exception {
        TopicAdminConfig expectedTopicAdminConfig = new TopicAdminConfig();

        expectedTopicAdminConfig.setPublishEnable(true);
        expectedTopicAdminConfig.setPublishRetries(3l);
        expectedTopicAdminConfig.setConsumeEnable(true);
        expectedTopicAdminConfig.setConsumePollingIntervalSeconds(3l);

        assertThat(expectedTopicAdminConfig, is(topicAdminConfig));
        assertThat(expectedTopicAdminConfig.hashCode(), is(topicAdminConfig.hashCode()));
        assertThat(expectedTopicAdminConfig.toString(), is(topicAdminConfig.toString()));
        assertThat(expectedTopicAdminConfig.getConsumePollingIntervalSeconds(),
                is(topicAdminConfig.getConsumePollingIntervalSeconds()));
        assertThat(expectedTopicAdminConfig.getPublishRetries(), is(topicAdminConfig.getPublishRetries()));
        assertTrue(expectedTopicAdminConfig.equals(topicAdminConfig));
    }

}
