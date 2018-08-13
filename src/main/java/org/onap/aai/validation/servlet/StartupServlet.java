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
package org.onap.aai.validation.servlet;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.onap.aai.cl.api.Logger;
import org.onap.aai.validation.config.TopicAdminConfig;
import org.onap.aai.validation.logging.ApplicationMsgs;
import org.onap.aai.validation.logging.LogHelper;
import org.onap.aai.validation.services.EventPollingService;
import org.springframework.stereotype.Service;

/**
 * Main application service
 *
 */
@Service
public class StartupServlet {

    private static final Logger applicationLogger = LogHelper.INSTANCE;

    private static final long DEFAULT_POLLING_INTERVAL = 10;

    private final EventPollingService eventPollingService;
    private final TopicAdminConfig topicAdminConfig;

    /**
     * @param eventPollingService
     * @param topicAdminConfig
     */
    @Inject
    public StartupServlet(EventPollingService eventPollingService, TopicAdminConfig topicAdminConfig) {
        this.eventPollingService = eventPollingService;
        this.topicAdminConfig = topicAdminConfig;
    }

    /**
     * Called from Spring
     */
    @PostConstruct
    public void init() {
        applicationLogger.info(ApplicationMsgs.STARTUP_SERVLET_INIT);

        // Schedule a polling service if consuming is enabled.
        boolean consumingEnabled = topicAdminConfig.isConsumeEnable();
        if (consumingEnabled) {
            Long consumerPollingIntervalSeconds = topicAdminConfig.getConsumePollingIntervalSeconds();

            long interval;
            if (consumerPollingIntervalSeconds == null) {
                applicationLogger.info(ApplicationMsgs.POLLING_INTERVAL_CONFIG_NOT_PRESENT,
                        Long.toString(DEFAULT_POLLING_INTERVAL));
                interval = DEFAULT_POLLING_INTERVAL;
            } else {
                interval = consumerPollingIntervalSeconds;
            }

            applicationLogger.info(ApplicationMsgs.POLLING_FOR_EVENTS, Long.toString(interval));
            Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(eventPollingService, 0, interval,
                    TimeUnit.SECONDS);
        } else {
            applicationLogger.info(ApplicationMsgs.POLLING_DISABLED);
        }

        applicationLogger.info(ApplicationMsgs.STARTUP_SERVLET_INIT_SUCCESS);
    }

    public EventPollingService getEventPollingService() {
        return eventPollingService;
    }

    public TopicAdminConfig getTopicAdminConfig() {
        return topicAdminConfig;
    }
}
