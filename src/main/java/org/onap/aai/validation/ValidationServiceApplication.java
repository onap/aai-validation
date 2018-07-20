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
package org.onap.aai.validation;

import java.util.HashMap;
import org.eclipse.jetty.util.security.Password;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ImportResource;

/**
 * Validation Service Spring Boot Application.
 */

@SpringBootApplication
@ImportResource("classpath:validation-service-beans.xml")
public class ValidationServiceApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        HashMap<String, Object> props = new HashMap<>();
        String keyStorePassword = System.getProperty("KEY_STORE_PASSWORD");
        if (keyStorePassword != null && !keyStorePassword.isEmpty()) {
            props.put("server.ssl.key-store-password", Password.deobfuscate(keyStorePassword));
        }
        new ValidationServiceApplication()
                .configure(new SpringApplicationBuilder(ValidationServiceApplication.class).properties(props))
                .run(args);
    }

}
