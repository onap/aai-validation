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
package org.onap.aai.auth;

import java.security.cert.X509Certificate;
import javax.inject.Inject;
import javax.security.auth.x500.X500Principal;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Cookie;
import org.onap.aai.validation.config.ValidationServiceAuthConfig;
import org.onap.aai.validation.logging.LogHelper;

public class AAIMicroServiceAuth {

    private static LogHelper applicationLogger = LogHelper.INSTANCE;

    private ValidationServiceAuthConfig validationServiceAuthConfig;
    private AAIMicroServiceAuthCore authCore;

    /**
     * @param validationServiceAuthConfig
     * @throws AAIAuthException
     *             if the policy file cannot be loaded
     */
    @Inject
    public AAIMicroServiceAuth(final ValidationServiceAuthConfig validationServiceAuthConfig) throws AAIAuthException {
        this.validationServiceAuthConfig = validationServiceAuthConfig;
        this.authCore = new AAIMicroServiceAuthCore();
        if (!validationServiceAuthConfig.isAuthenticationDisable()) {
            authCore.init(validationServiceAuthConfig.getAuthPolicyFile());
        }
    }

    /**
     * Check whether the given user may access the give function.
     * 
     * @param username
     *            user to be authorized
     * @param authFunction
     *            function the user wishes to access
     * @return true if the user is authorized to access the function, false otherwsie
     * @throws AAIAuthException
     *             if the auth object has not been initialized
     */
    public boolean authBasic(String username, String authFunction) throws AAIAuthException {
        return authCore.authorize(username, authFunction);
    }


    /**
     * Check whether the given user may access the give function.
     * 
     * @param username
     *            user to be authorized
     * @param authFunction
     *            function the user wishes to access
     * @return true if the user is authorized to access the function, false otherwsie
     * @throws AAIAuthException
     *             if the auth object has not been initialized
     */
    public String authUser(String authUser, String authFunction) throws AAIAuthException {
        StringBuilder username = new StringBuilder();

        username.append(authUser);
        if (!authBasic(username.toString(), authFunction)) {
            return "AAI_9101";

        }
        return "OK";
    }

    public boolean authCookie(Cookie cookie, String authFunction, StringBuilder username) throws AAIAuthException {
        if (cookie == null) {
            return false;
        }
        applicationLogger.debug("Got one:" + cookie);

        return authCore.authorize(username.toString(), authFunction);
    }

    public boolean validateRequest(HttpServletRequest req, String action, String apiPath) throws AAIAuthException {

        applicationLogger.debug("validateRequest: " + apiPath);
        applicationLogger.debug("validationServiceConfig.isAuthenticationDisable(): "
                + validationServiceAuthConfig.isAuthenticationDisable());

        if (validationServiceAuthConfig.isAuthenticationDisable()) {
            return true;
        }
        String[] ps = apiPath.split("/");
        String authPolicyFunctionName = ps[0];
        if (ps.length > 1) {
            if (ps[0].matches("v\\d+")) {
                authPolicyFunctionName = ps[1];
            } else {
                authPolicyFunctionName = ps[0];
            }
        }

        String cipherSuite = (String) req.getAttribute("javax.servlet.request.cipher_suite");
        String authUser = null;
        if (cipherSuite != null) {
            X509Certificate[] certChain = (X509Certificate[]) req.getAttribute("javax.servlet.request.X509Certificate");
            if (certChain != null) {
                X509Certificate clientCert = certChain[0];
                X500Principal subjectDN = clientCert.getSubjectX500Principal();
                authUser = subjectDN.toString();
            }
        }

        if (authUser == null) {
            return false;
        }

        String status = authUser(authUser.toLowerCase(), action + ":" + authPolicyFunctionName);
        return "OK".equals(status);
    }
}
