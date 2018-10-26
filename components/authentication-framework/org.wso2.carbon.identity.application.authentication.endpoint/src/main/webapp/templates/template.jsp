
<div id="template-holder"></div>
<!-- customized template will be added here -->


<script id="sample-template" type="text/x-handlebars-template">

    <!-- You can customize the user prompt template here... -->

    <div class="margin-none wr-login">
        <div class="font-large info text-left padding-top-double" >
            <strong>{{promptLabel}}</strong>
        </div>
    </div>

    <div class="boarder-all ">
        <div class="clearfix"></div>
        <div class="padding-double login-form">

            <form action="../commonauth" method="POST"> <!-- DO NOT CHANGE -->
                <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group required">

                    <!-- Add the required input field/s here...
                    It should follow the below mentioned format-->

                    <label for="sampleInput" class="control-label"/>sample input</label>
                    <input type="text" id="sampleInput" name="sample_input" class="form-control" placeholder="input_placeholder" />

                </div>

                <input type="hidden" id="promptResp" name="promptResp" value="true"> <!-- DO NOT CHANGE -->
                <input type="hidden" id="promptId" name="promptId"> <!-- DO NOT CHANGE -->


                <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group required">
                    <input type="submit" class="wr-btn grey-bg col-xs-12 col-md-12 col-lg-12 uppercase font-extra-large" value="Submit">
                </div>
            </form>
            <div class="clearfix"></div>
        </div>
    </div>
    <br/>

</script>

<script type="text/javascript">

    document.getElementById("promptId").value= promptID; //DO NOT CHANGE

    var sampleInfo = document.getElementById("sample-template").innerHTML;
    var template = Handlebars.compile(sampleInfo);

    var quoteData = template({
        //add your data for the template script here...

        promptLabel: "This is a sample prompt label"
    });

    document.getElementById("template-holder").innerHTML += quoteData;

</script>

