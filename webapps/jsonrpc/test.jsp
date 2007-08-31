<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
 "http://www.w3.org/TR/html4/loose.dtd">
<%@
page contentType="text/html; charset=UTF-8" %><%@
page language="java" %><%@
page import="com.metaparadigm.jsonrpc.test.Test"
%><jsp:useBean id="JSONRPCBridge" scope="session"
     class="com.metaparadigm.jsonrpc.JSONRPCBridge"
/><jsp:useBean id="testObject" scope="session"
     class="com.metaparadigm.jsonrpc.test.Test"
/><%
   response.setDateHeader ("Expires", 0);
   //JSONRPCBridge.setDebug(true);
   JSONRPCBridge.registerObject("test", testObject);
   JSONRPCBridge.registerReference(Test.RefTest.class);
   JSONRPCBridge.registerCallableReference(Test.CallableRefTest.class);
%>
<html>
  <head>
    <link rel="stylesheet" type="text/css" href="css/site.css">
    <script type="text/javascript" src="jsonrpc.js"></script>
    <script type="text/javascript" src="test.js"></script>
    <title>JSON-RPC-Java Tests</title>
   </head>
   <body bgcolor="#ffffff" onLoad="onLoad()">

    <h1><img align="left" src="images/json.png" width="55" height="55" hspace="6" vspace="0" alt="JSON logo"/>JSON-RPC-Java</h1>
    <div class="tagline">JavaScript to Java remote communication.</div>
    <hr />
    <div class="menu"><a href="index.html">Home</a> | <a href="tutorial.html">Tutorial</a> | <a href="manual.html">Manual</a> | <a href="demos.html">Demos</a> | <a href="docs/">API Documentation</a> | <a href="http://oss.metaparadigm.com/mailman/listinfo/json-rpc-java">Mailing List</a> | <a href="CHANGES.txt">Changelog</a></div>

    <h2>JSON-RPC-Java Tests</h2>

    <table cellpadding="2" cellspacing="0" border="0">
      <tr>
        <td valign="top">Eval:</td>
        <td colspan="6">
          <input type="text" id="eval" size="80"
	       value="jsonrpc.test.echo({bang: 'foo', baz: 9})" />
        </td>
      </tr>
      <tr>
        <td valign="top">Result:</td>
        <td colspan="6">
          <textarea wrap="off" id="result" cols="80" rows="26"></textarea>
        </td>
      </tr>
      <tr>
        <td></td>
        <td><input type="button" value="Eval"
                         onclick="doEval()" /></td>
        <td><input type="button" value="List Methods"
                         onclick="doListMethods()" /></td>
	<td><input type="button" value="Basic Tests"
                         onclick="doBasicTests()" /></td>
	<td><input type="button" value="Reference Tests"
                         onclick="doReferenceTests()" /></td>
	<td><input type="button" value="Container Tests"
                         onclick="doContainerTests()" /></td>
	<td><input type="button" value="Exception Test"
                         onclick="doExceptionTest()" /></td>
      </tr>
    </table>

    <br>
    <hr>
    <table cellpadding="0" cellspacing="0" border="0" width="100%">
      <tr>
	<td><code>$Id: test.jsp,v 1.31 2005/02/13 01:26:47 mclark Exp $</code></td>
	<td><div class="copyright">Copyright 2005 <a href="http://www.metaparadigm.com/">Metaparadigm Pte Ltd</a></div></td>
      </tr>
    </table>
  </body>
</html>
