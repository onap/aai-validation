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

package org.onap.aai.validation.factory;

import java.net.MalformedURLException;
import org.onap.aai.event.client.DMaaPEventConsumer;

public class DMaaPEventConsumerFactory {

    public DMaaPEventConsumer createEventConsumer(String topicHost, String topicName, String topicUsername,
            String topicPassword, String consumerGroup, String consumerId, String transportType, String protocol)
            throws MalformedURLException {
        return new DMaaPEventConsumer(topicHost, topicName, topicUsername, topicPassword, consumerGroup, consumerId,
                DMaaPEventConsumer.DEFAULT_MESSAGE_WAIT_TIMEOUT, //
                DMaaPEventConsumer.DEFAULT_MESSAGE_LIMIT,
                transportType == null ? DMaaPEventConsumer.DEFAULT_TRANSPORT_TYPE : transportType,
                protocol == null ? DMaaPEventConsumer.DEFAULT_PROTOCOL : protocol, //
                null /* no filter */);
    }

}
