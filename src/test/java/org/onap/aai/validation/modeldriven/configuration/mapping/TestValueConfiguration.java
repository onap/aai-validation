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
package org.onap.aai.validation.modeldriven.configuration.mapping;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.onap.aai.validation.modeldriven.configuration.mapping.Filter;
import org.onap.aai.validation.modeldriven.configuration.mapping.ValueConfiguration;

public class TestValueConfiguration {

    static {
        System.setProperty("AJSC_HOME", ".");
    }

    @Test
    public void testAllMethodsToImproveCodeCoverage() {

        List<String> valid = new ArrayList<>();
        valid.add("string1");
        Filter filter = new Filter();
        filter.setPath("testPath");
        filter.setValid(valid);

        ValueConfiguration valueConf1 = new ValueConfiguration();
        valueConf1.setFilter(filter);
        valueConf1.setId("id");
        valueConf1.setOrigin("testOrigin");
        valueConf1.setRoot("testRoot");
        valueConf1.setValue("testValue");

        ValueConfiguration valueConf2 = new ValueConfiguration();
        valueConf2.setFilter(filter);
        valueConf2.setId("id");
        valueConf2.setOrigin("testOrigin");
        valueConf2.setRoot("testRoot");
        valueConf2.setValue("testValue");

        assertThat(valueConf1, is(valueConf2));
        assertThat(valueConf1.hashCode(), is(valueConf2.hashCode()));
        assertThat(valueConf1.getFilter(), is(valueConf2.getFilter()));
        assertThat(valueConf1.getId(), is(valueConf2.getId()));
        assertThat(valueConf1.getOrigin(), is(valueConf2.getOrigin()));
        assertThat(valueConf1.getRoot(), is(valueConf2.getRoot()));
        assertThat(valueConf1.getValue(), is(valueConf2.getValue()));
        assertThat(valueConf1.toString(), is(valueConf2.toString()));
        assertTrue(valueConf1.equals(valueConf2));
    }

}
