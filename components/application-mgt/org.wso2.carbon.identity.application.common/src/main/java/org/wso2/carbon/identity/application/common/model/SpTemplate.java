/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.identity.application.common.model;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Data transfer representation for Service Provider Template.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "SpTemplate")
public class SpTemplate implements Serializable {

    @XmlElement(name = "Name", required = true)
    private String name;

    @XmlElement(name = "Description")
    private String description;

    @XmlElement(name = "Content", required = true)
    private String content;

    public SpTemplate() {
    }

    public SpTemplate(String name, String description, String content) {

        this.name = name;
        this.description = description;
        this.content = content;
    }

    /**
     * Get Service Provider Template Name.
     *
     * @return SP template name
     */
    public String getName() {

        return name;
    }

    /**
     * Set Service Provider Template Name.
     *
     * @param name SP template name
     */
    public void setName(String name) {

        this.name = name;
    }

    /**
     * Get Service Provider Template Description.
     *
     * @return SP template description
     */
    public String getDescription() {

        return description;
    }

    /**
     * Set Service Provider Template Description.
     *
     * @param description SP template description
     */
    public void setDescription(String description) {

        this.description = description;
    }

    /**
     * Get Service Provider Template XML Content.
     *
     * @return SP template content
     */
    public String getContent() {

        return content;
    }

    /**
     * Set Service Provider Template XML Content.
     *
     * @param content SP template content
     */
    public void setContent(String content) {

        this.content = content;
    }
}

