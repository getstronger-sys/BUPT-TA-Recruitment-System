<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <%@ include file="/WEB-INF/jspf/viewport.jspf" %>
    <title>BUPT TA Recruitment - Login</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="container container--auth">
    <header class="auth-topbar">
        <span class="brand">BUPT Teaching Assistant Recruitment System</span>
    </header>
    <div class="auth-shell">
        <div class="auth-card">
            <div class="auth-card-head">
                <h1 class="auth-title">Welcome back</h1>
                <p class="auth-subtitle">Sign in to the Teaching Assistant Recruitment System</p>
            </div>

            <% if (request.getParameter("registered") != null) { %>
            <p class="success">Registration successful. Please sign in.</p>
            <% } %>
            <% if (request.getParameter("error") != null) { %>
            <p class="error">Please login to continue.</p>
            <% } %>
            <% String err = (String) request.getAttribute("error"); if (err != null) { %>
            <p class="error"><%= err %></p>
            <% } %>

            <form action="${pageContext.request.contextPath}/login" method="post" class="form form--auth">
                <label for="login-username">Username</label>
                <input id="login-username" type="text" name="username" required autocomplete="username" autofocus>
                <label for="login-password">Password</label>
                <input id="login-password" type="password" name="password" required autocomplete="current-password">
                <button type="submit">Sign in</button>
            </form>

            <div class="auth-card-footer">
                <a href="${pageContext.request.contextPath}/register.jsp">New here? Register as TA or MO</a>
            </div>
        </div>
    </div>
</div>
</body>
</html>
