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
                <input id="login-password" type="password" name="password" required autocomplete="current-password">
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
    var messages = {
        valueMissing: "Please fill out this field.",
        typeMismatch: "Please enter a valid value.",
        patternMismatch: "Please match the requested format.",
        tooShort: "Please lengthen this text.",
        tooLong: "Please shorten this text.",
        rangeUnderflow: "Value is too small.",
        rangeOverflow: "Value is too large.",
        stepMismatch: "Please enter a valid value.",
        badInput: "Please enter a valid value."
    };

    document.querySelectorAll("form").forEach(function (form) {
        var controls = form.querySelectorAll("input, textarea, select");
        controls.forEach(function (control) {
            control.addEventListener("invalid", function () {
                control.setCustomValidity("");
                for (var key in messages) {
                    if (control.validity[key]) {
                        control.setCustomValidity(messages[key]);
                        break;
                    }
                }
            });
            control.addEventListener("input", function () { control.setCustomValidity(""); });
            control.addEventListener("change", function () { control.setCustomValidity(""); });
        });
    });
});
</script>
</body>
</html>
