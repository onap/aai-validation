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

import java.util.List;
import org.springframework.beans.factory.annotation.Value;

/**
 * Loads the properties needed by the controller using spring.
 */
public class ValidationControllerConfig extends PropertiesConfig {

	@Value("${event.domain}")
	private String eventDomain;

	@Value("#{'${event.action.exclude}'.split(',')}")
	private List<String> excludedEventActions;

	@Value("#{'${event.type.rule}'.split(',')}")
	private List<String> eventTypeRule;

	@Value("#{'${event.type.model}'.split(',')}")
	private List<String> eventTypeModel;

	@Value("${event.type.end:END-EVENT}")
    private String eventTypeEnd;

	public String getEventDomain() {
		return eventDomain;
	}

	public void setEventDomain(String eventDomain) {
		this.eventDomain = eventDomain;
	}

	public List<String> getExcludedEventActions() {
		return excludedEventActions;
	}

	public void setExcludedEventActions(List<String> excludedEventActions) {
		this.excludedEventActions = excludedEventActions;
	}

	public List<String> getEventTypeRule() {
		return eventTypeRule;
	}

	public void setEventTypeRule(List<String> eventTypeRule) {
		this.eventTypeRule = eventTypeRule;
	}

	public List<String> getEventTypeModel() {
		return eventTypeModel;
	}

	public void setEventTypeModel(List<String> eventTypeModel) {
		this.eventTypeModel = eventTypeModel;
	}

    public String getEventTypeEnd() {
        return eventTypeEnd;
    }

    public void setEventTypeEnd(String eventTypeEnd) {
        this.eventTypeEnd = eventTypeEnd;
    }
}
