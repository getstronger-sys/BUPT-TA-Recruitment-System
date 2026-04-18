<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    if (session == null || session.getAttribute("user") == null) {
        response.sendRedirect(request.getContextPath() + "/index.jsp?error=login");
        return;
    }
    if ("forbidden".equals(request.getParameter("error"))) { %>
<!DOCTYPE html>
<html>
<head><meta charset="UTF-8"><%@ include file="/WEB-INF/jspf/viewport.jspf" %><title>Access Denied</title><link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css"></head>
<body><div class="container"><p class="error">Access denied. You do not have permission for this page.</p>
<form action="<%= request.getContextPath() %>/logout" method="post" class="inline-form logout-form">
<%@ include file="/WEB-INF/jspf/csrf-hidden.jspf" %>
<button type="submit" class="logout-button">Logout</button>
</form></div></body></html>
<% return; }
    String role = (String) session.getAttribute("role");
    if (role == null) role = "";
    String ctx = request.getContextPath();

    if ("TA".equals(role)) {
        response.sendRedirect(ctx + "/ta/dashboard");
    } else if ("MO".equals(role)) {
        response.sendRedirect(ctx + "/mo/jobs");
    } else if ("ADMIN".equals(role)) {
        response.sendRedirect(ctx + "/admin/dashboard");
    } else {
        response.sendRedirect(ctx + "/index.jsp?error=unknown");
    }
%>
