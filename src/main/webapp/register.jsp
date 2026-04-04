<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jspf/html-esc.jspf" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Register - BUPT TA Recruitment</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="container">
    <h1>Register</h1>
    <% String err = (String) request.getAttribute("error"); if (err != null) { %>
    <p class="error"><%= err %></p>
    <% } %>
    <form action="${pageContext.request.contextPath}/register" method="post" class="form">
        <label>Username *</label>
        <input type="text" name="username" required value="<%= escHtml((String) request.getAttribute("username")) %>">
        <label>Password * (min 4 chars)</label>
        <div class="password-field">
            <input type="password" name="password" required minlength="4" data-password-input>
            <button type="button" class="password-toggle" data-password-toggle aria-label="Show password" aria-pressed="false">Show</button>
        </div>
        <label>Confirm Password *</label>
        <div class="password-field">
            <input type="password" name="confirmPassword" required data-password-input>
            <button type="button" class="password-toggle" data-password-toggle aria-label="Show password" aria-pressed="false">Show</button>
        </div>
        <label>Role *</label>
        <select name="role" required>
            <option value="TA" <%= !"MO".equals(request.getAttribute("role")) ? "selected" : "" %>>Teaching Assistant (TA)</option>
            <option value="MO" <%= "MO".equals(request.getAttribute("role")) ? "selected" : "" %>>Module Organiser (MO)</option>
        </select>
        <label>Student ID <span class="muted-inline">(required for TA applicants)</span></label>
        <input type="text" name="studentId" value="<%= escHtml((String) request.getAttribute("studentId")) %>" placeholder="e.g. 2023001234">
        <label>Email *</label>
        <input type="email" name="email" required value="<%= escHtml((String) request.getAttribute("email")) %>">
        <label>Real Name</label>
        <input type="text" name="realName" placeholder="Your name" value="<%= escHtml((String) request.getAttribute("realName")) %>">
        <button type="submit">Register</button>
    </form>
    <p><a href="${pageContext.request.contextPath}/index.jsp">Back to Login</a></p>
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
