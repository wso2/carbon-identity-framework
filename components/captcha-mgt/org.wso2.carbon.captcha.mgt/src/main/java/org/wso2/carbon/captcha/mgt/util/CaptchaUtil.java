/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.captcha.mgt.util;

import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.util.Config;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.captcha.mgt.beans.CaptchaInfoBean;
import org.wso2.carbon.captcha.mgt.constants.CaptchaMgtConstants;
import org.wso2.carbon.captcha.mgt.internal.CaptchaMgtServiceComponent;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.AccessControlConstants;
import org.wso2.carbon.user.core.AuthorizationManager;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;

/**
 * CaptchaUtil - Captcha related Utility methods (for kaptcha).
 */
public class CaptchaUtil {
    private static final Log log = LogFactory.getLog(CaptchaUtil.class);

    /**
     * Clean the old captcha's from the registry.
     *
     * @throws Exception RegistryException, if cleaning the captcha's fail in middle.
     */
    public static void cleanOldCaptchas() throws Exception {
        // we will clean captchas older than 20mins
        new Thread() {
            public void run() {
                //  As this is a util method. setting the  super tenant
                PrivilegedCarbonContext context = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                context.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
                context.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
                try {
                    Registry superTenantRegistry =
                            CaptchaMgtServiceComponent.getConfigSystemRegistry(
                                    MultitenantConstants.SUPER_TENANT_ID);
                    if (!superTenantRegistry.resourceExists(
                            CaptchaMgtConstants.CAPTCHA_DETAILS_PATH)) {
                        return;  // resource doesn't exist in the superTenantRegistry. just return.
                    }
                    CollectionImpl c = (CollectionImpl) superTenantRegistry.get(
                            CaptchaMgtConstants.CAPTCHA_DETAILS_PATH);
                    String[] childPaths = c.getChildren();
                    for (String childPath : childPaths) {
                        Resource resource = superTenantRegistry.get(childPath);
                        long createdTime = resource.getCreatedTime().getTime();
                        long currentTime = new Date().getTime();

                        // if the current time exceeds the given life time. Set as 20 mins currently.
                        if (currentTime >= createdTime +
                                CaptchaMgtConstants.CAPTCHA_IMG_TIMEOUT_MIN * 60 * 1000) {
                            // just remove the image files
                            String imagePath = resource.getProperty(
                                    CaptchaMgtConstants.CAPTCHA_PATH_PROPERTY_KEY);
                            // deleting the captcha image
                            superTenantRegistry.delete(imagePath);

                            // delete the registry entry
                            superTenantRegistry.delete(childPath);
                        }

                    }
                } catch (RegistryException e) {
                    String msg = "Error in cleaning old captchas.";
                    log.error(msg, e);
                }
            }
        }.start();
    }

    /**
     * Clean the captcha
     *
     * @param secretKey secret key
     * @throws Exception RegistryException
     */
    public static void cleanCaptcha(String secretKey) throws Exception {
        String recordPath = CaptchaMgtConstants.CAPTCHA_DETAILS_PATH +
                RegistryConstants.PATH_SEPARATOR + secretKey;
        Registry superTenantRegistry = CaptchaMgtServiceComponent.getConfigSystemRegistry(
                MultitenantConstants.SUPER_TENANT_ID);
        Resource resource = superTenantRegistry.get(recordPath);

        String imagePath = resource.getProperty(
                CaptchaMgtConstants.CAPTCHA_PATH_PROPERTY_KEY);
        // delete the captcha image
        superTenantRegistry.delete(imagePath);

        // delete the registry entry
        superTenantRegistry.delete(recordPath);
        // clean the old captchas as well.
        cleanOldCaptchas();
    }

    /**
     * Generate the captcha image.
     *
     * @return CaptchaInfoBean
     * @throws Exception - no exception handling here.
     *                   Exceptions in generating the captcha are thrown as they are.
     */
    public static CaptchaInfoBean generateCaptchaImage() throws Exception {
        String randomSecretKey = UUID.randomUUID().toString();  //random string for the captcha.
        String imagePath = CaptchaMgtConstants.CAPTCHA_IMAGES_PATH +
                RegistryConstants.PATH_SEPARATOR + randomSecretKey + ".jpg";

        Config config = new Config(new Properties());
        Producer captchaProducer = config.getProducerImpl();
        String captchaText = captchaProducer.createText();

        BufferedImage image = captchaProducer.createImage(captchaText);

        File tempFile = File.createTempFile("temp-", ".jpg");

        try {
            ImageIO.write(image, "jpg", tempFile);

            byte[] imageBytes = CarbonUtils.getBytesFromFile(tempFile);

            // saving the image
            Registry superTenantRegistry = CaptchaMgtServiceComponent.getConfigSystemRegistry(
                    MultitenantConstants.SUPER_TENANT_ID);
            Resource imageResource = superTenantRegistry.newResource();
            imageResource.setContent(imageBytes);
            superTenantRegistry.put(imagePath, imageResource);


            // prepare the captcha info bean
            CaptchaInfoBean captchaInfoBean = new CaptchaInfoBean();
            captchaInfoBean.setSecretKey(randomSecretKey);   //random generated value as secret key
            captchaInfoBean.setImagePath("registry" + RegistryConstants.PATH_SEPARATOR + "resource" +
                    RegistryConstants.CONFIG_REGISTRY_BASE_PATH + imagePath);

            // now create an entry in the registry on the captcha
            Resource recordResource = superTenantRegistry.newResource();
            ((ResourceImpl) recordResource).setVersionableChange(false); // no need to version
            recordResource.setProperty(CaptchaMgtConstants.CAPTCHA_TEXT_PROPERTY_KEY, captchaText);
            recordResource.setProperty(CaptchaMgtConstants.CAPTCHA_PATH_PROPERTY_KEY, imagePath);

            superTenantRegistry.put(CaptchaMgtConstants.CAPTCHA_DETAILS_PATH +
                            RegistryConstants.PATH_SEPARATOR + randomSecretKey,
                    recordResource);
            if (log.isDebugEnabled()) {
                log.debug("Successfully generated the captcha image.");
            }
            return captchaInfoBean;
        } finally {
            if (!tempFile.delete()) {
                log.warn("Could not delete " + tempFile.getAbsolutePath());
            }
        }
    }

    /**
     * Validates that the user entered the correct string displayed in the captcha.
     *
     * @param captchaInfoBean Captcha details
     * @throws Exception, if the captcha validation fails, it will throw exception.
     *                    Method completes successfully if the user input matches the captcha image shown.
     */
    public static void validateCaptcha(CaptchaInfoBean captchaInfoBean) throws Exception {
        String userAnswer = captchaInfoBean.getUserAnswer();    // user's answer for the captcha
        if (userAnswer.equals("")) {
            // if no user answer given we will throw an error
            String msg = CaptchaMgtConstants.CAPTCHA_ERROR_MSG +
                    " User has not answered to captcha text.";
            log.error(msg);
            throw new Exception(msg);
        }
        String secretKey = captchaInfoBean.getSecretKey();  // gets the random generated secret key.

        String recordPath = CaptchaMgtConstants.CAPTCHA_DETAILS_PATH +
                RegistryConstants.PATH_SEPARATOR + secretKey;
        Registry superTenantRegistry = CaptchaMgtServiceComponent.getConfigSystemRegistry(
                MultitenantConstants.SUPER_TENANT_ID);
        if (!superTenantRegistry.resourceExists(recordPath)) {
            String msg = "The captcha details are not available.";
            log.error(msg);
            throw new Exception(msg);
        }

        Resource resource = superTenantRegistry.get(recordPath);
        String captchaText = resource.getProperty(
                CaptchaMgtConstants.CAPTCHA_TEXT_PROPERTY_KEY);
        if (captchaText == null) {
            String msg = "The captcha details are not available.";
            log.error(msg);
            throw new Exception(msg);
        }
        if (!captchaText.equals(userAnswer)) {   //wrong user input
            String msg = CaptchaMgtConstants.CAPTCHA_ERROR_MSG +
                    " The user's answer doesn't match the captcha text.";
            log.error(msg);
            throw new Exception(msg);
        }
        // if all goes well, we will reach here.
        if (log.isDebugEnabled()) {
            log.debug("Successfully validated the captcha.");
        }
    }

    /**
     * Processes the CaptchaInfoBean object
     *
     * @param captchaInfoBean Captcha Information
     * @throws Exception, if processing the bean failed.
     */
    public static void processCaptchaInfoBean(CaptchaInfoBean captchaInfoBean) throws Exception {
        // Validate the captcha
        try {
            CaptchaUtil.validateCaptcha(captchaInfoBean);
        } catch (Exception e) {
            String msg = CaptchaMgtConstants.CAPTCHA_ERROR_MSG;
            log.error(msg, e);
            throw new AxisFault(msg);
        } finally {
            try {
                CaptchaUtil.cleanCaptcha(captchaInfoBean.getSecretKey());
            } catch (Exception e) {
                String msg = "Error in cleaning captcha. ";
                log.error(msg, e);
                // not throwing the exception in finally more up.
            }
        }
    }


    public static void setAnonAccessToCaptchaImages() throws Exception {
        UserRegistry systemTenantRegistry = CaptchaMgtServiceComponent.getConfigSystemRegistry(
                MultitenantConstants.SUPER_TENANT_ID);
        setAnonAuthorization(RegistryConstants.CONFIG_REGISTRY_BASE_PATH +
                        CaptchaMgtConstants.CAPTCHA_IMAGES_PATH,
                systemTenantRegistry.getUserRealm());
    }

    public static void setAnonAuthorization(String path, UserRealm userRealm)
            throws RegistryException {

        if (userRealm == null) {
            return;
        }

        try {
            AuthorizationManager accessControlAdmin = userRealm.getAuthorizationManager();
            String everyoneRole = CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME;

            accessControlAdmin.authorizeRole(everyoneRole, path, ActionConstants.GET);
            accessControlAdmin.denyRole(everyoneRole, path, ActionConstants.PUT);
            accessControlAdmin.denyRole(everyoneRole, path, ActionConstants.DELETE);
            accessControlAdmin.denyRole(everyoneRole, path, AccessControlConstants.AUTHORIZE);

        } catch (UserStoreException e) {
            String msg = "Could not set authorizations for the " + path + ".";
            log.error(msg, e);
            throw new RegistryException(msg);
        }
    }

}
