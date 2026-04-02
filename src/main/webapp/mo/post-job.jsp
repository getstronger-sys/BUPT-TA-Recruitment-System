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
    <div class="nav top-nav">
        <span class="brand">QM TA Portal</span>
        <span class="user"><%= session.getAttribute("realName") %> | <a href="${pageContext.request.contextPath}/logout">Logout</a></span>
    </div>
    <div class="page-layout">
        <div class="left-nav-wrap">
            <div class="icon-rail">
                <div class="icon-dot">J</div>
                <div class="icon-dot active">P</div>
                <div class="icon-dot">D</div>
            </div>
            <aside class="side-nav">
                <a href="${pageContext.request.contextPath}/mo/jobs">My Jobs</a>
                <a class="active" href="${pageContext.request.contextPath}/mo/post-job">Post Job</a>
            </aside>
        </div>
        <main class="main-panel">
    <h1>Post a New Job</h1>
    <div class="context-card">
        <strong>Posting Tip</strong>
        <p>Add required skills clearly to improve matching quality and reduce irrelevant applications.</p>
    </div>
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
        </main>
        <aside class="right-sidebar">
            <div class="widget-card">
                <div class="widget-title">Posting Checklist</div>
                <p class="widget-line">Clear title and module code</p>
                <p class="widget-line">Realistic required skills</p>
                <p class="widget-line">Reasonable max applicants</p>
            </div>
        </aside>
    </div>
</div>
</body>
</html>
