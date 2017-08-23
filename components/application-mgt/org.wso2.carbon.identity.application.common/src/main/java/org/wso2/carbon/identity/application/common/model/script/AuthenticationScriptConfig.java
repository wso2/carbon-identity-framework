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
package org.wso2.carbon.identity.application.common.model.script;

import org.apache.axiom.om.OMElement;

import java.io.Serializable;
import javax.xml.namespace.QName;

/**
 * Holds a dynamic authentication script.
 * Contains script as a string and sevaral other meta-information related to script.
 */
public class AuthenticationScriptConfig implements Serializable {

    private static final long serialVersionUID = -16127229981193883L;

    public static final QName ATTR_TYPE = new QName(null, "type");

    public String type;
    private String content;

    /**
     * Builds the script with Axiom.
     *
     * @param scriptOM OM element of the script.
     * @return the built script. Will return null if supplied OMElement being null or it is not of the correct type.
     */
    public static AuthenticationScriptConfig build(OMElement scriptOM) {
        if (scriptOM == null) {
            return null;
        }

        AuthenticationScriptConfig scriptConfig = new AuthenticationScriptConfig();
        String type = scriptOM.getAttributeValue(ATTR_TYPE);
        scriptConfig.setType(type);
        scriptConfig.setContent(scriptOM.getText());
        return scriptConfig;
    }

    /**
     * @return type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return content
     */
    public String getContent() {
        return content;
    }

    /**
     * @param content
     */
    public void setContent(String content) {
        this.content = content;
    }
}
