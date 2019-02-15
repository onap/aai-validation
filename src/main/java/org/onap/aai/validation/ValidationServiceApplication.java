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

package org.onap.aai.validation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.onap.aai.validation.config.TopicPropertiesConfig;
import org.onap.aai.validation.util.StringUtils;
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

    private enum SystemProperty {
        KEY_STORE_PASSWORD, // Mandatory password for the Application's keystore (containing the server cert)
        JAVA_TRUST_STORE("javax.net.ssl.trustStore"), // JVM
        JAVA_TRUST_STORE_PASSWORD("javax.net.ssl.trustStorePassword") // JVM
        ;

        private final String propertyName;

        SystemProperty() {
            propertyName = this.toString();
        }

        SystemProperty(String property) {
            propertyName = property;
        }

        public String readValue() {
            String propertyValue = System.getProperty(propertyName);
            if (propertyValue == null) {
                throw new IllegalArgumentException("System Property " + this + " not set");
            }
            return StringUtils.decrypt(propertyValue);
        }

        public void set(String propertyValue) {
            System.setProperty(propertyName, propertyValue);
        }
    }

    private enum ApplicationProperty {
        SERVER_SSL_KEY_STORE("server.ssl.key-store"), // Spring
        SERVER_SSL_KEY_STORE_PASSWORD("server.ssl.key-store-password"), // Spring
        SCHEMA_SERVICE_KEY_STORE_PASSWORD("schema.service.ssl.key-store-password"), // aai-schema-ingest
        SCHEMA_SERVICE_TRUST_STORE_PASSWORD("schema.service.ssl.trust-store-password") // aai-schema-ingest
        ;

        private final String propertyName;

        ApplicationProperty(String property) {
            propertyName = property;
        }

        public String from(Environment env) {
            return env.getProperty(this.propertyName);
        }

        public String mandatoryFrom(Environment env) {
            String value = from(env);
            if (value == null) {
                throw new IllegalArgumentException("Env property " + this.propertyName + " not set");
            }
            return value;
        }
    }

    /**
     * Create and run the Application.
     * 
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        new ValidationServiceApplication() //
                .configure(new SpringApplicationBuilder(ValidationServiceApplication.class)
                        .properties(buildEnvironmentProperties()))
                .run(args);
    }

    /**
     * Set the trust store system properties using default values from the application properties.
     */
    @PostConstruct
    public void setSystemProperties() {
        String trustStorePath = ApplicationProperty.SERVER_SSL_KEY_STORE.from(env);
        if (trustStorePath != null) {
            String trustStorePassword = ApplicationProperty.SERVER_SSL_KEY_STORE_PASSWORD.mandatoryFrom(env);
            SystemProperty.JAVA_TRUST_STORE.set(trustStorePath);
            SystemProperty.JAVA_TRUST_STORE_PASSWORD.set(trustStorePassword);
        }
    }

    /**
     * Create the default properties for the Spring Application's environment.
     * 
     * @param keyStorePassword
     *            SSL key store password
     * @return the default environment properties
     */
    private static Map<String, Object> buildEnvironmentProperties() {
        String keyStorePassword = SystemProperty.KEY_STORE_PASSWORD.readValue();
        Map<String, Object> props = new HashMap<>();
        for (ApplicationProperty property : Arrays.asList( //
                ApplicationProperty.SERVER_SSL_KEY_STORE_PASSWORD,
                ApplicationProperty.SCHEMA_SERVICE_KEY_STORE_PASSWORD,
                ApplicationProperty.SCHEMA_SERVICE_TRUST_STORE_PASSWORD)) {
            props.put(property.propertyName, keyStorePassword);
        }
        return props;
    }

}
