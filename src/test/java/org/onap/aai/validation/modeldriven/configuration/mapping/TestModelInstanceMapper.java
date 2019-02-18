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

package org.onap.aai.validation.modeldriven.configuration.mapping;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.onap.aai.validation.modeldriven.configuration.mapping.ModelInstanceMapper.MappingType;

public class TestModelInstanceMapper {

    static {
        System.setProperty("APP_HOME", ".");
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

        ModelInstanceMapper mapper1 = new ModelInstanceMapper();
        mapper1.setInstance(valueConf1);
        mapper1.setMappingType(MappingType.ATTRIBUTE.toString());
        mapper1.setModel(valueConf1);

        ModelInstanceMapper mapper2 = new ModelInstanceMapper();
        mapper2.setInstance(valueConf1);
        mapper2.setMappingType(MappingType.ATTRIBUTE.toString());
        mapper2.setModel(valueConf1);

        assertThat(mapper1.hashCode(), is(mapper2.hashCode()));
        assertThat(mapper1.getInstance(), is(mapper2.getInstance()));
        assertThat(mapper1.getMappingType(), is(mapper2.getMappingType()));
        assertThat(mapper1.getModel(), is(mapper2.getModel()));
        assertThat(mapper1.toString(), is(mapper2.toString()));
        assertTrue(mapper1.equals(mapper2));
    }

}
