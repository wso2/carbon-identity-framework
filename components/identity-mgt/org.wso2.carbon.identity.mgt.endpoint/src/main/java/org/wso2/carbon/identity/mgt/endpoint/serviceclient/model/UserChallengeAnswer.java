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

package org.wso2.carbon.identity.mgt.endpoint.serviceclient.model;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * store challenge question answer along with challenge question properties
 */
@XmlRootElement(name = "answer")
public class UserChallengeAnswer implements Serializable {

    private ChallengeQuestion question;

    private String answer;

    public UserChallengeAnswer() {
    }

    public UserChallengeAnswer(ChallengeQuestion question, String answer) {
        this.question = question;
        this.answer = answer;
    }

    public ChallengeQuestion getQuestion() {
        return question;
    }

    public void setQuestion(ChallengeQuestion question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}
