<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
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
        <input type="password" name="password" required>
        <button type="submit">Login</button>
    </form>
    <p><a href="${pageContext.request.contextPath}/register.jsp">Register as TA or MO</a></p>
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
