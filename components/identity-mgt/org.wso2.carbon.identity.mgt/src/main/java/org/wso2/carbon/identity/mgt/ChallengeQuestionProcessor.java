/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
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

package org.wso2.carbon.identity.mgt;


import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.mgt.constants.IdentityMgtConstants;
import org.wso2.carbon.identity.mgt.dto.ChallengeQuestionDTO;
import org.wso2.carbon.identity.mgt.dto.ChallengeQuestionIdsDTO;
import org.wso2.carbon.identity.mgt.dto.UserChallengesDTO;
import org.wso2.carbon.identity.mgt.internal.IdentityMgtServiceComponent;
import org.wso2.carbon.identity.mgt.util.Utils;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.core.UserStoreException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * process user challenges and questions
 */
public class ChallengeQuestionProcessor {

    private static final Log log = LogFactory.getLog(ChallengeQuestionProcessor.class);

    /**
     * @return
     * @throws IdentityException
     */
    public List<ChallengeQuestionDTO> getAllChallengeQuestions() throws IdentityException {

        List<ChallengeQuestionDTO> questionDTOs = new ArrayList<ChallengeQuestionDTO>();
        try {
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            Registry registry = IdentityMgtServiceComponent.getRegistryService().
                    getConfigSystemRegistry(tenantId);
            if (registry.resourceExists(IdentityMgtConstants.IDENTITY_MANAGEMENT_QUESTIONS)) {
                Collection collection = (Collection) registry.
                        get(IdentityMgtConstants.IDENTITY_MANAGEMENT_QUESTIONS);
                String[] children = collection.getChildren();
                for (String child : children) {
                    Resource resource = registry.get(child);
                    String question = resource.getProperty("question");
                    String isPromoteQuestion = resource.getProperty("isPromoteQuestion");
                    String questionSetId = resource.getProperty("questionSetId");
                    if (question != null) {
                        ChallengeQuestionDTO questionDTO = new ChallengeQuestionDTO();
                        questionDTO.setQuestion(question);
                        if (isPromoteQuestion != null) {
                            questionDTO.setPromoteQuestion(Boolean.parseBoolean(isPromoteQuestion));
                        }
                        if (questionSetId != null) {
                            questionDTO.setQuestionSetId(questionSetId);
                        }
                        questionDTO.setPromoteQuestion(false);
                        questionDTOs.add(questionDTO);
                    }
                }

            }
        } catch (RegistryException e) {
            throw IdentityException.error(e.getMessage(), e);
        }
        return questionDTOs;
    }

    /**
     * @param questionDTOs
     * @throws IdentityException
     */
    public void setChallengeQuestions(ChallengeQuestionDTO[] questionDTOs) throws IdentityException {
        Registry registry = null;
        try {
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            registry = IdentityMgtServiceComponent.getRegistryService().getConfigSystemRegistry(tenantId);

            if (!registry.resourceExists(IdentityMgtConstants.IDENTITY_MANAGEMENT_PATH)) {
                Collection securityQuestionResource = registry.newCollection();
                registry.put(IdentityMgtConstants.IDENTITY_MANAGEMENT_PATH, securityQuestionResource);
            }
            Resource identityMgtResource = registry.get(IdentityMgtConstants.IDENTITY_MANAGEMENT_PATH);
            if (identityMgtResource != null) {
                String questionCollectionPath = IdentityMgtConstants.IDENTITY_MANAGEMENT_QUESTIONS;
                if (registry.resourceExists(questionCollectionPath)) {
                    registry.delete(questionCollectionPath);
                }

                Collection questionCollection = registry.newCollection();
                registry.put(questionCollectionPath, questionCollection);

                for (int i = 0; i < questionDTOs.length; i++) {
                    Resource resource = registry.newResource();
                    resource.addProperty("question", questionDTOs[i].getQuestion());
                    resource.addProperty("isPromoteQuestion", String.valueOf(questionDTOs[i].isPromoteQuestion()));
                    resource.addProperty("questionSetId", questionDTOs[i].getQuestionSetId());
                    registry.put(IdentityMgtConstants.IDENTITY_MANAGEMENT_QUESTIONS +
                            RegistryConstants.PATH_SEPARATOR + "question" + i +
                            RegistryConstants.PATH_SEPARATOR, resource);
                }
            }
        } catch (RegistryException e) {
            throw IdentityException.error("Error while setting challenge question.", e);
        }

    }

    /**
     * // TODO manage oder
     *
     * @param userName
     * @param tenantId
     * @param adminService
     * @return
     */
    public UserChallengesDTO[] getChallengeQuestionsOfUser(String userName, int tenantId,
                                                           boolean adminService) {

        List<UserChallengesDTO> challengesDTOs = new ArrayList<UserChallengesDTO>();
        try {
            if (log.isDebugEnabled()) {
                log.debug("Retrieving Challenge question from the user profile.");
            }
            List<String> challengesUris = getChallengeQuestionUris(userName, tenantId);

            for (int i = 0; i < challengesUris.size(); i++) {
                String challengesUri = challengesUris.get(i).trim();
                String challengeValue = Utils.getClaimFromUserStoreManager(userName,
                        tenantId, challengesUri);

                String[] challengeValues = challengeValue.
                        split(IdentityMgtConfig.getInstance().getChallengeQuestionSeparator());
                if (challengeValues != null && challengeValues.length == 2) {
                    UserChallengesDTO dto = new UserChallengesDTO();
                    dto.setId(challengesUri);
                    dto.setQuestion(challengeValues[0].trim());
                    if (adminService) {
                        dto.setAnswer(challengeValues[1].trim());
                    }
                    dto.setOrder(i);
                    dto.setPrimary(false);
                    challengesDTOs.add(dto);
                }
            }

        } catch (Exception e) {
            String msg = "No associated challenge question found for the user";
            if(log.isDebugEnabled()){
                log.debug(msg, e);
            }
        }

        if (!challengesDTOs.isEmpty()) {
            return challengesDTOs.toArray(new UserChallengesDTO[challengesDTOs.size()]);
        } else {
            return new UserChallengesDTO[0];
        }

    }


    public UserChallengesDTO getUserChallengeQuestion(String userName, int tenantId,
                                                      boolean adminService) throws IdentityMgtServiceException {

        UserChallengesDTO dto = null;

        List<UserChallengesDTO> challengesDTOs = new ArrayList<UserChallengesDTO>();
        try {
            if (log.isDebugEnabled()) {
                log.debug("Retrieving Challenge question from the user profile.");
            }
            List<String> challengesUris = getChallengeQuestionUris(userName, tenantId);

            for (int i = 0; i < challengesUris.size(); i++) {
                String challengesUri = challengesUris.get(i).trim();
                String challengeValue = Utils.getClaimFromUserStoreManager(userName, tenantId,
                        challengesUri);

                String[] challengeValues = challengeValue.split(IdentityMgtConfig.getInstance()
                        .getChallengeQuestionSeparator());
                if (challengeValues != null && challengeValues.length == 2) {
                    dto = new UserChallengesDTO();
                    dto.setId(challengesUri);
                    dto.setQuestion(challengeValues[0].trim());
                    if (adminService) {
                        dto.setAnswer(challengeValues[1].trim());
                    }
                    dto.setOrder(i);
                    dto.setPrimary(false);
                    challengesDTOs.add(dto);
                }
            }

        } catch (Exception e) {
            String msg = "No associated challenge question found for the user";
            if(log.isDebugEnabled()){
                log.debug(msg, e);
            }
        }

        return dto;
    }

    public UserChallengesDTO getUserChallengeQuestion(String userName, int tenantId,
                                                      String challengesUri) throws IdentityMgtServiceException {

        UserChallengesDTO dto = null;

        try {
            if (log.isDebugEnabled()) {
                log.debug("Retrieving Challenge question from the user profile.");
            }

            String challengeValue = Utils.getClaimFromUserStoreManager(userName, tenantId,
                    challengesUri);

            if (challengeValue != null) {

                String[] challengeValues = challengeValue.split(IdentityMgtConfig.getInstance()
                        .getChallengeQuestionSeparator());
                if (challengeValues != null && challengeValues.length == 2) {
                    dto = new UserChallengesDTO();
                    dto.setId(challengesUri);
                    dto.setQuestion(challengeValues[0].trim());

                }
            } else {
                dto = new UserChallengesDTO();
                dto.setError("Challenge questions have not been answered by the user: " + userName);
            }

        } catch (Exception e) {
            String errorMsg = "Error while getting the challenge questions for the user: "
                    + userName;
            if(log.isDebugEnabled()){
                log.debug(errorMsg, e);
            }
            dto = new UserChallengesDTO();
            dto.setError(errorMsg);
            throw new IdentityMgtServiceException(errorMsg,e);
        }

        return dto;

    }

    public ChallengeQuestionIdsDTO getUserChallengeQuestionIds(String userName, int tenantId)
            throws IdentityMgtServiceException {

        ChallengeQuestionIdsDTO dto = new ChallengeQuestionIdsDTO();

        if (log.isDebugEnabled()) {
            log.debug("Retrieving Challenge question ids from the user profile.");
        }
        List<String> challengesUris = getChallengeQuestionUris(userName, tenantId);

        if (challengesUris.isEmpty()) {
            String msg = "No associated challenge question found for the user";
            if (log.isDebugEnabled()) {
                log.debug(msg);
            }
            throw new IdentityMgtServiceException(msg);
        }

        String[] ids = new String[challengesUris.size()];

        for (int i = 0; i < challengesUris.size(); i++) {
            ids[i] = challengesUris.get(i).trim();
        }

        dto.setIds(ids);

        return dto;

    }

    /**
     * @param userName
     * @param tenantId
     * @return
     */
    public List<String> getChallengeQuestionUris(String userName, int tenantId) throws IdentityMgtServiceException{

        if (log.isDebugEnabled()) {
            log.debug("Challenge Question from the user profile.");
        }

        List<String> challenges = new ArrayList<String>();
        String claimValue = null;
        String[] challengesUris;

        try {
            claimValue = Utils.getClaimFromUserStoreManager(userName, tenantId,
                    "http://wso2.org/claims/challengeQuestionUris");
        } catch (IdentityException e) {
            throw new IdentityMgtServiceException("Error while getting cliams.", e);
        }

        if (claimValue != null) {
            if (claimValue.contains(IdentityMgtConfig.getInstance().getChallengeQuestionSeparator())) {
                challengesUris = claimValue.split(IdentityMgtConfig.getInstance().getChallengeQuestionSeparator());
            } else {
                challengesUris = new String[]{claimValue.trim()};
            }

            for (String challengesUri : challengesUris) {
                if (StringUtils.isNotBlank(challengesUri)) {
                    challenges.add(challengesUri.trim());
                }
            }
        }

        return challenges;
    }

    /**
     * @param userName
     * @param tenantId
     * @return
     */
    public int getNoOfChallengeQuestions(String userName, int tenantId) throws IdentityMgtServiceException {

        List<String> questions = getChallengeQuestionUris(userName, tenantId);
        return questions.size();
    }

    /**
     * @param userName
     * @param tenantId
     * @param challengesDTOs
     * @throws IdentityException
     */
    public void setChallengesOfUser(String userName, int tenantId,
                                    UserChallengesDTO[] challengesDTOs) throws IdentityException {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Challenge Question from the user profile.");
            }
            List<String> challengesUris = new ArrayList<String>();
            String challengesUrisValue = "";
            String separator = IdentityMgtConfig.getInstance().getChallengeQuestionSeparator();

            if (!ArrayUtils.isEmpty(challengesDTOs)) {
                for (UserChallengesDTO dto : challengesDTOs) {
                    if (dto.getId() != null && dto.getQuestion() != null && dto.getAnswer() != null) {
                        String oldValue = Utils.
                                getClaimFromUserStoreManager(userName, tenantId, dto.getId().trim());

                        if (oldValue != null && oldValue.contains(separator)) {
                            String oldAnswer = oldValue.split(separator)[1];
                            if (!oldAnswer.trim().equals(dto.getAnswer().trim())) {
                                String claimValue = dto.getQuestion().trim() + separator +
                                        Utils.doHash(dto.getAnswer().trim().toLowerCase());
                                Utils.setClaimInUserStoreManager(userName,
                                        tenantId, dto.getId().trim(), claimValue);
                            }
                        } else {
                            String claimValue = dto.getQuestion().trim() + separator +
                                    Utils.doHash(dto.getAnswer().trim().toLowerCase());
                            Utils.setClaimInUserStoreManager(userName,
                                    tenantId, dto.getId().trim(), claimValue);
                        }
                        challengesUris.add(dto.getId().trim());
                    }
                }

                for (String challengesUri : challengesUris) {
                    if ("".equals(challengesUrisValue)) {
                        challengesUrisValue = challengesUri;
                    } else {
                        challengesUrisValue = challengesUrisValue +
                                IdentityMgtConfig.getInstance().getChallengeQuestionSeparator() + challengesUri;
                    }
                }

                Utils.setClaimInUserStoreManager(userName, tenantId,
                        "http://wso2.org/claims/challengeQuestionUris", challengesUrisValue);

            }
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            String msg = "No associated challenge question found for the user";
            throw IdentityException.error(msg, e);
        }
    }

    /**
     * @param userName
     * @param tenantId
     * @param challengesDTOs
     * @return
     */
    public boolean verifyChallengeQuestion(String userName, int tenantId,
                                           UserChallengesDTO[] challengesDTOs) {

        boolean verification = false;
        try {
            if (log.isDebugEnabled()) {
                log.debug("Challenge Question from the user profile.");
            }

            UserChallengesDTO[] storedDto = getChallengeQuestionsOfUser(userName, tenantId, true);

            for (UserChallengesDTO challengesDTO : challengesDTOs) {
                if (challengesDTO.getAnswer() == null || challengesDTO.getAnswer().trim().length() < 1) {
                    return false;
                }

                for (UserChallengesDTO dto : storedDto) {
                    if ((challengesDTO.getId() == null || !challengesDTO.getId().trim().equals(dto.getId())) &&
                            (challengesDTO.getQuestion() == null || !challengesDTO.getQuestion().
                            trim().equals(dto.getQuestion()))) {
                        continue;

                    }

                    String hashedAnswer = Utils.doHash(challengesDTO.getAnswer().trim().toLowerCase());
                    if (hashedAnswer.equals(dto.getAnswer())) {
                        verification = true;
                    } else {
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            String msg = "No associated challenge question found for the user";
            log.debug(msg, e);
        }

        return verification;
    }

    public boolean verifyUserChallengeAnswer(String userName, int tenantId,
                                             UserChallengesDTO challengesDTO) {

        boolean verification = false;
        try {
            if (log.isDebugEnabled()) {
                log.debug("Challenge Question from the user profile.");
            }

            UserChallengesDTO[] storedDto = getChallengeQuestionsOfUser(userName, tenantId, true);

            if (challengesDTO.getAnswer() == null || challengesDTO.getAnswer().trim().length() < 1) {
                return false;
            }

            for (UserChallengesDTO dto : storedDto) {

                if (dto.getId().equals(challengesDTO.getId())) {

                    String hashedAnswer = Utils.doHash(challengesDTO.getAnswer().trim()
                            .toLowerCase());
                    if (hashedAnswer.equals(dto.getAnswer())) {
                        verification = true;
                    } else {
                        return false;
                    }
                }

            }
        } catch (Exception e) {
            String msg = "No associated challenge question found for the user";
            log.debug(msg, e);
        }

        return verification;
    }

    /**
     * @param userName
     * @param tenantId
     * @return
     */
    public UserChallengesDTO[] getPrimaryChallengeQuestionsOfUser(String userName, int tenantId) {

        List<UserChallengesDTO> challengesDTOs = new ArrayList<>();
        try {
            if (log.isDebugEnabled()) {
                log.debug("Challenge Question from the user profile.");
            }
            String claimValue;

            claimValue = Utils.getClaimFromUserStoreManager(userName, tenantId,
                    "http://wso2.org/claims/primaryChallengeQuestion");

            String[] challenges = claimValue.split(IdentityMgtConfig.getInstance().getChallengeQuestionSeparator());
            for (String challenge : challenges) {
                UserChallengesDTO dto = new UserChallengesDTO();
                String question = challenge.substring(0, challenge.indexOf("="));
                dto.setQuestion(question);
                dto.setPrimary(true);
                challengesDTOs.add(dto);
            }

        } catch (Exception e) {
            String msg = "No associated challenge question found for the user";
            log.debug(msg, e);
        }

        if (!challengesDTOs.isEmpty()) {
            return challengesDTOs.toArray(new UserChallengesDTO[challengesDTOs.size()]);
        } else {
            return new UserChallengesDTO[0];
        }
    }

    /**
     * @param userName
     * @param tenantId
     * @param userChallengesDTOs
     * @return
     * @throws UserStoreException
     */
    public boolean verifyPrimaryChallengeQuestion(String userName, int tenantId,
                                                  UserChallengesDTO[] userChallengesDTOs) {

        boolean verification = false;
        try {
            if (log.isDebugEnabled()) {
                log.debug("Challenge Question from the user profile for user " + userName);
            }
            String claimValue = Utils.getClaimFromUserStoreManager(userName, tenantId,
                    "http://wso2.org/claims/primaryChallengeQuestion");

            if (claimValue == null) {
                log.debug("No associated challenge question found for the user " + userName);
                return false;
            }

            String[] challenges = claimValue.split(IdentityMgtConfig.getInstance().getChallengeQuestionSeparator());
            Map<String, String> challengeMap = new HashMap<String, String>();
            for (int i = 0; i < challenges.length; i = i + 2) {
                challengeMap.put(challenges[i], challenges[i + 1]);
            }

            for (UserChallengesDTO userChallengesDTO : userChallengesDTOs) {
                for (Map.Entry<String, String> entry : challengeMap.entrySet()) {
                    String challengeQuestion = entry.getKey();
                    if (challengeQuestion.equals(userChallengesDTO.getQuestion().trim())) {
                        String challengeAnswer = entry.getValue();
                        if (challengeAnswer.equals(Utils.
                                doHash(userChallengesDTO.getAnswer().trim().toLowerCase()))) {
                            verification = true;
                        } else {
                            return false;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("No associated challenge question found for the user " + userName, e);
        }

        return verification;
    }

}
