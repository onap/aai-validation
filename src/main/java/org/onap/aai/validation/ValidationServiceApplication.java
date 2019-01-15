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
import java.util.Map;
import javax.annotation.PostConstruct;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.util.security.Password;
import org.onap.aai.validation.config.TopicPropertiesConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.env.Environment;

/**
 * Validation Service Spring Boot Application.
 */

@Configuration
@EnableAutoConfiguration
@Import(TopicPropertiesConfig.class)
@ImportResource("classpath:validation-service-beans.xml")
public class ValidationServiceApplication extends SpringBootServletInitializer {

    @Autowired
    private Environment env;

    public static void main(String[] args) {
        Map<String, Object> props = new HashMap<>();
        String keyStorePassword = System.getProperty("KEY_STORE_PASSWORD");
        if (!StringUtils.isEmpty(keyStorePassword)) {
            String deobfuscated = Password.deobfuscate(keyStorePassword);
            props.put("server.ssl.key-store-password", deobfuscated);
            props.put("schema.service.ssl.key-store-password", deobfuscated);
            props.put("schema.service.ssl.trust-store-password", deobfuscated);
        }
        new ValidationServiceApplication()
                .configure(new SpringApplicationBuilder(ValidationServiceApplication.class).properties(props))
                .run(args);
    }

    /**
     * Set the trust store system properties using default values from the application properties.
     */
    @PostConstruct
    public void setSystemProperties() {
        String trustStorePath = env.getProperty("server.ssl.key-store");
        if (trustStorePath != null) {
            String trustStorePassword = env.getProperty("server.ssl.key-store-password");
            if (trustStorePassword != null) {
                System.setProperty("javax.net.ssl.trustStore", trustStorePath);
                System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);
            }
        }
    }
}
