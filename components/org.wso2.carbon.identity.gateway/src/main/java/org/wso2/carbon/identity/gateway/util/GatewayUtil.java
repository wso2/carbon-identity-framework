/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.gateway.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.deployment.engine.Artifact;
import org.wso2.carbon.identity.gateway.api.exception.GatewayServerException;
import org.wso2.carbon.identity.gateway.common.model.idp.IdentityProviderConfig;
import org.wso2.carbon.identity.gateway.common.model.idp.IdentityProviderEntity;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.BeanAccess;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GatewayUtil {

    private Logger logger = LoggerFactory.getLogger(GatewayUtil.class);

    /**
     * Read provide name by using file name.
     *
     * @param fileName
     * @return provider name
     * @throws GatewayServerException
     */
    public static String getProviderName(String fileName) throws GatewayServerException {
        if(!fileName.endsWith("." + Constants.YAML_EXTENSION)){
            throw new GatewayServerException("Provider config file should be yaml.");
        }
        return fileName.substring(0, fileName.indexOf(".yaml"));
    }

    /**
     * Load provider config.
     *
     * @param artifact
     * @param providerClass
     * @param <T>
     * @return
     * @throws GatewayServerException
     */
    public static <T extends Object> T getProvider(Artifact artifact, Class<T> providerClass)
            throws GatewayServerException {
        Path path = Paths.get(artifact.getPath());
        T provider = null ;
        if (Files.exists(path)) {
            try {
                Reader in = new InputStreamReader(Files.newInputStream(path), StandardCharsets.UTF_8);
                Yaml yaml = new Yaml();
                yaml.setBeanAccess(BeanAccess.FIELD);
                provider = yaml.loadAs(in, providerClass);
                if(provider == null){
                    throw new GatewayServerException("Provider is not loaded correctly.");
                }
            } catch (Exception e) {
                throw new GatewayServerException("Error while deploying service provider configuration.");
            }
        }
        return provider;
    }
}
