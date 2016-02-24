<%@ page import="org.wso2.carbon.utils.xml.XMLPrettyPrinter" %><%@ page import="java.io.ByteArrayInputStream" %><%@ page import="java.io.InputStream" %><%

    String rawXML = request.getParameter("xmlString");
    rawXML = rawXML.replaceAll("\n|\\r|\\f", "");
    //Tabs should not be replaced with empty strings. They should be replaced
    //with a space
    rawXML = rawXML.replaceAll("\\t+", " ");
    InputStream xmlIn = new ByteArrayInputStream(rawXML.getBytes());
    XMLPrettyPrinter xmlPrettyPrinter = new XMLPrettyPrinter(xmlIn);
    rawXML = xmlPrettyPrinter.xmlFormat();
    if (rawXML.startsWith("\n")) {
        rawXML = rawXML.substring(1);
    }


%><%=rawXML%>