<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jspf/html-esc.jspf" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <%@ include file="/WEB-INF/jspf/viewport.jspf" %>
    <title>Register - BUPT TA Recruitment</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="container container--auth">
    <header class="auth-topbar">
        <span class="brand">BUPT Teaching Assistant Recruitment System</span>
    </header>
    <div class="auth-shell">
        <div class="auth-card auth-card--wide">
            <a class="auth-back" href="${pageContext.request.contextPath}/index.jsp">&larr; Back to sign in</a>
            <div class="auth-card-head">
                <h1 class="auth-title">Create account</h1>
                <p class="auth-subtitle">Register as a Teaching Assistant or Module Organiser</p>
            </div>

            <% String err = (String) request.getAttribute("error"); if (err != null) { %>
            <p class="error"><%= err %></p>
            <% } %>

            <form action="${pageContext.request.contextPath}/register" method="post" class="form form--auth">
                <label for="reg-username">Username *</label>
                <input id="reg-username" type="text" name="username" required autocomplete="username" value="<%= escHtml((String) request.getAttribute("username")) %>">
                <label for="reg-password">Password * (min 4 characters)</label>
                <input id="reg-password" type="password" name="password" required minlength="4" autocomplete="new-password">
                <label for="reg-confirm">Confirm password *</label>
                <input id="reg-confirm" type="password" name="confirmPassword" required autocomplete="new-password">
                <label for="reg-role">Role *</label>
                <select id="reg-role" name="role" required>
                    <option value="TA" <%= !"MO".equals(request.getAttribute("role")) ? "selected" : "" %>>Teaching Assistant (TA)</option>
                    <option value="MO" <%= "MO".equals(request.getAttribute("role")) ? "selected" : "" %>>Module Organiser (MO)</option>
                </select>
                <label for="reg-student-id">Student ID <span class="muted-inline">(required for TA applicants)</span></label>
                <input id="reg-student-id" type="text" name="studentId" value="<%= escHtml((String) request.getAttribute("studentId")) %>" placeholder="e.g. 2023001234" autocomplete="off">
                <label for="reg-email">Email *</label>
                <input id="reg-email" type="email" name="email" required autocomplete="email" value="<%= escHtml((String) request.getAttribute("email")) %>">
                <label for="reg-name">Real name</label>
                <input id="reg-name" type="text" name="realName" placeholder="Your name" autocomplete="name" value="<%= escHtml((String) request.getAttribute("realName")) %>">
                <button type="submit">Register</button>
            </form>
            <div class="auth-card-footer">
                <a href="${pageContext.request.contextPath}/index.jsp">Already have an account? Sign in</a>
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
