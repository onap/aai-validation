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
package org.onap.aai.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import org.onap.aai.validation.logging.ApplicationMsgs;
import org.onap.aai.validation.logging.LogHelper;

/**
 * Authentication and authorization by user and role.
 */
public class AAIMicroServiceAuthCore {

    private static LogHelper applicationLogger = LogHelper.INSTANCE;

    public static final String APPCONFIG_DIR = (System.getProperty("CONFIG_HOME") == null)
            ? Paths.get(System.getProperty("APP_HOME"), "appconfig").toString() : System.getProperty("CONFIG_HOME");

    private static Path appConfigAuthDir = Paths.get(APPCONFIG_DIR, "auth");
    private static Path defaultAuthFileName = appConfigAuthDir.resolve("auth_policy.json");

    private static boolean usersInitialized = false;
    private static HashMap<String, AAIAuthUser> users;
    private static boolean timerSet = false;
    private static String policyAuthFileName;

    public enum HttpMethods {
        GET,
        PUT,
        DELETE,
        HEAD,
        POST
    }

    // Don't instantiate
    private AAIMicroServiceAuthCore() {}

    public static String getDefaultAuthFileName() {
        return defaultAuthFileName.toString();
    }

    public static void setDefaultAuthFileName(String defaultAuthFileName) {
        AAIMicroServiceAuthCore.defaultAuthFileName = Paths.get(defaultAuthFileName);
    }

    /**
     * @param authPolicyFile
     * @throws AAIAuthException
     *         if the policy file cannot be loaded
     */
    public static synchronized void init(String authPolicyFile) throws AAIAuthException {

        try {
            policyAuthFileName = AAIMicroServiceAuthCore.getConfigFile(authPolicyFile);
        } catch (IOException e) {
            applicationLogger.debug("Exception while retrieving policy file.");
            applicationLogger.error(ApplicationMsgs.PROCESS_REQUEST_ERROR, e);
            throw new AAIAuthException(e.getMessage());
        }
        if (policyAuthFileName == null) {
            throw new AAIAuthException("Auth policy file could not be found");
        }
        AAIMicroServiceAuthCore.reloadUsers();

        TimerTask task = new FileWatcher(new File(policyAuthFileName)) {
            @Override
            protected void onChange(File file) {
                // here we implement the onChange
                applicationLogger.debug("File " + file.getName() + " has been changed!");
                try {
                    AAIMicroServiceAuthCore.reloadUsers();
                } catch (AAIAuthException e) {
                    applicationLogger.error(ApplicationMsgs.PROCESS_REQUEST_ERROR, e);
                }
                applicationLogger.debug("File " + file.getName() + " has been reloaded!");
            }
        };

        if (!timerSet) {
            timerSet = true;
            Timer timer = new Timer();
            long period = TimeUnit.SECONDS.toMillis(1);
            timer.schedule(task, new Date(), period);
            applicationLogger.debug("Config Watcher Interval = " + period);
        }
    }

    public static String getConfigFile(String authPolicyFile) throws IOException {
        File authFile = new File(authPolicyFile);
        if (authFile.exists()) {
            return authFile.getCanonicalPath();
        }
        authFile = appConfigAuthDir.resolve(authPolicyFile).toFile();
        if (authFile.exists()) {
            return authFile.getCanonicalPath();
        }
        if (getDefaultAuthFileName() != null) {
            authFile = new File(getDefaultAuthFileName());
            if (authFile.exists()) {
                return getDefaultAuthFileName();
            }
        }
        return null;
    }

    /**
     * @throws AAIAuthException
     */
    public static synchronized void reloadUsers() throws AAIAuthException {
        users = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();

        try {
            applicationLogger.debug("Reading from " + policyAuthFileName);
            JsonNode rootNode = mapper.readTree(new File(policyAuthFileName));
            JsonNode rolesNode = rootNode.path("roles");

            for (JsonNode roleNode : rolesNode) {
                String roleName = roleNode.path("name").asText();
                AAIAuthRole r = new AAIAuthRole();
                JsonNode usersNode = roleNode.path("users");
                JsonNode functionsNode = roleNode.path("functions");
                for (JsonNode functionNode : functionsNode) {
                    addFunctionToRole(r, roleName, functionNode);
                }
                for (JsonNode userNode : usersNode) {
                    String name = userNode.path("username").asText().toLowerCase();
                    AAIAuthUser user;
                    if (users.containsKey(name)) {
                        user = users.get(name);
                    } else {
                        user = new AAIAuthUser();
                    }

                    applicationLogger.debug("Assigning " + roleName + " to user " + name);
                    user.addRole(roleName, r);
                    users.put(name, user);
                }
            }
        } catch (FileNotFoundException e) {
            throw new AAIAuthException("Auth policy file could not be found", e);
        } catch (JsonProcessingException e) {
            throw new AAIAuthException("Error processing Auth policy file ", e);
        } catch (IOException e) {
            throw new AAIAuthException("Error reading Auth policy file", e);
        }

        usersInitialized = true;
    }

    /**
     * @param role
     * @param roleName
     * @param functionNode
     */
    private static void addFunctionToRole(AAIAuthRole role, String roleName, JsonNode functionNode) {
        String functionName = functionNode.path("name").asText();
        JsonNode methodsNode = functionNode.path("methods");

        if (methodsNode.size() == 0) {
            for (HttpMethods method : HttpMethods.values()) {
                String fullFunctionName = method.toString() + ":" + functionName;
                applicationLogger.debug("Installing (all methods) " + fullFunctionName + " on role " + roleName);
                role.addAllowedFunction(fullFunctionName);
            }
        } else {
            for (JsonNode methodNode : methodsNode) {
                String methodName = methodNode.path("name").asText();
                String fullFunctionName = methodName + ":" + functionName;
                applicationLogger.debug("Installing function " + fullFunctionName + " on role " + roleName);
                role.addAllowedFunction(fullFunctionName);
            }
        }
    }

    public static class AAIAuthUser {
        private HashMap<String, AAIAuthRole> roles;

        public AAIAuthUser() {
            this.roles = new HashMap<>();
        }

        public void addRole(String roleName, AAIAuthRole r) {
            this.roles.put(roleName, r);
        }

        public boolean checkAllowed(String checkFunc) {
            for (Entry<String, AAIAuthRole> role_entry : roles.entrySet()) {
                AAIAuthRole role = role_entry.getValue();
                if (role.hasAllowedFunction(checkFunc)) {
                    return true;
                }
            }
            return false;
        }

    }

    public static class AAIAuthRole {
        private List<String> allowedFunctions;

        public AAIAuthRole() {
            this.allowedFunctions = new ArrayList<>();
        }

        public void addAllowedFunction(String func) {
            this.allowedFunctions.add(func);
        }

        public void delAllowedFunction(String delFunc) {
            if (this.allowedFunctions.contains(delFunc)) {
                this.allowedFunctions.remove(delFunc);
            }
        }

        public boolean hasAllowedFunction(String functionName) {
            return allowedFunctions.contains(functionName);
        }
    }

    public static boolean authorize(String username, String authFunction) throws AAIAuthException {
        if (!usersInitialized || users == null) {
            throw new AAIAuthException("Auth module not initialized");
        }

        if (users.containsKey(username)) {
            if (users.get(username).checkAllowed(authFunction)) {
                logAuthenticationResult(username, authFunction, "AUTH ACCEPTED");
                return true;
            } else {
                logAuthenticationResult(username, authFunction, "AUTH FAILED");
                return false;
            }
        } else {
            logAuthenticationResult(username, authFunction, "User not found");
            return false;
        }
    }

    private static void logAuthenticationResult(String username, String authFunction, String result) {
        applicationLogger.debug(result + ": " + username + " on function " + authFunction);
    }

}