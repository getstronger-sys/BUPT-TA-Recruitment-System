<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    if (session == null || session.getAttribute("user") == null) {
        response.sendRedirect(request.getContextPath() + "/index.jsp?error=login");
        return;
    }
    if ("forbidden".equals(request.getParameter("error"))) { %>
<!DOCTYPE html>
<html>
<head><title>Access Denied</title><link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css"></head>
<body><div class="container"><p class="error">Access denied. You do not have permission for this page.</p>
<a href="<%= request.getContextPath() %>/logout">Logout</a></div></body></html>
<% return; }
    String role = (String) session.getAttribute("role");
    if (role == null) role = "";
    String ctx = request.getContextPath();

    if ("TA".equals(role)) {
        response.sendRedirect(ctx + "/ta/jobs");
    } else if ("MO".equals(role)) {
        response.sendRedirect(ctx + "/mo/jobs");
    } else if ("ADMIN".equals(role)) {
        response.sendRedirect(ctx + "/admin/workload");
    } else {
        response.sendRedirect(ctx + "/index.jsp?error=unknown");
    }
%>
