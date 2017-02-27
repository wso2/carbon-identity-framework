<%@ page import="org.wso2.carbon.utils.xml.XMLPrettyPrinter" %><%@ page import="java.io.ByteArrayInputStream" %><%@ page import="java.io.InputStream" %><%

    String httpMethod = request.getMethod();
    if (!"post".equalsIgnoreCase(httpMethod)) {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        return;
    }

    String rawXML = request.getParameter("xmlString");
    rawXML = rawXML.replaceAll("\n|\\r|\\f|\\t", "");
    InputStream xmlIn = new ByteArrayInputStream(rawXML.getBytes());
    XMLPrettyPrinter xmlPrettyPrinter = new XMLPrettyPrinter(xmlIn);
    rawXML = xmlPrettyPrinter.xmlFormat();
    if (rawXML.startsWith("\n")) {
        rawXML = rawXML.substring(1);
    }


%><%=rawXML%>