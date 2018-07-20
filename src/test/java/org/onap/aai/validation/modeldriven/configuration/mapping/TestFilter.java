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

public class TestFilter {

    static {
        System.setProperty("AJSC_HOME", ".");
    }

    @Test
    public void testAllMethodsInFilterClassToImproveCodeCoverage() {
        Filter filter1 = new Filter();
        List<String> valid = new ArrayList<>();
        valid.add("String 1");
        filter1.setPath("path");
        filter1.setValid(valid);

        Filter filter2 = new Filter();
        filter2.setPath("path");
        filter2.setValid(valid);

        assertThat(filter1, is(filter2));
        assertThat(filter1.hashCode(), is(filter2.hashCode()));
        assertThat(filter1.getPath(), is(filter2.getPath()));
        assertThat(filter1.getValid(), is(filter2.getValid()));
        assertThat(filter1.toString(), is(filter2.toString()));
        assertTrue(filter1.equals(filter2));
    }

}
