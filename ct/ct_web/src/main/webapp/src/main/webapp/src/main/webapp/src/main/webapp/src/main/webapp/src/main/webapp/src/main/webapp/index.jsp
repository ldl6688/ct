<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page language="java" contentType="text/html; charset=utf-8"
         pageEncoding="utf-8" %>
<%
    String path = request.getContextPath();
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <title>Show Time</title>
</head>
<body>
<form action="/queryContact">
    id:<input type="text" name="id"/>
    telephone:<input type="text" name="telephone"/>
    <input type="submit" value="查询联系人"/>
</form>
<form action="/queryContactList">
    <input type="submit" value="查询所有"/>
</form>
<br/>

<form action='<c:url value="/queryCallLogList2"/>' method="post">
    telephone:<input type="text" name="telephone"/>
    year:<input type="text" name="year"/>
    month:<input type="text" name="month"/>
    day:<input type="text" name="day"/>
    <input type="submit" value="查询该人通话记录"/>
</form>
</body>
</html>
