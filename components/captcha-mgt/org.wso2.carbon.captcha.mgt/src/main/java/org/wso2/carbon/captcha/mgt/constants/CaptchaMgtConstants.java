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
package org.wso2.carbon.captcha.mgt.constants;

/*
  Constants for Captcha Management functionality
 */
public class CaptchaMgtConstants {

    public static final String CAPTCHA_DETAILS_PATH =
            "/repository/components/org.wso2.carbon.captcha-details";

    public static final String CAPTCHA_IMAGES_PATH =
            "/repository/components/org.wso2.carbon.captcha-images";

    public static final String CAPTCHA_TEXT_PROPERTY_KEY = "captcha-text";
    public static final String CAPTCHA_PATH_PROPERTY_KEY = "captcha-path";
    public static final int CAPTCHA_IMG_TIMEOUT_MIN = 20;
    public static final String CAPTCHA_ERROR_MSG = "Captcha validation failed.";

}
