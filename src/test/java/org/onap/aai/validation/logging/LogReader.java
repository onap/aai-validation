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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang.time.StopWatch;

public class LogReader {

    private BufferedReader cachedReader;
    private Path cachedLog;

    public LogReader(String logDirectory, String logFilePrefix) throws IOException {
        cachedReader = getReader(logDirectory, logFilePrefix);
    }

    private BufferedReader getReader(String logDirectory, String logFilePrefix) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(getLogFile(logDirectory, logFilePrefix)));
        while (reader.readLine() != null) {
            // Consume all lines
        }
        return reader;
    }

    /**
     * @param logDirectory
     * @return the most recently created log file.
     * @throws IOException
     */
    private File getLogFile(String logDirectory, String filenamePrefix) throws IOException {
        Optional<Path> latestFilePath = Files.list(Paths.get(logDirectory))
                .filter(f -> Files.isDirectory(f) == false //
                        && f.getFileName().toString().startsWith(filenamePrefix)
                        && !f.getFileName().toString().endsWith(".zip"))
                .max(Comparator.comparingLong(f -> f.toFile().lastModified()));
        if (latestFilePath.isPresent()) {
            cachedLog = latestFilePath.get();
        } else {
            throw new IOException("No validation log files were found!");
        }

        return cachedLog.toFile();
    }

    /**
     * Read newly appended lines from the log.
     *
     * @return new lines appended to the log file
     * @throws IOException
     */
    public String getNewLines() throws IOException {
        StopWatch stopwatch = new StopWatch();
        stopwatch.start();

        while (!cachedReader.ready()) {
            if (stopwatch.getTime() > TimeUnit.SECONDS.toMillis(10)) {
                throw new IOException("Test took too long - waiting on " + cachedLog);
            }
            // else keep waiting
        }

        StringBuilder lines = new StringBuilder();
        String line;
        while ((line = cachedReader.readLine()) != null) {
            lines.append(line).append(System.lineSeparator());
        }
        return lines.toString();
    }
}
