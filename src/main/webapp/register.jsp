<%@ page contentType="text/html;charset=UTF-8" language="java" %>
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
        <input type="text" name="username" required>
        <label>Password * (min 4 chars)</label>
        <input type="password" name="password" required minlength="4">
        <label>Confirm Password *</label>
        <input type="password" name="confirmPassword" required>
        <label>Role *</label>
        <select name="role" required>
            <option value="TA">Teaching Assistant (TA)</option>
            <option value="MO">Module Organiser (MO)</option>
        </select>
        <label>Email</label>
        <input type="email" name="email">
        <label>Real Name</label>
        <input type="text" name="realName" placeholder="Your name">
        <button type="submit">Register</button>
    </form>
    <p><a href="${pageContext.request.contextPath}/index.jsp">Back to Login</a></p>
</div>
</body>
</html>
