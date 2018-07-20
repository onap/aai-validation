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

import java.util.Objects;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.springframework.beans.factory.annotation.Value;

/**
 * Configuration bean with topic administration properties that are loaded via Spring configuration.
 */
public class TopicAdminConfig {

    @Value("${topic.publish.enable}")
    private boolean publishEnable;

    @Value("${topic.publish.retries}")
    private Long publishRetries;

    @Value("${topic.consume.enable}")
    private boolean consumeEnable;

    @Value("${topic.consume.polling.interval.seconds}")
    private Long consumePollingIntervalSeconds;

    public boolean isPublishEnable() {
        return publishEnable;
    }

    public void setPublishEnable(boolean publishEnable) {
        this.publishEnable = publishEnable;
    }

    public Long getPublishRetries() {
        return publishRetries;
    }

    public void setPublishRetries(Long publishRetries) {
        this.publishRetries = publishRetries;
    }

    public boolean isConsumeEnable() {
        return consumeEnable;
    }

    public void setConsumeEnable(boolean consumeEnable) {
        this.consumeEnable = consumeEnable;
    }

    public Long getConsumePollingIntervalSeconds() {
        return consumePollingIntervalSeconds;
    }

    public void setConsumePollingIntervalSeconds(Long consumePollingIntervalSeconds) {
        this.consumePollingIntervalSeconds = consumePollingIntervalSeconds;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.consumeEnable, this.consumePollingIntervalSeconds, this.publishEnable,
                this.publishRetries);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TopicAdminConfig)) {
            return false;
        } else if (obj == this) {
            return true;
        }
        TopicAdminConfig rhs = (TopicAdminConfig) obj;
     // @formatter:off
     return new EqualsBuilder()
                  .append(consumeEnable, rhs.consumeEnable)
                  .append(consumePollingIntervalSeconds, rhs.consumePollingIntervalSeconds)
                  .append(publishEnable, rhs.publishEnable)
                  .append(publishRetries, rhs.publishRetries)
                  .isEquals();
     // @formatter:on
    }

    @Override
    public String toString() {
        return "TopicAdminConfig [publishEnable=" + publishEnable + ", publishRetries=" + publishRetries
                + ", consumeEnable=" + consumeEnable + ", consumePollingIntervalSeconds="
                + consumePollingIntervalSeconds + "]";
    }
}
