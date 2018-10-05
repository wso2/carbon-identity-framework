/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.identity.application.common.model;

import org.apache.axiom.om.OMElement;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.Iterator;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Data transfer representation for Default Authentication Sequence.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "DefaultAuthenticationSequence")
public class DefaultAuthenticationSequence implements Serializable {

    @XmlElement(name = "name")
    private String name;

    @XmlElement(name = "description")
    private String description;

    @XmlElement(name = "seqContent")
    private LocalAndOutboundAuthenticationConfig seqContent;

    @XmlElement(name = "seqContentXml")
    private String seqContentXml;

    public static DefaultAuthenticationSequence build(OMElement defaultAuthSeqOM) {

        DefaultAuthenticationSequence authenticationSequence = new DefaultAuthenticationSequence();
        Iterator<?> iter = defaultAuthSeqOM.getChildElements();
        while (iter.hasNext()) {
            OMElement member = (OMElement) iter.next();
            if ("name".equals(member.getLocalName())) {
                if (StringUtils.isNotBlank(member.getText())) {
                    authenticationSequence.setName(member.getText());
                }
            } else if ("description".equals(member.getLocalName())) {
                if (StringUtils.isNotBlank(member.getText())) {
                    authenticationSequence.setDescription(member.getText());
                }
            } else if ("seqContent".equals(member.getLocalName())) {
                if (StringUtils.isNotBlank(member.getText())) {
                    authenticationSequence.setContentXml(member.getText());
                }
            }
        }
        return authenticationSequence;
    }

    /**
     * Get default authentication sequence name.
     *
     * @return default authentication sequence name
     */
    public String getName() {

        return name;
    }

    /**
     * Set default authentication sequence name.
     *
     * @param name default authentication sequence name
     */
    public void setName(String name) {

        this.name = name;
    }

    /**
     * Get default authentication sequence description.
     *
     * @return default authentication sequence description
     */
    public String getDescription() {

        return description;
    }

    /**
     * Set default authentication sequence description.
     *
     * @param description default authentication sequence description
     */
    public void setDescription(String description) {

        this.description = description;
    }

    /**
     * Get default authentication sequence content.
     *
     * @return default authentication sequence content
     */
    public LocalAndOutboundAuthenticationConfig getContent() {

        return seqContent;
    }

    /**
     * Set default authentication sequence content.
     *
     * @param content default authentication sequence content
     */
    public void setContent(LocalAndOutboundAuthenticationConfig content) {

        this.seqContent = content;
    }

    /**
     * Get default authentication sequence content in XML.
     *
     * @return default authentication sequence in XML
     */
    public String getContentXml() {

        return seqContentXml;
    }

    /**
     * Set default authentication sequence XML content.
     *
     * @param contentXml default authentication sequence XML content
     */
    public void setContentXml(String contentXml) {

        this.seqContentXml = contentXml;
    }
}

