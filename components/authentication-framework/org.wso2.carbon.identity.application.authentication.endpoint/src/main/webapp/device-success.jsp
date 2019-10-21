<%@ page import="org.wso2.identity.oauth2.device.response.DeviceFlowResponseTypeHandler" %>

<html>

<head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>WSO2 Identity Server</title>
    <link rel="icon" href="images/favicon.png" type="image/x-icon">
    <link href="libs/bootstrap_3.4.1/css/bootstrap.min.css" rel="stylesheet">
    <link href="css/Roboto.css" rel="stylesheet">
    <link href="css/custom-common.css" rel="stylesheet">
</head>

<style>
    
    #Para1{
        text-align: center;
        font-size: 30px;
    }
    #Para2{
        text-align: center;
        font-size: 30px;
    }
</style>

<body>
<header class="header header-default">
    <div class="container-fluid"><br></div>
    <div class="container-fluid">
        <div class="pull-left brand float-remove-xs text-center-xs">
            <a href="#">
                <img src="images/logo-inverse.svg" alt="WSO2" title="WSO2" class="logo">
                
                <h1><em>Identity Server</em></h1>
            </a>
        </div>
    </div>
</header>
<%--    <h1 id="DeviceHead">--%>
<%--        Device Flow--%>
<%--    </h1>--%>
<%--    <p id="Para1">--%>
<%--        Login Successful.--%>
<%--    </p>--%>
<%--    <p id="Para2">--%>
<%--        Return to device.--%>
<%--    </p>--%>
<div class="container-fluid body-wrapper">
    
    <div class="row">
        <div class="col-md-12">
            
            <!-- content -->
            <div class="container col-xs-10 col-sm-6 col-md-6 col-lg-4 col-centered wr-content wr-login col-centered">
                <div>
                    <h2 class="wr-title uppercase blue-bg padding-double white boarder-bottom-blue margin-none">
                        
                        SUCCESSFUL
                    
                    
                    </h2>
                </div>
                <div class="boarder-all ">
                    <div class="clearfix"></div>
                    <div class="padding-double login-form">
                        <script src="libs/jquery_3.4.1/jquery-3.4.1.js"></script></div>
                    <p id="Para1">
                        Login successful for
                        <%= request.getParameter("app_name")%>. Please close the browser and return to your device.
<%--                    </p>--%>
<%--                    <p id="Para2">--%>
<%--                        Return to your device.--%>
<%--                    </p>--%>
                    
                    <div class="clearfix"></div>
                    <div class="clearfix"></div>
                
                </div>
            </div>
            <!-- /content -->
        
        </div>
    </div>
    <!-- /content/body -->

</div>
<footer class="footer">
    <div class="container-fluid">
        <p>WSO2 Identity Server | ©
            2019
            <a href="http://wso2.com/" target="_blank"><i class="icon fw fw-wso2"></i>
                Inc
            </a>. All rights reserved
        </p>
    </div>
</footer>
</body>

</html>