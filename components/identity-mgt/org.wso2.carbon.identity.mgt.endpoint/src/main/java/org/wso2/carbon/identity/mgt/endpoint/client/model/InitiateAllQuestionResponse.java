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
 * InitiateAllQuestionResponse
 */
public class InitiateAllQuestionResponse extends ResponseWithHeaders  {

    private String key = null;
    private List<Question> questions = new ArrayList<Question>();
    private Link link = null;


    /**
     **/
    public InitiateAllQuestionResponse key(String key) {
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
    public InitiateAllQuestionResponse questions(List<Question> questions) {
        this.questions = questions;
        return this;
    }


    @JsonProperty("questions")
    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }


    /**
     **/
    public InitiateAllQuestionResponse link(Link link) {
        this.link = link;
        return this;
    }


    @JsonProperty("link")
    public Link getLink() {
        return link;
    }

    public void setLink(Link link) {
        this.link = link;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        InitiateAllQuestionResponse initiateAllQuestionResponse = (InitiateAllQuestionResponse) o;
        return Objects.equals(this.key, initiateAllQuestionResponse.key) &&
                Objects.equals(this.questions, initiateAllQuestionResponse.questions) &&
                Objects.equals(this.link, initiateAllQuestionResponse.link);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, questions, link);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class InitiateAllQuestionResponse {\n");

        sb.append("    key: ").append(toIndentedString(key)).append("\n");
        sb.append("    questions: ").append(toIndentedString(questions)).append("\n");
        sb.append("    link: ").append(toIndentedString(link)).append("\n");
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

