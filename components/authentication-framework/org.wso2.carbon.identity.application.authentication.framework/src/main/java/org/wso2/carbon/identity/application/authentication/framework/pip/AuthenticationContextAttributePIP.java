/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.framework.pip;

import org.wso2.balana.ctx.EvaluationCtx;
import org.wso2.carbon.identity.entitlement.pip.AbstractPIPAttributeFinder;

import java.net.URI;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class AuthenticationContextAttributePIP extends AbstractPIPAttributeFinder {

    private static final String PIP_NAME = "AuthenticationContextAttributePIP";

    private static final String SP_NAME_ATTRIBUTE = "http://wso2.org/authentication/sp-name";

    /**
     *  Since we override the {@link #getAttributeValues(URI, URI, URI, String, EvaluationCtx)} this won't be called.
     */
    @Override
    public Set<String> getAttributeValues(String subject, String resource, String action, String environment,
                                          String attributeId, String issuer) throws Exception {
        throw new UnsupportedOperationException("Method unsupported in the context");
    }

    @Override
    public Set<String> getAttributeValues(URI attributeType, URI attributeId, URI category, String issuer,
                                          EvaluationCtx evaluationCtx) throws Exception {


        return super.getAttributeValues(attributeType, attributeId, category, issuer, evaluationCtx);
    }

    @Override
    public void init(Properties properties) throws Exception {

    }

    @Override
    public String getModuleName() {

        return PIP_NAME;
    }

    @Override
    public Set<String> getSupportedAttributes() {

        Set<String> supportedAttributes = new HashSet<>();
        supportedAttributes.add(SP_NAME_ATTRIBUTE);
        return supportedAttributes;
    }
}
