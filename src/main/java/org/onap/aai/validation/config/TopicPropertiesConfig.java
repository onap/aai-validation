/**
 * ============LICENSE_START===================================================
 * Copyright (c) 2018 Amdocs
 * ============================================================================
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
 * ============LICENSE_END=====================================================
 */
package org.onap.aai.validation.config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.onap.aai.validation.logging.ApplicationMsgs;
import org.onap.aai.validation.logging.LogHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

@Configuration
public class TopicPropertiesConfig {

    private static LogHelper applicationLogger = LogHelper.INSTANCE;

    @Value("${topics.properties.location}")
    private String topicsPropertiesLocation;

    private static final String[] propertyFilePatterns = { "file:./%s/*.properties", "classpath:/%s/*.properties" };

    @Bean(name = "topicProperties")
    public Properties topicProperties() throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        List<Resource> resourceList = new ArrayList<>();
        for (String patternTemplate : propertyFilePatterns) {
            String pattern = String.format(patternTemplate, topicsPropertiesLocation).replace("//", "/");
            applicationLogger.info(ApplicationMsgs.LOAD_PROPERTIES, "using pattern " + pattern);
            try {
                resourceList.addAll(Arrays.asList(resolver.getResources(pattern)));
            } catch (FileNotFoundException e) {
                applicationLogger.info(ApplicationMsgs.LOAD_PROPERTIES, e.getMessage());
            }
        }

        applicationLogger.info(ApplicationMsgs.LOAD_PROPERTIES, resourceList.toString());

        PropertiesFactoryBean config = new PropertiesFactoryBean();
        config.setLocations(resourceList.toArray(new Resource[resourceList.size()]));
        config.afterPropertiesSet();
        return config.getObject();
    }

}
