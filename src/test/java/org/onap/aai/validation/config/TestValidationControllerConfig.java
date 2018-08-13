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

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.aai.validation.config.ValidationControllerConfig;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/validation-controller-config/test-validation-service-beans.xml"})
public class TestValidationControllerConfig {

    static {
        System.setProperty("AJSC_HOME", ".");
    }

    @Inject
    private ValidationControllerConfig validationControllerConfig;

    @Test
    public void testValidationControllerConfig() {
        ValidationControllerConfig expected = new ValidationControllerConfig();
        List<String> excludedEventActionList = new ArrayList<String>();
        excludedEventActionList.add("DELETE");

        List<String> eventTypeRuleList = new ArrayList<String>(2);
        eventTypeRuleList.add("AAI-EVENT");
        eventTypeRuleList.add("AAI-DATA-EXPORT-API");

        List<String> eventTypeModelList = new ArrayList<String>();
        eventTypeModelList.add("AAI-DATA-EXPORT-NQ");

        expected.setEventDomain("devINT1");
        expected.setExcludedEventActions(excludedEventActionList);
        expected.setEventTypeRule(eventTypeRuleList);
        expected.setEventTypeModel(eventTypeModelList);
        expected.setEventTypeEnd("END-EVENT");

        assertThat(expected.getEventDomain(), is(validationControllerConfig.getEventDomain()));
        assertThat(expected.getExcludedEventActions(), is(validationControllerConfig.getExcludedEventActions()));
        assertThat(expected.getEventTypeRule(), is(validationControllerConfig.getEventTypeRule()));
        assertThat(expected.getEventTypeModel(), is(validationControllerConfig.getEventTypeModel()));
        assertThat(expected.getEventTypeEnd(), is(validationControllerConfig.getEventTypeEnd()));
    }

}
