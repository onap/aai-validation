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
package org.onap.aai.validation.ruledriven.configuration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import org.junit.rules.TemporaryFolder;
import org.onap.aai.cl.api.Logger;
import org.onap.aai.validation.logging.LogHelper;
import org.onap.aai.validation.ruledriven.configuration.build.ContentBuilder;
import org.onap.aai.validation.ruledriven.configuration.build.EntityBuilder;
import org.onap.aai.validation.ruledriven.configuration.build.RuleBuilder;

public class ConfigFileBuilder extends ContentBuilder {

    private static final Logger logger = LogHelper.INSTANCE;

    private static final String TMP_CONFIG_FILE = "config.txt";
    private TemporaryFolder testFolder;
    private File tempFile;

    public ConfigFileBuilder(TemporaryFolder testFolder) {
        super();
        this.testFolder = testFolder;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ConfigBuilder [").append(testFolder.getRoot().getAbsolutePath()).append(System.lineSeparator());
        for (ContentBuilder item : items) {
            sb.append(item).append(System.lineSeparator());
        }
        sb.append(System.lineSeparator()).append("]");
        return sb.toString();
    }

    public EntityBuilder entity() {
        EntityBuilder item = new EntityBuilder();
        addContent(item);
        return item;
    }

    public RuleBuilder rule() {
        RuleBuilder item = new RuleBuilder();
        addContent(item);
        return item;
    }

    public void entity(Properties props) {
        EntityBuilder entity = entity();
        entity.appendProperties(props);
    }

    public RuleBuilder rule(String name) {
        RuleBuilder rule = rule();
        rule.appendValue("name", name);
        return rule;
    }

    public List<EntitySection> loadConfiguration() throws IOException {
        logger.debug("Configuration file: " + build());
        return buildConfiguration(build());
    }

    private List<EntitySection> buildConfiguration(String text) throws IOException {
        tempFile = createConfigFile(text);
        return RulesConfigurationLoader.loadConfiguration(tempFile).getEntities();
    }

    private File createConfigFile(String configText) throws IOException {
        tempFile = testFolder.newFile(TMP_CONFIG_FILE);
        BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile));
        bw.write(configText);
        bw.close();
        return tempFile;
    }

    public void freeResources() {
        tempFile.delete();
    }

}
