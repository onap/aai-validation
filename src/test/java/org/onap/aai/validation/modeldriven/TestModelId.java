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
package org.onap.aai.validation.modeldriven;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.onap.aai.validation.modeldriven.ModelId;

public class TestModelId {

    static {
        System.setProperty("AJSC_HOME", ".");
    }

    @Test
    public void testGettersAndSetters() {
        String attr = null;
        String id = null;
        ModelId modelId = new ModelId(attr, id);
        assertThat(modelId.getModelIdAttribute(), is(equalTo(attr)));
        assertThat(modelId.getModelId(), is(equalTo(id)));
        assertThat(modelId.isEmpty(), is(true));

        attr = "";
        modelId.setModelIdAttribute(attr);
        assertThat(modelId.getModelIdAttribute(), is(equalTo(attr)));
        assertThat(modelId.getModelId(), is(equalTo(id)));
        assertThat(modelId.isEmpty(), is(true));

        attr = "new_attr_value";
        modelId.setModelIdAttribute(attr);
        assertThat(modelId.getModelIdAttribute(), is(equalTo(attr)));
        assertThat(modelId.getModelId(), is(equalTo(id)));
        assertThat(modelId.isEmpty(), is(true));

        id = "new_id";
        modelId.setModelId(id);
        assertThat(modelId.getModelIdAttribute(), is(equalTo(attr)));
        assertThat(modelId.getModelId(), is(equalTo(id)));
        assertThat(modelId.isEmpty(), is(false));

        id = "";
        modelId.setModelId(id);
        assertThat(modelId.getModelIdAttribute(), is(equalTo(attr)));
        assertThat(modelId.getModelId(), is(equalTo(id)));
        assertThat(modelId.isEmpty(), is(true));

        attr = null;
        modelId.setModelIdAttribute(attr);
        assertThat(modelId.getModelIdAttribute(), is(equalTo(attr)));
        assertThat(modelId.getModelId(), is(equalTo(id)));
        assertThat(modelId.isEmpty(), is(true));
    }

    @Test
    public void testIsEmpty() {
        assertThat(new ModelId(null, null).isEmpty(), is(true));
        assertThat(new ModelId("", null).isEmpty(), is(true));
        assertThat(new ModelId(null, "").isEmpty(), is(true));
        assertThat(new ModelId("", "").isEmpty(), is(true));
        assertThat(new ModelId("A", null).isEmpty(), is(true));
        assertThat(new ModelId(null, "B").isEmpty(), is(true));
        assertThat(new ModelId("A", "B").isEmpty(), is(false));
    }

    @Test
    public void testEqualsMethod() {
        ModelId id1 = new ModelId("a", "b");
        ModelId id2 = new ModelId("a", "b");
        assertThat(id1, is(equalTo(id1)));
        assertThat(id1, is(equalTo(id2)));
        assertThat(id1, is(not(equalTo(null))));
        assertThat(id1.hashCode(), is(equalTo(id2.hashCode())));
    }

}
