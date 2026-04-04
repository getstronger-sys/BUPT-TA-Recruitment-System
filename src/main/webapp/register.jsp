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
        <span class="brand">QM TA Portal</span>
    </header>
    <div class="auth-shell">
        <div class="auth-card auth-card--wide">
            <a class="auth-back" href="${pageContext.request.contextPath}/index.jsp">← Back to sign in</a>
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
                <div class="password-field">
                    <input id="reg-password" type="password" name="password" required minlength="4" autocomplete="new-password" data-password-input>
                    <button type="button" class="password-toggle" data-password-toggle aria-label="Show password" aria-pressed="false">Show</button>
                </div>
                <label for="reg-confirm">Confirm password *</label>
                <div class="password-field">
                    <input id="reg-confirm" type="password" name="confirmPassword" required autocomplete="new-password" data-password-input>
                    <button type="button" class="password-toggle" data-password-toggle aria-label="Show password" aria-pressed="false">Show</button>
                </div>
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
