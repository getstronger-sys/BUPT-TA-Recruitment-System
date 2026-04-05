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
        <input type="password" name="password" required minlength="4">
        <label>Confirm Password *</label>
        <input type="password" name="confirmPassword" required>
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
