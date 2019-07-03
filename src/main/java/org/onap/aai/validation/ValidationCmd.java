/*-
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright (C) 2019 Nokia Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.aai.validation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.onap.aai.validation.config.RuleIndexingConfig;
import org.onap.aai.validation.exception.ValidationServiceException;
import org.onap.aai.validation.reader.EventReader;
import org.onap.aai.validation.result.ValidationResult;
import org.onap.aai.validation.result.Violation;
import org.onap.aai.validation.ruledriven.RuleDrivenValidator;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@EnableAutoConfiguration
@ImportResource("classpath:validation-cmd-beans.xml")
public class ValidationCmd extends SpringBootServletInitializer {

  private static final String EVENT_FILE_PATH = "event.json";

  private List<Path> rulesFoldersPaths =new ArrayList<>();
  private String eventFilePath = EVENT_FILE_PATH;

  private void printUsage() {
    System.out.println("ValidationCmd is designed to test defined rules from command line. Usage:");
    System.out.println("ValidationCmd [-e eventFilePath] [-r rulesFolderPath][]");
    System.out
        .println("-e eventFilePath - path to file with json event. Default: " + EVENT_FILE_PATH);
    System.out.println(
        "-r rulesFolderPath - path to foler with rules definition - *.groovy fils. If more than one then each requires -r");
  }

  private boolean parseCmd(String[] args) {
    if (args == null || args.length <= 0) {
      return true;
    }

    for (int i = 0; i < args.length; i++) {
      String s = args[i];
      if (s == null || s.isEmpty()) {
        continue;
      }

      if (0 == "-e".compareTo(s)) {
        i++;
        if (i >= args.length) {
          System.out.println("After -e missing eventFilePath");
          printUsage();
          return false;
        }
        eventFilePath = args[i];
      } else if (0 == "-r".compareTo(s)) {
        i++;
        if (i >= args.length) {
          System.out.println("After -r missing rulesFoldersPath");
          printUsage();
          return false;
        }
        rulesFoldersPaths.add(Paths.get(args[i]));
      } else {
        System.out.println("Unknown parameter: " + s);
        printUsage();
        return false;
      }
    }

    return true;
  }

  private RuleDrivenValidator createRuleDrivenValidator(ApplicationContext ctx) {

    EventReader eventReader = ctx.getBean(EventReader.class);
    RuleIndexingConfig ruleIndexingConfig = ctx.getBean(RuleIndexingConfig.class);

    RuleDrivenValidator validator = new RuleDrivenValidator(rulesFoldersPaths, null, eventReader,
        ruleIndexingConfig);

    return validator;
  }

  private void validate(RuleDrivenValidator validator)
      throws IOException, ValidationServiceException {

    String event = new String(Files.readAllBytes(Paths.get(eventFilePath)));

    List<ValidationResult> resultList = validator.validate(event);

    printResults(resultList);
  }

  private void printResults(List<ValidationResult> resultList) {
    if (resultList == null) {
      return;
    }

    System.out.println("--RESULT---------------------");
    for (ValidationResult vr : resultList) {
      System.out.println("--VIOLATIONS---------------------");
      for(Violation violation:vr.getViolations()){
        System.out.println("- ");
        System.out.println(violation.toString());
      }
      System.out.println("--JSON---------------------");
      System.out.println(vr.getViolations());
      String s = vr.toJson();
      System.out.println(s);
    }
    System.out.println("--RESULT-END---------------------");
  }

  private void printConfig() {

    System.out.print("rulesFoldersPaths= " );
    for(Path path: rulesFoldersPaths)
      System.out.print(path+",");
    System.out.println();
    System.out.println("eventFilePath= " + eventFilePath);
  }

  /**
   * Create and run the Application.
   *
   * @param args the command line arguments
   */
  public static void main(String[] args) {

    System.out.println("user.dir=" + System.getProperty("user.dir"));
    ApplicationContext ctx = new AnnotationConfigApplicationContext(ValidationCmd.class);

    ValidationCmd validationCmd = new ValidationCmd();
    try {
      if (!validationCmd.parseCmd(args)) {
        return;
      }

      validationCmd.printConfig();

      RuleDrivenValidator validator = validationCmd.createRuleDrivenValidator(ctx);

      validationCmd.validate(validator);

    } catch (Exception e) {
      System.out.println(e.getMessage());
      e.printStackTrace();
    }
  }
}
