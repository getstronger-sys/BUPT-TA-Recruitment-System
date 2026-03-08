<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="bupt.ta.model.TAProfile" %>
<%@ page import="java.util.List" %>
<% TAProfile profile = (TAProfile) request.getAttribute("profile"); if (profile == null) profile = new TAProfile(); %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>My Profile - TA Recruitment</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="container">
    <div class="nav">
        <a href="${pageContext.request.contextPath}/ta/jobs">Find Jobs</a>
        <a href="${pageContext.request.contextPath}/ta/applications">My Applications</a>
        <a href="${pageContext.request.contextPath}/ta/profile">My Profile</a>
        <span class="user"><%= session.getAttribute("realName") %> | <a href="${pageContext.request.contextPath}/logout">Logout</a></span>
    </div>
    <h1>My Profile</h1>
    <% if ("1".equals(request.getParameter("success"))) { %><p class="success">Profile saved.</p><% } %>
    <% if ("cv_success".equals(request.getParameter("cv_success")) || "1".equals(request.getParameter("cv_success"))) { %><p class="success">CV uploaded successfully.</p><% } %>
    <% if ("no_file".equals(request.getParameter("error"))) { %><p class="error">Please select a file to upload.</p><% } %>
    <% if ("invalid_type".equals(request.getParameter("error"))) { %><p class="error">Invalid file type. Use PDF, DOC, DOCX or TXT.</p><% } %>

    <form action="${pageContext.request.contextPath}/ta/profile" method="post" class="form">
        <label>Student ID</label>
        <input type="text" name="studentId" value="<%= profile.getStudentId() != null ? profile.getStudentId() : "" %>">
        <label>Phone</label>
        <input type="tel" name="phone" value="<%= profile.getPhone() != null ? profile.getPhone() : "" %>">
        <label>Skills (comma-separated, e.g. Java, Python, Teaching)</label>
        <input type="text" name="skills" value="<%= (profile.getSkills() != null && !profile.getSkills().isEmpty()) ? String.join(", ", profile.getSkills()) : "" %>">
        <label>Availability</label>
        <input type="text" name="availability" value="<%= profile.getAvailability() != null ? profile.getAvailability() : "" %>" placeholder="e.g. Mon/Wed/Fri 9-12">
        <label>Introduction</label>
        <textarea name="introduction"><%= profile.getIntroduction() != null ? profile.getIntroduction() : "" %></textarea>
        <button type="submit">Save Profile</button>
    </form>

    <h2>Upload CV</h2>
    <% if (profile.getCvFilePath() != null && !profile.getCvFilePath().isEmpty()) { %>
    <p class="success">Current CV: <%= profile.getCvFilePath() %></p>
    <% } %>
    <form action="${pageContext.request.contextPath}/ta/upload-cv" method="post" enctype="multipart/form-data" class="form">
        <label>Select file (PDF, DOC, DOCX, TXT, max 5MB)</label>
        <input type="file" name="cvFile" accept=".pdf,.doc,.docx,.txt">
        <button type="submit">Upload CV</button>
    </form>
</div>
</body>
</html>
