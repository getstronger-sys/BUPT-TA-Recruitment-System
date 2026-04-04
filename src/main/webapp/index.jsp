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
        <span class="brand">QM TA Portal</span>
    </header>
    <div class="auth-shell">
        <div class="auth-card">
            <div class="auth-card-head">
                <h1 class="auth-title">BUPT International School</h1>
                <p class="auth-subtitle">Teaching Assistant Recruitment System</p>
            </div>
            <p class="auth-lead">Sign in with your username and password.</p>

            <% if (request.getParameter("registered") != null) { %>
            <p class="success">Registration successful! Please login.</p>
            <% } %>
            <% if (request.getParameter("error") != null) { %>
            <p class="error">Please login to continue.</p>
            <% } %>
            <% String err = (String) request.getAttribute("error"); if (err != null) { %>
            <p class="error"><%= err %></p>
            <% } %>

            <form action="${pageContext.request.contextPath}/login" method="post" class="form form--auth">
                <label for="login-username">Username</label>
                <input id="login-username" type="text" name="username" required autocomplete="username">
                <label for="login-password">Password</label>
                <div class="password-field">
                    <input id="login-password" type="password" name="password" required autocomplete="current-password" data-password-input>
                    <button type="button" class="password-toggle" data-password-toggle aria-label="Show password" aria-pressed="false">Show</button>
                </div>
                <button type="submit">Sign in</button>
            </form>
            <div class="auth-card-footer">
                <a href="${pageContext.request.contextPath}/register.jsp">Create an account (TA or MO)</a>
            </div>
        </div>
    </div>
</div>
<script>
document.addEventListener("DOMContentLoaded", function () {
    document.querySelectorAll("[data-password-toggle]").forEach(function (button) {
        button.addEventListener("click", function () {
            var wrapper = button.closest(".password-field");
            var input = wrapper ? wrapper.querySelector("[data-password-input]") : null;
            if (!input) return;
            var show = input.type === "password";
            input.type = show ? "text" : "password";
            button.textContent = show ? "Hide" : "Show";
            button.setAttribute("aria-label", show ? "Hide password" : "Show password");
            button.setAttribute("aria-pressed", show ? "true" : "false");
        });
    });
});
</script>
</body>
</html>
