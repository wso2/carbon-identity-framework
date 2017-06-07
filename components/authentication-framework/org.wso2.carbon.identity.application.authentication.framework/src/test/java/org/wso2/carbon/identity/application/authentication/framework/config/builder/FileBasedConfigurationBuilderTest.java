/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.config.builder;

import junit.framework.TestCase;
import org.wso2.carbon.identity.application.authentication.framework.config.model.AuthenticationChain;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.List;

/**
 * Tests reading of some of the fragments from the configuration.
 *
 */
public class FileBasedConfigurationBuilderTest extends TestCase {

    protected void setUp() throws NoSuchFieldException, IllegalAccessException {
        URL root = this.getClass().getClassLoader().getResource(".");
        File file = new File(root.getPath());
        System.setProperty("carbon.home", file.toString());

        Field fileBasedConfigurationBuilderInstance = FileBasedConfigurationBuilder.class.getDeclaredField("instance");
        fileBasedConfigurationBuilderInstance.setAccessible(true);
        fileBasedConfigurationBuilderInstance.set(null, null);
    }

    public void testFindSequenceByApplicationId_MultiAuthChains() throws Exception {
        List<SequenceConfig> sequenceConfigs = getSequenceConfigs();
        SequenceConfig sequence0 = sequenceConfigs.get(0);
        assertNotNull(sequence0);
        assertNotNull(sequence0.getAuthenticationChainMap().get("test-chain-0"));
        assertNotNull(sequence0.getAuthenticationChainMap().get("test-chain-1"));
        assertNotNull(sequence0.getAuthenticationChainMap().get("test-chain-2"));

        AuthenticationChain chain0 = sequence0.getAuthenticationChainMap().get("test-chain-1");
        assertNotNull(chain0.getStepConfigList());

        assertEquals(2, chain0.getStepConfigList().size());
    }

    public void testFindSequenceByApplicationId_MultiAuthChains_Acr() throws Exception {
        List<SequenceConfig> sequenceConfigs = getSequenceConfigs();
        SequenceConfig sequence0 = sequenceConfigs.get(0);

        assertNotNull(sequence0.getChainForAcr("selector1"));
        assertNotNull(sequence0.getChainForAcr("selector2"));
        assertNotNull(sequence0.getChainForAcr("selector3"));
        assertNotNull(sequence0.getChainForAcr("loa1"));

        assertEquals(sequence0.getChainForAcr("selector2"), sequence0.getChainForAcr("selector3"));
    }

    private List<SequenceConfig> getSequenceConfigs()
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        FileBasedConfigurationBuilder configurationBuilder = FileBasedConfigurationBuilder.getInstance();
        Method buildConfigurationMethod = FileBasedConfigurationBuilder.class
                .getDeclaredMethod("buildConfiguration", InputStream.class);
        buildConfigurationMethod.setAccessible(true);
        InputStream file = DefaultAdaptiveAuthEvaluatorTest.class
                .getResourceAsStream("application-authentication-multi-chain.xml");
        buildConfigurationMethod.invoke(configurationBuilder, file);
        return FileBasedConfigurationBuilder.getInstance().getSequenceList();
    }
}