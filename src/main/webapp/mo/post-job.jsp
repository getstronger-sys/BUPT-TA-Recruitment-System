<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Post Job - MO</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="container">
    <div class="nav">
        <a href="${pageContext.request.contextPath}/mo/jobs">My Jobs</a>
        <a href="${pageContext.request.contextPath}/mo/post-job">Post Job</a>
        <span class="user"><%= session.getAttribute("realName") %> | <a href="${pageContext.request.contextPath}/logout">Logout</a></span>
    </div>
    <h1>Post a New Job</h1>
    <% String err = (String) request.getAttribute("error"); if (err != null) { %>
    <p class="error"><%= err %></p>
    <% } %>
    <form action="${pageContext.request.contextPath}/mo/post-job" method="post" class="form">
        <label>Job Title *</label>
        <input type="text" name="title" required placeholder="e.g. TA for Software Engineering">
        <label>Module Code *</label>
        <input type="text" name="moduleCode" required placeholder="e.g. EBU6304">
        <label>Module Name</label>
        <input type="text" name="moduleName" placeholder="e.g. Software Engineering">
        <label>Job Type</label>
        <select name="jobType">
            <option value="MODULE_TA">Module TA</option>
            <option value="INVIGILATION">Invigilation</option>
            <option value="OTHER">Other</option>
        </select>
        <label>Description</label>
        <textarea name="description" placeholder="Job description..."></textarea>
        <label>Required Skills (comma-separated)</label>
        <input type="text" name="skills" placeholder="e.g. Java, Python, Teaching">
        <label>Max Applicants (0 = unlimited)</label>
        <input type="number" name="maxApplicants" value="0" min="0">
        <button type="submit">Post Job</button>
    </form>
</div>
</body>
</html>
