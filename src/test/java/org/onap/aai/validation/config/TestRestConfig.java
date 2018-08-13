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
import static org.junit.Assert.assertTrue;

import javax.inject.Inject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.aai.validation.config.RestConfig;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/rest-config/test-validation-service-beans.xml"})
public class TestRestConfig {

    static {
        System.setProperty("AJSC_HOME", ".");
    }

    @Inject
    private RestConfig restConfig;

    @Test
    public void testRestConfigPopulation() throws Exception {
        RestConfig expectedRestConfig = new RestConfig();

        expectedRestConfig.setHost("localhost");
        expectedRestConfig.setPort(8080);
        expectedRestConfig.setProtocol("https");
        expectedRestConfig.setBaseModelURI("${baseModelURI}");
        expectedRestConfig.setTrustStorePath("/dir1/dir2/trustStorePath");
        expectedRestConfig.setTrustStorePassword("70c87528c88dcd9f9c2558d30e817868");
        expectedRestConfig.setKeyStorePath("/dir1/dir2/keyStorePath");
        expectedRestConfig.setKeyStorePassword("70c87528c88dcd9f9c2558d30e817868");
        expectedRestConfig.setKeyManagerFactoryAlgorithm("AES");
        expectedRestConfig.setKeyStoreType("jks");
        expectedRestConfig.setSecurityProtocol("TLS");
        expectedRestConfig.setConnectionTimeout(100);
        expectedRestConfig.setReadTimeout(200);

        assertThat(expectedRestConfig, is(restConfig));
        assertThat(expectedRestConfig.getBaseModelURI(), is(restConfig.getBaseModelURI()));
        assertThat(expectedRestConfig.getConnectionTimeout(), is(restConfig.getConnectionTimeout()));
        assertThat(expectedRestConfig.getHost(), is(restConfig.getHost()));
        assertThat(expectedRestConfig.getKeyManagerFactoryAlgorithm(), is(restConfig.getKeyManagerFactoryAlgorithm()));
        assertThat(expectedRestConfig.getKeyStorePassword(), is(restConfig.getKeyStorePassword()));
        assertThat(expectedRestConfig.getKeyStorePath(), is(restConfig.getKeyStorePath()));
        assertThat(expectedRestConfig.getKeyStoreType(), is(restConfig.getKeyStoreType()));
        assertThat(expectedRestConfig.getPort(), is(restConfig.getPort()));
        assertThat(expectedRestConfig.getProtocol(), is(restConfig.getProtocol()));
        assertThat(expectedRestConfig.getReadTimeout(), is(restConfig.getReadTimeout()));
        assertThat(expectedRestConfig.getSecurityProtocol(), is(restConfig.getSecurityProtocol()));
        assertThat(expectedRestConfig.getTrustStorePassword(), is(restConfig.getTrustStorePassword()));
        assertThat(expectedRestConfig.getTrustStorePath(), is(restConfig.getTrustStorePath()));
        assertThat(expectedRestConfig.hashCode(), is(restConfig.hashCode()));
        assertThat(expectedRestConfig.toString(), is(restConfig.toString()));
        assertTrue(expectedRestConfig.equals(restConfig));

    }
}
