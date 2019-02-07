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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.namespace.QName;

/**
 * Holds a dynamic authentication script.
 * Contains script as a string and several other meta-information related to script.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "AuthenticationScript")
public class AuthenticationScriptConfig implements Serializable {

    private static final long serialVersionUID = -16127229981193884L;

    public static final QName ATTR_TYPE = new QName(null, "language");
    public static final QName ATTR_ENABLED = new QName(null, "enabled");
    public static final String LANGUAGE_JAVASCRIPT = "application/javascript";


    @XmlAttribute(name = "language")
    private String language = LANGUAGE_JAVASCRIPT;

    @XmlValue
    private String content;

    @XmlAttribute(name = "enabled")
    private boolean enabled;

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
        String enabled = scriptOM.getAttributeValue(ATTR_ENABLED);
        scriptConfig.setLanguage(type);
        scriptConfig.setContent(scriptOM.getText());
        if (Boolean.parseBoolean(enabled)) {
            scriptConfig.setEnabled(true);
        }
        return scriptConfig;
    }

    /**
     * @return the language of the Script.
     */
    public String getLanguage() {
        return language;
    }

    /**
     * @param language the language of the Script.
     */
    public void setLanguage(String language) {
        this.language = language;
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

    /**
     * @return whether the JavaScript based conditional step enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @param enabled specify whether the script is enabled.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}