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
package org.onap.aai.validation.auth;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.security.cert.X509Certificate;
import javax.security.auth.x500.X500Principal;
import javax.ws.rs.core.Cookie;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.aai.auth.AAIAuthException;
import org.onap.aai.auth.AAIMicroServiceAuth;
import org.onap.aai.auth.AAIMicroServiceAuthCore;
import org.onap.aai.validation.config.ValidationServiceAuthConfig;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * Tests @{link AAIMicroServiceAuth}
 */

public class MicroServiceAuthTest {

    static {
        System.setProperty("APP_HOME", ".");
        System.setProperty("CONFIG_HOME", Paths.get(System.getProperty("user.dir"), "src/test/resources").toString());
    }

    private static final String VALID_ADMIN_USER = "cn=common-name, ou=org-unit, o=org, l=location, st=state, c=us";
    private static final String authPolicyFile = "auth_policy.json";

    /**
     * Temporarily invalidate the default policy file and then try to initialise the authorisation class using the name
     * of a policy file that does not exist.
     *
     * @throws AAIAuthException
     * @throws IOException
     */
    @Test(expected = AAIAuthException.class)
    public void missingPolicyFile() throws AAIAuthException, IOException {
        String defaultFile = AAIMicroServiceAuthCore.getDefaultAuthFileName();
        try {
            AAIMicroServiceAuthCore.setDefaultAuthFileName("invalid.default.file");
            ValidationServiceAuthConfig authConfig = new ValidationServiceAuthConfig();
            authConfig.setAuthPolicyFile("invalid.file.name");
            new AAIMicroServiceAuth(authConfig);
        } finally {
            AAIMicroServiceAuthCore.setDefaultAuthFileName(defaultFile);
        }
    }

    /**
     * Test loading of a temporary file created with the specified roles
     *
     * @throws AAIAuthException
     * @throws IOException
     * @throws JSONException
     */
    @Test
    public void createLocalAuthFile() throws AAIAuthException, IOException, JSONException {
        JSONObject roles = createRoleObject("role", createUserObject("user"), createFunctionObject("func"));
        AAIMicroServiceAuth auth = createAuthService(roles);
        assertThat(auth.authUser("nosuchuser", "method:func"), is(equalTo("AAI_9101")));
        assertThat(auth.authUser("user", "method:func"), is(equalTo("OK")));
    }

    /**
     * Test loading of the policy file relative to CONFIG_HOME
     *
     * @throws AAIAuthException
     */
    @Test
    public void createAuth() throws AAIAuthException {
        AAIMicroServiceAuth auth = createStandardAuth();
        assertAdminUserAuthorisation(auth, VALID_ADMIN_USER);
    }

    @Test
    public void testAuthUser() throws AAIAuthException {
        AAIMicroServiceAuth auth = createStandardAuth();
        assertThat(auth.authUser(VALID_ADMIN_USER, "GET:actions"), is(equalTo("OK")));
        assertThat(auth.authUser(VALID_ADMIN_USER, "WRONG:action"), is(equalTo("AAI_9101")));
    }

    @Test
    public void testAuthCookie() throws AAIAuthException {
        AAIMicroServiceAuth auth = createStandardAuth();
        Cookie mockCookie = new Cookie("name", null);
        StringBuilder user = new StringBuilder(VALID_ADMIN_USER);

        assertThat(auth.authCookie(null, "GET:actions", user), is(false));
        assertThat(auth.authCookie(null, "WRONG:action", user), is(false));

        assertThat(auth.authCookie(mockCookie, "GET:actions", user), is(true));
        assertThat(auth.authCookie(mockCookie, "WRONG:action", user), is(false));
    }

    @Test
    public void testValidateRequests() throws AAIAuthException {
        AAIMicroServiceAuth auth = createStandardAuth();
        assertThat(auth.validateRequest(new MockHttpServletRequest(), null, "app/v1/service"), is(false));
        assertThat(auth.validateRequest(createMockRequest(), "POST", "getAndPublish"), is(false));
        assertThat(auth.validateRequest(createMockRequest(), "POST", "validate"), is(true));
    }

    private MockHttpServletRequest createMockRequest() {
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setSecure(true);
        servletRequest.setScheme("https");
        servletRequest.setServerPort(9501);
        servletRequest.setServerName("localhost");
        servletRequest.setRequestURI("/services/validation-service/v1/app/validate");

        X509Certificate mockCertificate = Mockito.mock(X509Certificate.class);
        Mockito.when(mockCertificate.getSubjectX500Principal())
                .thenReturn(new X500Principal("CN=test, OU=qa, O=Test Ltd, L=London, ST=London, C=GB"));

        servletRequest.setAttribute("javax.servlet.request.X509Certificate", new X509Certificate[] {mockCertificate});
        servletRequest.setAttribute("javax.servlet.request.cipher_suite", "");
        return servletRequest;
    }

    private AAIMicroServiceAuth createStandardAuth() throws AAIAuthException {
        ValidationServiceAuthConfig authConfig = new ValidationServiceAuthConfig();
        authConfig.setAuthPolicyFile(authPolicyFile);
        return new AAIMicroServiceAuth(authConfig);
    }

    /**
     * @param rolesJson
     * @return
     * @throws IOException
     * @throws AAIAuthException
     */
    private AAIMicroServiceAuth createAuthService(JSONObject roles) throws IOException, AAIAuthException {
        ValidationServiceAuthConfig authConfig = new ValidationServiceAuthConfig();
        File file = File.createTempFile("auth-policy", "json");
        file.deleteOnExit();
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(roles.toString());
        fileWriter.flush();
        fileWriter.close();

        authConfig.setAuthPolicyFile(file.getAbsolutePath());
        return new AAIMicroServiceAuth(authConfig);
    }

    /**
     * Assert authorisation results for an admin user based on the test policy file
     *
     * @param auth
     * @param adminUser
     * @throws AAIAuthException
     */
    private void assertAdminUserAuthorisation(AAIMicroServiceAuth auth, String adminUser) throws AAIAuthException {
        assertThat(auth.authUser(adminUser, "GET:actions"), is(equalTo("OK")));
        assertThat(auth.authUser(adminUser, "POST:actions"), is(equalTo("OK")));
        assertThat(auth.authUser(adminUser, "PUT:actions"), is(equalTo("OK")));
        assertThat(auth.authUser(adminUser, "DELETE:actions"), is(equalTo("OK")));
    }

    private JSONArray createFunctionObject(String functionName) throws JSONException {
        JSONArray functionsArray = new JSONArray();
        JSONObject func = new JSONObject();
        func.put("name", functionName);
        func.put("methods", createMethodObject("method"));
        functionsArray.put(func);
        return functionsArray;
    }

    private JSONArray createMethodObject(String methodName) throws JSONException {
        JSONArray methodsArray = new JSONArray();
        JSONObject method = new JSONObject();
        method.put("name", methodName);
        methodsArray.put(method);
        return methodsArray;
    }

    private JSONArray createUserObject(String username) throws JSONException {
        JSONArray usersArray = new JSONArray();
        JSONObject user = new JSONObject();
        user.put("username", username);
        usersArray.put(user);
        return usersArray;
    }

    private JSONObject createRoleObject(String roleName, JSONArray usersArray, JSONArray functionsArray)
            throws JSONException {
        JSONObject roles = new JSONObject();

        JSONObject role = new JSONObject();
        role.put("name", roleName);
        role.put("functions", functionsArray);
        role.put("users", usersArray);

        JSONArray rolesArray = new JSONArray();
        rolesArray.put(role);
        roles.put("roles", rolesArray);

        return roles;
    }

}
