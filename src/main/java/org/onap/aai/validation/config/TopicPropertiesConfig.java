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

import org.onap.aai.validation.logging.LogHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Configuration
public class TopicPropertiesConfig {

    private static LogHelper applicationLogger = LogHelper.INSTANCE;

    @Value("${topics.properties.location}")
    private String topicsPropertiesLocation;

    private static final String[] TOPICS_PROPERTIES_LOCATION_TPL = { "file:./%s/*.properties", "classpath:/%s/*.properties" };

    @Bean(name="topicProperties")
    public Properties topicProperties() throws IOException {
        PropertiesFactoryBean config = new PropertiesFactoryBean();
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        List<Resource> resouceList = new ArrayList<Resource>();
        try {
            for (String p : bldrsPropLoc2Path(topicsPropertiesLocation)) {
                Resource[] resources = resolver.getResources(p);
                if (resources != null && resources.length > 0) {
                    for (Resource resource : resources) {
                        resouceList.add(resource);
                    }
                    break;
                }
            }
        } catch (Exception e) {
            applicationLogger.logAuditError(e);
        }
        config.setLocations(resouceList.toArray(new Resource[]{}));
        config.afterPropertiesSet();
        return config.getObject();
    }

    private static String[] bldrsPropLoc2Path(String topicsPropertiesLocation) {
        String[] res = new String[TOPICS_PROPERTIES_LOCATION_TPL.length];
        int indx = 0;
        for (String tmpl : TOPICS_PROPERTIES_LOCATION_TPL) {
            res[indx++] = String.format(tmpl, topicsPropertiesLocation).replace("//", "/");
        }
        return res;
    }

}
