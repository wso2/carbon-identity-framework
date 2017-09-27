package org.wso2.carbon.identity.event;


import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import java.io.*;
import java.util.Properties;



public class IdentityEventUtilsTest {

    private Properties properties;
    private IdentityEventUtils identityEventUtils;

    @BeforeMethod
    public void setUp() throws Exception {

        setProperties();
    }

    private void setProperties() {

        properties = new Properties();
        properties.setProperty("key", "replaced");

    }


    @Test
    public void testreadMessageTemplate() throws IOException {

        String identityEventUtils = IdentityEventUtils.readMessageTemplate("src/test/resources/sample-file.xml");
        String fileContent = "<file></file>\n";
        Assert.assertTrue(fileContent.equals(identityEventUtils));

    }

    @Test
    public  void testreplacePlaceHolder(){

        String replaceRegexStartsWit="start";
        String replaceRegexEndsWit="end";
        String key = "key";
        String finalContent = "replacedextra";
        String contentBefore = "startkeyendextra";
        String replacePlaceHolder = IdentityEventUtils.replacePlaceHolders(contentBefore,replaceRegexStartsWit,replaceRegexEndsWit,properties);
        Assert.assertEquals(finalContent,replacePlaceHolder);
    }

    @Test
    public void testgetPropertiesWithPrefix(){


    }
}



