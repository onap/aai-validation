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
package org.onap.aai.validation.services;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.aai.validation.controller.ValidationController;
import org.onap.aai.validation.exception.ValidationServiceException;
import org.onap.aai.validation.publisher.MockEventPublisher;
import org.onap.aai.validation.test.util.TestEntity;
import org.onap.aai.validation.test.util.TestUtil;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(locations = {"classpath:oxm-reader/schemaIngest.properties", "classpath:application.properties"})
@ContextConfiguration(locations = {"classpath:/info-service/test-validation-service-beans.xml"})
public class TestInfoService {

    static {
        System.setProperty("APP_HOME", ".");
    }

    enum TestData {
        VSERVER("rule-driven-validator/test_events/vserver-create-event.json");

        private String filename;

        TestData(String filename) {
            this.filename = filename;
        }

        public String getFilename() {
            return this.filename;
        }
    }

    private InfoService infoService;

    @Inject
    private ValidationController validationController;

    @Inject
    MockEventPublisher messagePublisher;

    @Before
    public void setUp() throws ValidationServiceException {
        infoService = new InfoService();
        infoService.setValidationController(validationController);
    }

    /**
     * @throws ValidationServiceException
     */
    @Test
    public void testInitialisedInfoService() throws ValidationServiceException {
        assertThat(infoService.getValidationController(), is(validationController));
        String info = infoService.getInfo();
        assertResultsStringFormatted(info);
    }

    /**
     * @throws ValidationServiceException
     */
    @Test
    public void testThrowableRecorded() throws ValidationServiceException {
        validationController.recordThrowable(new Exception());
        String info = infoService.getInfo();
        assertResultsStringFormatted(info);
        assertThat(info, containsString("Exception reported"));
    }

    /**
     * @throws ValidationServiceException
     */
    @Test
    public void testInvalidEventRecorded() throws ValidationServiceException {
        validationController.execute("", "http");
        String info = infoService.getInfo();
        assertResultsStringFormatted(info);
        assertThat(info, containsString("errored=1"));
    }

    @Test
    public void testVserverEventRecorded() throws URISyntaxException, IOException {
        Path vserverTestFile = Paths.get(ClassLoader.getSystemResource(TestData.VSERVER.getFilename()).toURI());
        Path root = vserverTestFile.getParent();
        assertThat(root, is(not(nullValue())));
        TestEntity entity = new TestEntity(root, vserverTestFile, "test_events", "results/expected");
        messagePublisher.setTestEntity(entity);
        messagePublisher.setTestDescription(entity.inputFile.getAbsolutePath());
        validationController.execute(TestUtil.getFileAsString(TestData.VSERVER.getFilename()), "http");
        String info = infoService.getInfo();
        assertResultsStringFormatted(info);
        assertThat(info, containsString("total=1"));
    }

    /**
     * Assert that the info service status string contains the expected standard results and formatting.
     *
     * @param info
     */
    private void assertResultsStringFormatted(String info) {
        assertThat(info, startsWith("Status: Up\n"));
        assertThat(info, containsString("Started at"));
        assertThat(info, containsString("total=0"));
    }

}
