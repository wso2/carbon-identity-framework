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

import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * InitiateQuestionResponse
 */
public class InitiateQuestionResponse extends ResponseWithHeaders {
  
  private String key = null;
  private Question question = null;
  private Link link = null;

  
  /**
   **/
  public InitiateQuestionResponse key(String key) {
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
  public InitiateQuestionResponse question(Question question) {
    this.question = question;
    return this;
  }
  

  @JsonProperty("question")
  public Question getQuestion() {
    return question;
  }
  public void setQuestion(Question question) {
    this.question = question;
  }


  /**
   **/
  public InitiateQuestionResponse link(Link link) {
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
    InitiateQuestionResponse initiateQuestionResponse = (InitiateQuestionResponse) o;
    return Objects.equals(this.key, initiateQuestionResponse.key) &&
        Objects.equals(this.question, initiateQuestionResponse.question) &&
        Objects.equals(this.link, initiateQuestionResponse.link);
  }

  @Override
  public int hashCode() {
    return Objects.hash(key, question, link);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class InitiateQuestionResponse {\n");

    sb.append("    key: ").append(toIndentedString(key)).append("\n");
    sb.append("    question: ").append(toIndentedString(question)).append("\n");
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

