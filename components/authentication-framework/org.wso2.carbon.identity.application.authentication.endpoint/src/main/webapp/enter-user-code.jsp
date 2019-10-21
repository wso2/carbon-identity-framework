<!DOCTYPE html>
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

<script type="text/javascript">

    var userCode;

    function loadFunc() {

        const urlParams = new URLSearchParams(window.location.search);
        userCode = urlParams.get('user_code');
        document.getElementById("usercode").value = userCode;
    }
</script>

<body onload="loadFunc()">

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

<div class="container-fluid body-wrapper">
    
    <div class="row">
        <div class="col-md-12">
            
            <!-- content -->
            <div class="container col-xs-10 col-sm-6 col-md-6 col-lg-4 col-centered wr-content wr-login col-centered">
                <div>
                    <h2 class="wr-title uppercase blue-bg padding-double white boarder-bottom-blue margin-none">
                        
                        Sign In
                    
                    </h2>
                </div>
                <div class="boarder-all ">
                    <div class="clearfix"></div>
                    <div class="padding-double login-form">
                        <form action="../oauth2/device" method="get">
                            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group">
                                <label for="usercode">User Code</label>
                                <input id="usercode" name="user_code" type="text" class="form-control" tabindex="0"
                                       placeholder="" required="">
                            </div>
                            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group">
                                <button class="wr-btn grey-bg col-xs-12 col-md-12 col-lg-12 uppercase
                                            font-extra-large margin-bottom-double" type="submit" value="Submit">
                                    Sign In
                                </button>
                            </div>
                            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group">
                                <p>
                                    Check your user code before sign in.
                                </p>
                            </div>
                        </form>
                        <div class="clearfix"></div>
                        
                        <div class="clearfix"></div>
                    </div>
                </div>
            </div>
        </div>
    </div>
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