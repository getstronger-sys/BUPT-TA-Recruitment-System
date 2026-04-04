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
<div class="container">
    <h1>BUPT International School</h1>
    <h2>Teaching Assistant Recruitment System</h2>

    <% if (request.getParameter("registered") != null) { %>
    <p class="success">Registration successful! Please login.</p>
    <% } %>
    <% if (request.getParameter("error") != null) { %>
    <p class="error">Please login to continue.</p>
    <% } %>
    <% String err = (String) request.getAttribute("error"); if (err != null) { %>
    <p class="error"><%= err %></p>
    <% } %>

    <form action="${pageContext.request.contextPath}/login" method="post" class="form">
        <label>Username:</label>
        <input type="text" name="username" required>
        <label>Password:</label>
        <div class="password-field">
            <input type="password" name="password" required data-password-input>
            <button type="button" class="password-toggle" data-password-toggle aria-label="Show password" aria-pressed="false">Show</button>
        </div>
        <button type="submit">Login</button>
    </form>
    <p><a href="${pageContext.request.contextPath}/register.jsp">Register as TA or MO</a></p>
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
