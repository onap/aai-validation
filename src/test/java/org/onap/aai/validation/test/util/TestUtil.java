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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Test helper methods.
 */
public class TestUtil {

    public static final String VALIDATION_SERVICE_URL = "/services/validation-service/v1/app/validate";

    /**
     * Gets files, such as test data from the classpath.
     *
     * @param filename
     *            the name of the file
     * @return a String with the file contents.
     * @throws URISyntaxException
     * @throws IOException
     */
    public static String getFileAsString(String filename) throws URISyntaxException, IOException {
        // Try loading the named file directly
        URI uri = new File(filename).toURI();
        URL systemResource = ClassLoader.getSystemResource(filename);
        if (systemResource != null) {
            uri = systemResource.toURI();
        }
        byte[] encoded = Files.readAllBytes(Paths.get(uri));
        return new String(encoded, StandardCharsets.UTF_8);
    }
}
