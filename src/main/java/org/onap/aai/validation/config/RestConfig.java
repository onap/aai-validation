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

import java.util.Objects;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.eclipse.jetty.util.security.Password;
import org.springframework.beans.factory.annotation.Value;

/**
 * Configuration required to establish REST client requests with an application.
 */
public class RestConfig {

    @Value("${host}")
    private String host;

    @Value("${port}")
    private Integer port;

    @Value("${httpProtocol}")
    private String protocol;

    @Value("${baseModelURI}")
    private String baseModelURI;

    @Value("${trustStorePath}")
    private String trustStorePath;

    @Value("${trustStorePassword.x}")
    private String trustStorePassword;

    @Value("${keyStorePath}")
    private String keyStorePath;

    @Value("${keyStorePassword.x}")
    private String keyStorePassword;

    @Value("${keyManagerFactoryAlgorithm}")
    private String keyManagerFactoryAlgorithm;

    @Value("${keyStoreType}")
    private String keyStoreType;

    @Value("${securityProtocol}")
    private String securityProtocol;

    @Value("${connectionTimeout}")
    private Integer connectionTimeout;

    @Value("${readTimeout}")
    private Integer readTimeout;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getBaseModelURI() {
        return baseModelURI;
    }

    public void setBaseModelURI(String baseModelURI) {
        this.baseModelURI = baseModelURI;
    }

    public String getTrustStorePath() {
        return trustStorePath;
    }

    public void setTrustStorePath(String trustStorePath) {
        this.trustStorePath = trustStorePath;
    }

    /**
     * Assumes the password is encrypted.
     *
     * @return the decrypted password
     */
    public String getTrustStorePassword() {
        return Password.deobfuscate(trustStorePassword);
    }

    public void setTrustStorePassword(String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
    }

    public String getKeyStorePath() {
        return keyStorePath;
    }

    public void setKeyStorePath(String keyStorePath) {
        this.keyStorePath = keyStorePath;
    }

    /**
     * Assumes the password is encrypted.
     *
     * @return the decrypted password
     */
    public String getKeyStorePassword() {
        return Password.deobfuscate(keyStorePassword);
    }

    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    public String getKeyManagerFactoryAlgorithm() {
        return keyManagerFactoryAlgorithm;
    }

    public void setKeyManagerFactoryAlgorithm(String keyManagerFactoryAlgorithm) {
        this.keyManagerFactoryAlgorithm = keyManagerFactoryAlgorithm;
    }

    public String getKeyStoreType() {
        return keyStoreType;
    }

    public void setKeyStoreType(String keyStoreType) {
        this.keyStoreType = keyStoreType;
    }

    public String getSecurityProtocol() {
        return securityProtocol;
    }

    public void setSecurityProtocol(String securityProtocol) {
        this.securityProtocol = securityProtocol;
    }

    public Integer getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(Integer connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public Integer getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Integer readTimeout) {
        this.readTimeout = readTimeout;
    }

    @Override
    public String toString() {
        return "RestConfig [host=" + host + ", port=" + port + ", protocol=" + protocol + ", baseModelURI="
                + baseModelURI + ", trustStorePath=" + trustStorePath + ", trustStorePassword=" + trustStorePassword
                + ", keyStorePath=" + keyStorePath + ", keyStorePassword=" + keyStorePassword
                + ", keyManagerFactoryAlgorithm=" + keyManagerFactoryAlgorithm + ", keyStoreType=" + keyStoreType
                + ", securityProtocol=" + securityProtocol + ", connectionTimeout=" + connectionTimeout
                + ", readTimeout=" + readTimeout + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.baseModelURI, this.connectionTimeout, this.host, this.keyManagerFactoryAlgorithm,
                this.keyStorePassword, this.keyStorePath, this.keyStoreType, this.port, this.protocol, this.readTimeout,
                this.securityProtocol, this.trustStorePassword, this.trustStorePath);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof RestConfig)) {
            return false;
        } else if (obj == this) {
            return true;
        }
        RestConfig rhs = (RestConfig) obj;
     // @formatter:off
     return new EqualsBuilder()
                  .append(baseModelURI, rhs.baseModelURI)
                  .append(connectionTimeout, rhs.connectionTimeout)
                  .append(host, rhs.host)
                  .append(keyManagerFactoryAlgorithm, rhs.keyManagerFactoryAlgorithm)
                  .append(keyStorePassword, rhs.keyStorePassword)
                  .append(keyStorePath, rhs.keyStorePath)
                  .append(keyStoreType, rhs.keyStoreType)
                  .append(port, rhs.port)
                  .append(protocol, rhs.protocol)
                  .append(readTimeout, rhs.readTimeout)
                  .append(securityProtocol, rhs.securityProtocol)
                  .append(trustStorePassword, rhs.trustStorePassword)
                  .append(trustStorePath, rhs.trustStorePath)
                  .isEquals();
     // @formatter:on
    }
}
