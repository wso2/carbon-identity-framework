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
            "<!DOCTYPE html>\n" +
                    "<html >\n" +
                    "<head>\n" +
                    "  <meta charset=\"UTF-8\">\n" +
                    "  <title>Login Form</title>\n" +
                    "  \n" +
                    "  <link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/normalize/5.0.0/normalize.min.css\">\n" +
                    "\n" +
                    "  \n" +
                    "      <style>\n" +
                    "      /* NOTE: The styles were added inline because Prefixfree needs access to your styles and they must be inlined if they are on local disk! */\n" +
                    "      @import url(http://fonts.googleapis.com/css?family=Open+Sans);\n" +
                    ".btn { display: inline-block; *display: inline; *zoom: 1; padding: 4px 10px 4px; margin-bottom: 0; font-size: 13px; line-height: 18px; color: #333333; text-align: center;text-shadow: 0 1px 1px rgba(255, 255, 255, 0.75); vertical-align: middle; background-color: #f5f5f5; background-image: -moz-linear-gradient(top, #ffffff, #e6e6e6); background-image: -ms-linear-gradient(top, #ffffff, #e6e6e6); background-image: -webkit-gradient(linear, 0 0, 0 100%, from(#ffffff), to(#e6e6e6)); background-image: -webkit-linear-gradient(top, #ffffff, #e6e6e6); background-image: -o-linear-gradient(top, #ffffff, #e6e6e6); background-image: linear-gradient(top, #ffffff, #e6e6e6); background-repeat: repeat-x; filter: progid:dximagetransform.microsoft.gradient(startColorstr=#ffffff, endColorstr=#e6e6e6, GradientType=0); border-color: #e6e6e6 #e6e6e6 #e6e6e6; border-color: rgba(0, 0, 0, 0.1) rgba(0, 0, 0, 0.1) rgba(0, 0, 0, 0.25); border: 1px solid #e6e6e6; -webkit-border-radius: 4px; -moz-border-radius: 4px; border-radius: 4px; -webkit-box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.2), 0 1px 2px rgba(0, 0, 0, 0.05); -moz-box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.2), 0 1px 2px rgba(0, 0, 0, 0.05); box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.2), 0 1px 2px rgba(0, 0, 0, 0.05); cursor: pointer; *margin-left: .3em; }\n" +
                    ".btn:hover, .btn:active, .btn.active, .btn.disabled, .btn[disabled] { background-color: #e6e6e6; }\n" +
                    ".btn-large { padding: 9px 14px; font-size: 15px; line-height: normal; -webkit-border-radius: 5px; -moz-border-radius: 5px; border-radius: 5px; }\n" +
                    ".btn:hover { color: #333333; text-decoration: none; background-color: #e6e6e6; background-position: 0 -15px; -webkit-transition: background-position 0.1s linear; -moz-transition: background-position 0.1s linear; -ms-transition: background-position 0.1s linear; -o-transition: background-position 0.1s linear; transition: background-position 0.1s linear; }\n" +
                    ".btn-primary, .btn-primary:hover { text-shadow: 0 -1px 0 rgba(0, 0, 0, 0.25); color: #ffffff; }\n" +
                    ".btn-primary.active { color: rgba(255, 255, 255, 0.75); }\n" +
                    ".btn-primary { background-color: #4a77d4; background-image: -moz-linear-gradient(top, #6eb6de, #4a77d4); background-image: -ms-linear-gradient(top, #6eb6de, #4a77d4); background-image: -webkit-gradient(linear, 0 0, 0 100%, from(#6eb6de), to(#4a77d4)); background-image: -webkit-linear-gradient(top, #6eb6de, #4a77d4); background-image: -o-linear-gradient(top, #6eb6de, #4a77d4); background-image: linear-gradient(top, #6eb6de, #4a77d4); background-repeat: repeat-x; filter: progid:dximagetransform.microsoft.gradient(startColorstr=#6eb6de, endColorstr=#4a77d4, GradientType=0);  border: 1px solid #3762bc; text-shadow: 1px 1px 1px rgba(0,0,0,0.4); box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.2), 0 1px 2px rgba(0, 0, 0, 0.5); }\n" +
                    ".btn-primary:hover, .btn-primary:active, .btn-primary.active, .btn-primary.disabled, .btn-primary[disabled] { filter: none; background-color: #4a77d4; }\n" +
                    ".btn-block { width: 100%; display:block; }\n" +
                    "\n" +
                    "* { -webkit-box-sizing:border-box; -moz-box-sizing:border-box; -ms-box-sizing:border-box; -o-box-sizing:border-box; box-sizing:border-box; }\n" +
                    "\n" +
                    "html { width: 100%; height:100%; overflow:hidden; }\n" +
                    "\n" +
                    "body { \n" +
                    "\twidth: 100%;\n" +
                    "\theight:100%;\n" +
                    "\tfont-family: 'Open Sans', sans-serif;\n" +
                    "\tbackground: #092756;\n" +
                    "\tbackground: -moz-radial-gradient(0% 100%, ellipse cover, rgba(104,128,138,.4) 10%,rgba(138,114,76,0) 40%),-moz-linear-gradient(top,  rgba(57,173,219,.25) 0%, rgba(42,60,87,.4) 100%), -moz-linear-gradient(-45deg,  #670d10 0%, #092756 100%);\n" +
                    "\tbackground: -webkit-radial-gradient(0% 100%, ellipse cover, rgba(104,128,138,.4) 10%,rgba(138,114,76,0) 40%), -webkit-linear-gradient(top,  rgba(57,173,219,.25) 0%,rgba(42,60,87,.4) 100%), -webkit-linear-gradient(-45deg,  #670d10 0%,#092756 100%);\n" +
                    "\tbackground: -o-radial-gradient(0% 100%, ellipse cover, rgba(104,128,138,.4) 10%,rgba(138,114,76,0) 40%), -o-linear-gradient(top,  rgba(57,173,219,.25) 0%,rgba(42,60,87,.4) 100%), -o-linear-gradient(-45deg,  #670d10 0%,#092756 100%);\n" +
                    "\tbackground: -ms-radial-gradient(0% 100%, ellipse cover, rgba(104,128,138,.4) 10%,rgba(138,114,76,0) 40%), -ms-linear-gradient(top,  rgba(57,173,219,.25) 0%,rgba(42,60,87,.4) 100%), -ms-linear-gradient(-45deg,  #670d10 0%,#092756 100%);\n" +
                    "\tbackground: -webkit-radial-gradient(0% 100%, ellipse cover, rgba(104,128,138,.4) 10%,rgba(138,114,76,0) 40%), linear-gradient(to bottom,  rgba(57,173,219,.25) 0%,rgba(42,60,87,.4) 100%), linear-gradient(135deg,  #670d10 0%,#092756 100%);\n" +
                    "\tfilter: progid:DXImageTransform.Microsoft.gradient( startColorstr='#3E1D6D', endColorstr='#092756',GradientType=1 );\n" +
                    "}\n" +
                    ".login { \n" +
                    "\tposition: absolute;\n" +
                    "\ttop: 50%;\n" +
                    "\tleft: 50%;\n" +
                    "\tmargin: -150px 0 0 -150px;\n" +
                    "\twidth:300px;\n" +
                    "\theight:300px;\n" +
                    "}\n" +
                    ".login h1 { color: #fff; text-shadow: 0 0 10px rgba(0,0,0,0.3); letter-spacing:1px; text-align:center; }\n" +
                    "\n" +
                    "input { \n" +
                    "\twidth: 100%; \n" +
                    "\tmargin-bottom: 10px; \n" +
                    "\tbackground: rgba(0,0,0,0.3);\n" +
                    "\tborder: none;\n" +
                    "\toutline: none;\n" +
                    "\tpadding: 10px;\n" +
                    "\tfont-size: 13px;\n" +
                    "\tcolor: #fff;\n" +
                    "\ttext-shadow: 1px 1px 1px rgba(0,0,0,0.3);\n" +
                    "\tborder: 1px solid rgba(0,0,0,0.3);\n" +
                    "\tborder-radius: 4px;\n" +
                    "\tbox-shadow: inset 0 -5px 45px rgba(100,100,100,0.2), 0 1px 1px rgba(255,255,255,0.2);\n" +
                    "\t-webkit-transition: box-shadow .5s ease;\n" +
                    "\t-moz-transition: box-shadow .5s ease;\n" +
                    "\t-o-transition: box-shadow .5s ease;\n" +
                    "\t-ms-transition: box-shadow .5s ease;\n" +
                    "\ttransition: box-shadow .5s ease;\n" +
                    "}\n" +
                    "input:focus { box-shadow: inset 0 -5px 45px rgba(100,100,100,0.4), 0 1px 1px rgba(255,255,255,0.2); }\n" +
                    "\n" +
                    "    </style>\n" +
                    "\n" +
                    "  <script src=\"https://cdnjs.cloudflare.com/ajax/libs/prefixfree/1.0.7/prefixfree.min.js\"></script>\n" +
                    "\n" +
                    "</head>\n" +
                    "\n" +
                    "<body>\n" +
                    "  <div class=\"login\">\n" +
                    "\t<h1>Login</h1>\n" +
                    "    <form method=\"post\" action=\"${callback}\">\n" +
                    "    \t<input type=\"text\" name=\"u\" placeholder=\"Username\" required=\"required\" />\n" +
                    "        <input type=\"password\" name=\"p\" placeholder=\"Password\" required=\"required\" />\n" +
                    "      \t  <input type='hidden' name='state' value='${state}'>\n" +
                    "        <button type=\"submit\" class=\"btn btn-primary btn-block btn-large\">Let me in.</button>\n" +
                    "    </form>\n" +
                    "</div>\n" +
                    "  \n" +
                    "    <script src=\"js/index.js\"></script>\n" +
                    "\n" +
                    "</body>\n" +
                    "</html>";


    public static String getACSURL(String state) {
        if (state == null || state.isEmpty()) {
            return "http://localhost:8290/travelocity/localauth";
        } else {
            return "http://localhost:8290/travelocity/localauth/?state=" + state;
        }
    }

}

