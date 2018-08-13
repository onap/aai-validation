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
package org.onap.aai.validation.factory;

import org.onap.aai.event.client.DMaaPEventPublisher;

public class DMaaPEventPublisherFactory {  


    public DMaaPEventPublisher createEventPublisher(String topicHost, String topicName, String topicUsername,
            String topicPassword, String topicTransportType) {
        int defaultBatchSize = DMaaPEventPublisher.DEFAULT_BATCH_SIZE;
        long defaultBatchAge = DMaaPEventPublisher.DEFAULT_BATCH_AGE;
        int defaultBatchDelay = DMaaPEventPublisher.DEFAULT_BATCH_DELAY;
        return new DMaaPEventPublisher(topicHost, topicName, topicUsername, topicPassword, defaultBatchSize,
                defaultBatchAge, defaultBatchDelay, topicTransportType);
    }

}
