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

package org.onap.aai.validation.ruledriven.validator;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import javax.inject.Inject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.aai.validation.exception.ValidationServiceException;
import org.onap.aai.validation.result.ValidationResult;
import org.onap.aai.validation.ruledriven.RuleDrivenValidator;
import org.onap.aai.validation.test.util.TestUtil;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(locations = {"classpath:oxm-reader/schemaIngest.properties"})
@ContextConfiguration(locations = {"classpath:data-dictionary/test-data-dictionary-beans.xml"})
public class TestDataDictionary {

    static {
        System.setProperty("APP_HOME", ".");
    }

    @Inject
    private RuleDrivenValidator validator;

    @Test
    public void testValidateWithRuleIndexing() throws ValidationServiceException, URISyntaxException, IOException {
        List<ValidationResult> results =
                validator.validate(TestUtil.getFileAsString("data-dictionary/test_events/test-create-event.json"));
        assertThat(results, is(not(empty())));
        assertThat(results.get(0).getViolations(), is(empty()));
    }
}
