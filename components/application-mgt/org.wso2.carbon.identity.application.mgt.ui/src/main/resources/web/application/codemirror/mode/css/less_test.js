/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

(function() {
  "use strict";

  var mode = CodeMirror.getMode({indentUnit: 2}, "text/x-less");
  function MT(name) { test.mode(name, mode, Array.prototype.slice.call(arguments, 1), "less"); }

  MT("variable",
     "[variable-2 @base]: [atom #f04615];",
     "[qualifier .class] {",
     "  [property width]: [variable percentage]([number 0.5]); [comment // returns `50%`]",
     "  [property color]: [variable saturate]([variable-2 @base], [number 5%]);",
     "}");

  MT("amp",
     "[qualifier .child], [qualifier .sibling] {",
     "  [qualifier .parent] [atom &] {",
     "    [property color]: [keyword black];",
     "  }",
     "  [atom &] + [atom &] {",
     "    [property color]: [keyword red];",
     "  }",
     "}");

  MT("mixin",
     "[qualifier .mixin] ([variable dark]; [variable-2 @color]) {",
     "  [property color]: [atom darken]([variable-2 @color], [number 10%]);",
     "}",
     "[qualifier .mixin] ([variable light]; [variable-2 @color]) {",
     "  [property color]: [atom lighten]([variable-2 @color], [number 10%]);",
     "}",
     "[qualifier .mixin] ([variable-2 @_]; [variable-2 @color]) {",
     "  [property display]: [atom block];",
     "}",
     "[variable-2 @switch]: [variable light];",
     "[qualifier .class] {",
     "  [qualifier .mixin]([variable-2 @switch]; [atom #888]);",
     "}");

  MT("nest",
     "[qualifier .one] {",
     "  [def @media] ([property width]: [number 400px]) {",
     "    [property font-size]: [number 1.2em];",
     "    [def @media] [attribute print] [keyword and] [property color] {",
     "      [property color]: [keyword blue];",
     "    }",
     "  }",
     "}");


  MT("interpolation", ".@{[variable foo]} { [property font-weight]: [atom bold]; }");
})();
