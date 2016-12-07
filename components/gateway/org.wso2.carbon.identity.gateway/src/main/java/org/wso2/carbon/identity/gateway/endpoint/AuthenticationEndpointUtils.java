/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.gateway.endpoint;

/**
 * Util class for AuthenticationEndpoint
 */
public class AuthenticationEndpointUtils {

    public static String getLoginPage() {
        return LOGIN_PAGE;
    }

    private static final String LOGIN_PAGE =
            "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" " +
                    "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" +
                    "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
                    "<head>\n" +
                    "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n" +
                    "<title>Login</title>\n" +
                    "<style>\n" +
                    "/* Basics */\n" +
                    "html, body {\n" +
                    "    width: 100%;\n" +
                    "    height: 100%;\n" +
                    "    font-family: \"Helvetica Neue\", Helvetica, sans-serif;\n" +
                    "    color: #444;\n" +
                    "    -webkit-font-smoothing: antialiased;\n" +
                    "    background: #f0f0f0;\n" +
                    "}\n" +
                    "#container {\n" +
                    "    position: fixed;\n" +
                    "    width: 340px;\n" +
                    "    height: 280px;\n" +
                    "    top: 50%;\n" +
                    "    left: 50%;\n" +
                    "    margin-top: -140px;\n" +
                    "    margin-left: -170px;\n" +
                    "\tbackground: #fff;\n" +
                    "    border-radius: 3px;\n" +
                    "    border: 1px solid #ccc;\n" +
                    "    box-shadow: 0 1px 2px rgba(0, 0, 0, .1);\n" +
                    "\t\n" +
                    "}\n" +
                    "form {\n" +
                    "    margin: 0 auto;\n" +
                    "    margin-top: 20px;\n" +
                    "}\n" +
                    "label {\n" +
                    "    color: #555;\n" +
                    "    display: inline-block;\n" +
                    "    margin-left: 18px;\n" +
                    "    padding-top: 10px;\n" +
                    "    font-size: 14px;\n" +
                    "}\n" +
                    "p a {\n" +
                    "    font-size: 11px;\n" +
                    "    color: #aaa;\n" +
                    "    float: right;\n" +
                    "    margin-top: -13px;\n" +
                    "    margin-right: 20px;\n" +
                    " -webkit-transition: all .4s ease;\n" +
                    "    -moz-transition: all .4s ease;\n" +
                    "    transition: all .4s ease;\n" +
                    "}\n" +
                    "p a:hover {\n" +
                    "    color: #555;\n" +
                    "}\n" +
                    "input {\n" +
                    "    font-family: \"Helvetica Neue\", Helvetica, sans-serif;\n" +
                    "    font-size: 12px;\n" +
                    "    outline: none;\n" +
                    "}\n" +
                    "input[type=text],\n" +
                    "input[type=password] {\n" +
                    "    color: #777;\n" +
                    "    padding-left: 10px;\n" +
                    "    margin: 10px;\n" +
                    "    margin-top: 12px;\n" +
                    "    margin-left: 18px;\n" +
                    "    width: 290px;\n" +
                    "    height: 35px;\n" +
                    "\tborder: 1px solid #c7d0d2;\n" +
                    "    border-radius: 2px;\n" +
                    "    box-shadow: inset 0 1.5px 3px rgba(190, 190, 190, .4), 0 0 0 5px #f5f7f8;\n" +
                    "-webkit-transition: all .4s ease;\n" +
                    "    -moz-transition: all .4s ease;\n" +
                    "    transition: all .4s ease;\n" +
                    "\t}\n" +
                    "input[type=text]:hover,\n" +
                    "input[type=password]:hover {\n" +
                    "    border: 1px solid #b6bfc0;\n" +
                    "    box-shadow: inset 0 1.5px 3px rgba(190, 190, 190, .7), 0 0 0 5px #f5f7f8;\n" +
                    "}\n" +
                    "input[type=text]:focus,\n" +
                    "input[type=password]:focus {\n" +
                    "    border: 1px solid #a8c9e4;\n" +
                    "    box-shadow: inset 0 1.5px 3px rgba(190, 190, 190, .4), 0 0 0 5px #e6f2f9;\n" +
                    "}\n" +
                    "#lower {\n" +
                    "    background: #2B507D;\n" +
                    "    width: 100%;\n" +
                    "    height: 64px;\n" +
                    "    margin-top: 20px;\n" +
                    "\t  box-shadow: inset 0 1px 1px #fff;\n" +
                    "    border-top: 1px solid #ccc;\n" +
                    "    border-bottom-right-radius: 3px;\n" +
                    "    border-bottom-left-radius: 3px;\n" +
                    "}\n" +
                    "input[type=checkbox] {\n" +
                    "    margin-left: 20px;\n" +
                    "    margin-top: 30px;\n" +
                    "}\n" +
                    ".check {\n" +
                    "    margin-left: 3px;\n" +
                    "\tfont-size: 11px;\n" +
                    "    color: #444;\n" +
                    "    text-shadow: 0 1px 0 #fff;\n" +
                    "}\n" +
                    "input[type=submit] {\n" +
                    "    float: right;\n" +
                    "    margin-right: 20px;\n" +
                    "    margin-top: 20px;\n" +
                    "    width: 80px;\n" +
                    "    height: 30px;\n" +
                    "font-size: 14px;\n" +
                    "    font-weight: bold;\n" +
                    "    color: #fff;\n" +
                    "    background-color: #F36C0C; /*IE fallback*/\n" +
                    "    border-radius: 5px;\n" +
                    "    border: 1px solid #FFFFFF;\n" +
                    "    cursor: pointer;\n" +
                    "}\n" +
                    "input[type=submit]:hover {\n" +
                    "    background-color: #000000;\n" +
                    "}\n" +
                    "input[type=submit]:active {\n" +
                    "    background-color: #F36C0C;\n" +
                    "}\n" +
                    "</style>\n" +
                    "\n" +
                    "</head>\n" +
                    "\n" +
                    "<body>\n" +
                    "    <!-- Begin Page Content -->\n" +
                    "    <div id=\"container\">\n" +
                    "        <form method=\"POST\" action=\"${callbackURL}\">\n" +
                    "            <label for=\"username\">Username:</label>\n" +
                    "            <input type=\"text\" id=\"username\" name=\"username\">\n" +
                    "            <label for=\"password\">Password:</label>\n" +
                    "            <input type=\"password\" id=\"password\" name=\"password\">\n" +
                    "            <div id=\"lower\">\n" +
                    "                <input type=\"submit\" value=\"Login\">\n" +
                    "            </div><!--/ lower-->\n" +
                    "            <input type='hidden' name='state' value='${state}'>" +
                    "        </form>\n" +
                    "    </div><!--/ container-->\n" +
                    "    <!-- End Page Content -->\n" +
                    "</body>\n" +
                    "</html>\n";


    public static String getACSURL(String state) {
        if (state == null || state.isEmpty()) {
            return "http://localhost:8290/travelocity/localauth";
        } else {
            return "http://localhost:8290/travelocity/localauth/?state=" + state;
        }
    }

}

