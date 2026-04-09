<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%@ include file="/WEB-INF/jspf/html-esc.jspf" %>
<%
    String reqUri = (String) request.getAttribute("javax.servlet.error.request_uri");
    if (reqUri == null) {
        reqUri = "";
    }
    String ctx = request.getContextPath();
    if (ctx == null) {
        ctx = "";
    }
    // Logged-in users keep their session; send them to the role-aware dashboard entry.
    HttpSession sess = request.getSession(false);
    boolean loggedIn = sess != null && sess.getAttribute("user") != null;
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <%@ include file="/WEB-INF/jspf/viewport.jspf" %>
    <title>404 - Page not found | TA Recruitment</title>
    <link rel="stylesheet" href="<%= ctx %>/css/style.css">
</head>
<body>
<div class="container container--error-page">
    <header class="error-page-topbar">
        <% if (loggedIn) { %>
        <a class="brand brand-link" href="<%= ctx %>/dashboard.jsp">BUPT Teaching Assistant Recruitment System</a>
        <% } else { %>
        <a class="brand brand-link" href="<%= ctx %>/index.jsp">BUPT Teaching Assistant Recruitment System</a>
        <% } %>
    </header>
    <div class="error-page-body">
    <div class="error-page">
        <p class="error-page-code" aria-hidden="true">404</p>
        <h1>Page not found</h1>
        <p class="error-page-lead">The address does not exist or has been moved.</p>
        <% if (!reqUri.isEmpty()) { %>
        <p class="error-page-uri"><span class="muted-inline">Requested:</span> <code class="error-page-path"><%= escHtml(reqUri) %></code></p>
        <% } %>
        <div class="context-card error-page-tip">
            <strong>What you can do</strong>
            <% if (loggedIn) { %>
            <p>Return to your home dashboard and use the menu from there.</p>
            <% } else { %>
            <p>Sign in to use the portal, or go back to the login page.</p>
            <% } %>
        </div>
        <p class="error-page-actions">
            <% if (loggedIn) { %>
            <a class="btn btn-primary btn-lg" href="<%= ctx %>/dashboard.jsp">Back to home</a>
            <% } else { %>
            <a class="btn btn-primary btn-lg" href="<%= ctx %>/index.jsp">Login</a>
            <% } %>
        </p>
    </div>
    </div>
</div>
</body>
</html>
