/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.mgt.endpoint.serviceclient.beans;

import org.wso2.carbon.identity.mgt.endpoint.serviceclient.model.ChallengeQuestion;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * Store single challenge question response
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "")
public class ChallengeQuestionResponse implements Serializable {

    private ChallengeQuestion question;
    private String code;
    private String status;

    public ChallengeQuestionResponse() {
        //Default constructor
    }

    public ChallengeQuestion getQuestion() {
        return question;
    }

    public void setQuestion(ChallengeQuestion question) {
        this.question = question;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
