/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
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

package org.wso2.carbon.identity.framework.common.testng;

import org.apache.commons.lang.StringUtils;
import org.testng.IAlterSuiteListener;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.xml.XmlSuite;
import org.wso2.carbon.identity.framework.testutil.log.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Duplicates the test-ng suites for the number of log levels present in the testng.xml.
 * Reads the parameters <code>log-levels</code>
 *
 * e.g.
 * <pre>
 * <code>
 *     <suite name="Suite1" verbose="1" >
 *       <parameter name="log-levels" value="debug, info"/>
 *       <test name="WebFinger-Unit-Test">
 *          <classes>
 *              <class name="org.wso2.carbon.identity.webfinger.builders.WebFingerOIDCResponseBuilderTest"/>
 *          </classes>
 *       </test>
 *     </suite>
 * </code>
 * </pre>
 */
public class LogLevelChangeListener implements IAlterSuiteListener, ISuiteListener {

    private static final String LOG_LEVEL_PARAM_NAME = "log-level";
    private static final String LOG_LEVELS_PARAM_NAME = "log-levels";

    @Override
    public void alter(List<XmlSuite> suites) {
        List<XmlSuite> cloned = new ArrayList<>(suites);
        suites.clear();

        for (XmlSuite suite : cloned) {
            String logLevels = suite.getParameter(LOG_LEVELS_PARAM_NAME);
            if (StringUtils.isNotEmpty(logLevels)) {
                List<XmlSuite> newSuites = createSuites(logLevels, suite);
                if (!newSuites.isEmpty()) {
                    suites.addAll(newSuites);
                }
            } else {
                suites.add(suite);
            }
        }
    }

    /**
     * Creates the suite list with given parameters.
     *
     * @param logLevels
     * @param suite
     * @return
     */
    private List<XmlSuite> createSuites(String logLevels, XmlSuite suite) {
        String[] levels = logLevels.split(",");
        ArrayList<XmlSuite> result = new ArrayList<>();
        for (String level : levels) {
            level = level.trim();
            if (StringUtils.isNotEmpty(level)) {
                suite.getParameters().put(LOG_LEVEL_PARAM_NAME, level);
            }
            XmlSuite suite1 = (XmlSuite) suite.clone();
            suite1.setName(suite1.getName() + "-" + level);
            result.add(suite1);
        }
        return result;
    }

    @Override
    public void onStart(ISuite iSuite) {
        String logLevel = iSuite.getParameter(LOG_LEVEL_PARAM_NAME);
        if (StringUtils.isNotEmpty(logLevel)) {
            LogUtil.configureLogLevel(logLevel);
        }
    }

    @Override
    public void onFinish(ISuite iSuite) {

    }
}
