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
<div class="container container--auth container--login">
    <header class="auth-topbar">
        <span class="brand">QM TA Portal</span>
    </header>
    <div class="auth-shell auth-shell--split">
        <section class="auth-hero" aria-label="Platform introduction">
            <p class="auth-hero-kicker">BUPT International School</p>
            <h1 class="auth-hero-title">Teaching Assistant Recruitment System</h1>
            <p class="auth-hero-copy">Sign in to continue your recruitment workflow. This portal supports Teaching Assistants, Module Organisers, and Admin staff with role-based access.</p>
            <ul class="auth-hero-points">
                <li><strong>Teaching Assistants:</strong> browse jobs, submit applications, and track status updates</li>
                <li><strong>Module Organisers:</strong> publish postings, manage candidates, and record decisions</li>
                <li><strong>Admin:</strong> monitor recruitment progress and workload metrics</li>
            </ul>
            <p class="auth-hero-copy">Use your assigned account credentials. If you are new to the system, create an account first.</p>
        </section>

        <section class="auth-card auth-card--login">
            <div class="auth-card-head auth-card-head--left">
                <h2 class="auth-title">Sign in</h2>
                <p class="auth-subtitle">Use your portal account to continue</p>
            </div>

            <% if (request.getParameter("registered") != null) { %>
            <p class="success">Registration successful. Please sign in.</p>
            <% } %>
            <% if (request.getParameter("error") != null) { %>
            <p class="error">Please sign in to continue.</p>
            <% } %>
            <% String err = (String) request.getAttribute("error"); if (err != null) { %>
            <p class="error"><%= err %></p>
            <% } %>

            <form action="${pageContext.request.contextPath}/login" method="post" class="form form--auth form--login">
                <label for="login-username">Username</label>
                <input id="login-username" type="text" name="username" required autocomplete="username" placeholder="Enter your username">
                <label for="login-password">Password</label>
                <input id="login-password" type="password" name="password" required autocomplete="current-password" placeholder="Enter your password">
                <button type="submit">Sign in</button>
            </form>

            <div class="auth-card-footer auth-card-footer--stacked">
                <p class="auth-helper">No account yet?</p>
                <a href="${pageContext.request.contextPath}/register.jsp">Register as TA or MO</a>
            </div>
        </section>
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
