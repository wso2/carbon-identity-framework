/*
 *
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.identity.mgt.endpoint.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * AnswerVerificationRequest
 */
public class AnswerVerificationRequest {

    private String key = null;
    private List<SecurityAnswer> answers = new ArrayList<SecurityAnswer>();
    private List<Property> properties = new ArrayList<Property>();


    /**
     **/
    public AnswerVerificationRequest key(String key) {
        this.key = key;
        return this;
    }

    @JsonProperty("key")
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }


    /**
     **/
    public AnswerVerificationRequest answers(List<SecurityAnswer> answers) {
        this.answers = answers;
        return this;
    }


    @JsonProperty("answers")
    public List<SecurityAnswer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<SecurityAnswer> answers) {
        this.answers = answers;
    }


    /**
     **/
    public AnswerVerificationRequest properties(List<Property> properties) {
        this.properties = properties;
        return this;
    }


    @JsonProperty("properties")
    public List<Property> getProperties() {
        return properties;
    }

    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AnswerVerificationRequest answerVerificationRequest = (AnswerVerificationRequest) o;
        return Objects.equals(this.key, answerVerificationRequest.key) &&
                Objects.equals(this.answers, answerVerificationRequest.answers) &&
                Objects.equals(this.properties, answerVerificationRequest.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, answers, properties);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AnswerVerificationRequest {\n");

        sb.append("    key: ").append(toIndentedString(key)).append("\n");
        sb.append("    answers: ").append(toIndentedString(answers)).append("\n");
        sb.append("    properties: ").append(toIndentedString(properties)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}

