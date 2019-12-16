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

package org.wso2.carbon.identity.mgt.endpoint.util.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;


/**
 * Question
 */
public class Question {

    private String question = null;
    private String questionSetId = null;


    /**
     **/
    public Question question(String question) {
        this.question = question;
        return this;
    }


    @JsonProperty("question")
    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }


    /**
     **/
    public Question questionSetId(String questionSetId) {
        this.questionSetId = questionSetId;
        return this;
    }


    @JsonProperty("question-set-id")
    public String getQuestionSetId() {
        return questionSetId;
    }

    public void setQuestionSetId(String questionSetId) {
        this.questionSetId = questionSetId;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Question question = (Question) o;
        return Objects.equals(this.question, question.question) &&
                Objects.equals(this.questionSetId, question.questionSetId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(question, questionSetId);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Question {\n");

        sb.append("    question: ").append(toIndentedString(question)).append("\n");
        sb.append("    questionSetId: ").append(toIndentedString(questionSetId)).append("\n");
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

