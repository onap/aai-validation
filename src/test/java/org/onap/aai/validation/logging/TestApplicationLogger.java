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

package org.onap.aai.validation.logging;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.util.Arrays;
import org.apache.commons.lang.time.StopWatch;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.mockito.Mockito;
import org.onap.aai.cl.api.LogFields;
import org.onap.aai.cl.api.Logger;
import org.onap.aai.cl.mdc.MdcOverride;
import org.onap.aai.validation.logging.LogHelper.TriConsumer;
import org.springframework.http.HttpHeaders;

/**
 * Simple test to log each of the validation messages in turn.
 *
 * This version tests only the error logger at INFO level.
 *
 */
public class TestApplicationLogger {

    @Rule
    public TestName name = new TestName();

    static {
        System.setProperty("APP_HOME", ".");
    }

    /**
     * Ensure that all of the EELF log files exist (and any log file rollover takes place) so that we can successfully
     * read from the log files (during the Test method).
     */
    @Before
    public void createLogFiles() {
        final String startMessage = "begin testing " + name.getMethodName();
        LogHelper.INSTANCE.debug(startMessage);
        LogHelper.INSTANCE.info(ApplicationMsgs.MESSAGE_AUDIT, startMessage);
        LogHelper.INSTANCE.logMetrics(startMessage);
        LogHelper.INSTANCE.logAuditSuccess(startMessage);
    }

    /**
     * Check that each message can be logged and that (by implication of successful logging) there is a corresponding
     * resource (message format).
     *
     * @throws IOException
     *             if an I/O error occurs when opening the log directory, or no files were found
     */
    @Test
    public void logAllMessages() throws IOException {
        Logger logger = LogHelper.INSTANCE;
        String logDirectory = LogHelper.getLogDirectory();
        LogReader errorReader = new LogReader(logDirectory, "error");
        LogReader debugReader = new LogReader(logDirectory, "debug");
        String[] args = {"1", "2", "3", "4"};
        for (ApplicationMsgs msg : Arrays.asList(ApplicationMsgs.values())) {
            if (msg.name().endsWith("ERROR")) {
                logger.error(msg, args);
                validateLoggedMessage(msg, errorReader, "ERROR");

                logger.error(msg, new RuntimeException("fred"), args);
                validateLoggedMessage(msg, errorReader, "fred");
            } else {
                logger.info(msg, args);
                validateLoggedMessage(msg, errorReader, "INFO");

                logger.warn(msg, args);
                validateLoggedMessage(msg, errorReader, "WARN");
            }

            if (logger.isDebugEnabled()) {
                logger.debug(msg, args);
                validateLoggedMessage(msg, debugReader, "DEBUG");
            }

            // The trace level is not enabled
            logger.trace(msg, args);
        }
    }

    /**
     * Check that each message can be logged and that (by implication of successful logging) there is a corresponding
     * resource (message format).
     *
     * @throws IOException
     */
    @Test
    public void logDebugMessages() throws IOException {
        org.junit.Assume.assumeTrue(LogHelper.INSTANCE.isDebugEnabled());
        LogReader reader = new LogReader(LogHelper.getLogDirectory(), "debug");
        LogHelper.INSTANCE.debug("a message");
        String str = reader.getNewLines();
        assertThat(str, is(notNullValue()));
    }


    /**
     * Check logAudit with HTTP headers
     *
     * @throws IOException
     */
    @Test
    public void logAuditMessage() throws IOException {
        final LogHelper logger = LogHelper.INSTANCE;
        final LogReader reader = new LogReader(LogHelper.getLogDirectory(), "audit");

        HttpHeaders headers = Mockito.mock(HttpHeaders.class);
        Mockito.when(headers.getFirst("X-ECOMP-RequestID")).thenReturn("ecomp-request-id");
        Mockito.when(headers.getFirst("X-FromAppId")).thenReturn("app-id");

        // Call logAudit without first calling startAudit
        logger.logAuditSuccess("first call: bob");
        String str = reader.getNewLines();
        assertThat(str, is(notNullValue()));
        assertThat("audit message log level", str, containsString("INFO"));
        assertThat("audit message content", str, containsString("bob"));

        // This time call the start method
        logger.startAudit(headers, null);
        logger.logAuditSuccess("second call: foo");
        str = reader.getNewLines();
        assertThat(str, is(notNullValue()));
        assertThat("audit message log level", str, containsString("INFO"));
        assertThat("audit message content", str, containsString("foo"));
        assertThat("audit message content", str, containsString("ecomp-request-id"));
        assertThat("audit message content", str, containsString("app-id"));
    }

    /**
     * Check logAudit with no HTTP headers
     *
     * @throws IOException
     */
    @Test
    public void logAuditMessageWithoutHeaders() throws IOException {
        LogHelper logger = LogHelper.INSTANCE;
        LogReader reader = new LogReader(LogHelper.getLogDirectory(), "audit");
        logger.startAudit(null, null);
        logger.logAuditSuccess("foo");
        String str = reader.getNewLines();
        assertThat(str, is(notNullValue()));
        assertThat("audit message log level", str, containsString("INFO"));
        assertThat("audit message content", str, containsString("foo"));
    }

    /**
     * Check logMetrics
     *
     * @throws IOException
     */
    @Test
    public void logMetricsMessage() throws IOException {
        LogReader reader = new LogReader(LogHelper.getLogDirectory(), "metrics");
        LogHelper logger = LogHelper.INSTANCE;
        logger.logMetrics("metrics: fred");
        String str = reader.getNewLines();
        assertThat(str, is(notNullValue()));
        assertThat("metrics message log level", str, containsString("INFO"));
        assertThat("metrics message content", str, containsString("fred"));
    }

    @Test
    public void logMetricsMessageWithStopwatch() throws IOException {
        LogReader reader = new LogReader(LogHelper.getLogDirectory(), "metrics");
        LogHelper logger = LogHelper.INSTANCE;
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        logger.logMetrics(stopWatch, "joe", "bloggs");
        String logLine = reader.getNewLines();
        assertThat(logLine, is(notNullValue()));
        assertThat("metrics message log level", logLine, containsString("INFO"));
        assertThat("metrics message content", logLine, containsString("joe"));
    }

    @Test
    public void callUnsupportedMethods() throws IOException {
        LogHelper logger = LogHelper.INSTANCE;
        ApplicationMsgs dummyMsg = ApplicationMsgs.LOAD_PROPERTIES;
        callUnsupportedOperationMethod(logger::error, dummyMsg);
        callUnsupportedOperationMethod(logger::info, dummyMsg);
        callUnsupportedOperationMethod(logger::warn, dummyMsg);
        callUnsupportedOperationMethod(logger::debug, dummyMsg);
        callUnsupportedOperationMethod(logger::trace, dummyMsg);
        try {
            logger.error(dummyMsg, new LogFields(), new RuntimeException("test"), "");
        } catch (UnsupportedOperationException e) {
            // Expected to reach here
        }
        try {
            logger.info(dummyMsg, new LogFields(), new MdcOverride(), "");
        } catch (UnsupportedOperationException e) {
            // Expected to reach here
        }
        try {
            logger.formatMsg(dummyMsg, "");
        } catch (UnsupportedOperationException e) {
            // Expected to reach here
        }
    }

    /**
     * Call a logger method which is expected to throw an UnsupportedOperationException
     *
     * @param logMethod
     * @param dummyMsg
     */
    private void callUnsupportedOperationMethod(TriConsumer<Enum<?>, LogFields, String[]> logMethod,
            ApplicationMsgs dummyMsg) {
        try {
            logMethod.accept(dummyMsg, new LogFields(), new String[] {""});
            org.junit.Assert.fail("method should have thrown execption"); // NOSONAR as code not reached
        } catch (UnsupportedOperationException e) {
            // Expected to reach here
        }
    }

    /**
     * Assert that a log message was logged to the expected log file at the expected severity
     *
     * @param msg
     * @param reader
     * @param severity
     * @throws IOException
     */
    private void validateLoggedMessage(ApplicationMsgs msg, LogReader reader, String severity) throws IOException {
        String str = reader.getNewLines();
        assertThat(str, is(notNullValue()));
        assertThat(msg.toString() + " log level", str, containsString(severity));
    }
}
