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
package org.onap.aai.validation.test.util;

import com.google.gson.JsonSyntaxException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import org.onap.aai.validation.result.ValidationResult;
import org.onap.aai.validation.result.ValidationResultBuilder;

public class TestEntity {

    public File inputFile;
    public String expectedResultsFile;

    /**
     * Create a new test entity.
     * 
     * @param root
     *            the top-level folder for the test suite
     * @param inputFilePath
     *            the path to the input file to be tested
     * @param inputEventsPath
     *            the folder containing the input file(s)
     * @param outputEventsPath
     *            the folder to write the outputs to
     */
    public TestEntity(Path root, Path inputFilePath, String inputEventsPath, String outputEventsPath) {
        String rootUri = root.toUri().toString();
        String resultsRoot = rootUri.replaceAll(inputEventsPath + "/$", outputEventsPath + "/");
        String inputFileUri = inputFilePath.toUri().toString();
        this.inputFile = inputFilePath.toFile();
        this.expectedResultsFile = inputFileUri.replace(rootUri, resultsRoot).replaceAll("\\.json$", ".exp.json");
    }

    public String getJson() throws URISyntaxException, IOException {
        return TestUtil.getFileAsString(inputFile.getPath());
    }

    /**
     * Fetch the expected JSON output from the test resources.
     *
     * @return the contents of the file that stores the expected JSON, or an empty string if there is no expected JSON
     * @throws URISyntaxException
     * @throws IOException
     */
    public String getExpectedJson() throws URISyntaxException, IOException {
        try {
            return TestUtil.getFileAsString(new URI(expectedResultsFile).getPath());
        } catch (NoSuchFileException e) {
            return "";
        }
    }

    public ValidationResult getExpectedValidationResult() throws JsonSyntaxException, URISyntaxException, IOException {
        return ValidationResultBuilder.fromJson(getExpectedJson());
    }

    @Override
    public String toString() {
        return "TestEntity [inputFile=" + inputFile + ", expectedResultsFile=" + expectedResultsFile + "]";
    }

    public boolean expectsError() throws URISyntaxException, IOException {
        try {
            getErrorFileContents();
        } catch (NoSuchFileException e) {
            return false;
        }
        return true;
    }

    public String getExpectedErrorMessage() throws URISyntaxException, IOException {
        return getErrorFileContents().trim();
    }

    private String getErrorFileContents() throws URISyntaxException, IOException {
        return TestUtil.getFileAsString(new URI(getErrorFileUri()).getPath());
    }

    private String getErrorFileUri() {
        return expectedResultsFile.replaceAll("\\.exp\\.json$", ".error");
    }
}
