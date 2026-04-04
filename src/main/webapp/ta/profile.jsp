<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jspf/html-esc.jspf" %>
<%@ page import="bupt.ta.model.TAProfile" %>
<%@ page import="java.util.List" %>
<% TAProfile profile = (TAProfile) request.getAttribute("profile"); if (profile == null) profile = new TAProfile(); %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <%@ include file="/WEB-INF/jspf/viewport.jspf" %>
    <title>My Profile - TA Recruitment</title>
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
                <div class="icon-dot">F</div>
                <div class="icon-dot">A</div>
                <div class="icon-dot active">P</div>
            </div>
            <aside class="side-nav">
                <a href="${pageContext.request.contextPath}/ta/jobs">Find Jobs</a>
                <a href="${pageContext.request.contextPath}/ta/applications">My Applications</a>
                <a class="active" href="${pageContext.request.contextPath}/ta/profile">My Profile</a>
            </aside>
        </div>
        <main class="main-panel ta-main">
            <h1>My Profile</h1>
            <p class="ta-page-lead">Keep your skills and CV up to date so module organisers and the matching system can rank you fairly.</p>
            <div class="context-card">
                <strong>Profile Tip</strong>
                <p>Profiles with clear skills, availability and CV are easier for MO to review and shortlist.</p>
            </div>
            <% if ("1".equals(request.getParameter("success"))) { %><p class="success">Profile saved.</p><% } %>
            <% if ("cv_success".equals(request.getParameter("cv_success")) || "1".equals(request.getParameter("cv_success"))) { %><p class="success">CV uploaded successfully.</p><% } %>
            <% if ("no_file".equals(request.getParameter("error"))) { %><p class="error">Please select a file to upload.</p><% } %>
            <% if ("invalid_type".equals(request.getParameter("error"))) { %><p class="error">Invalid file type. Use PDF, DOC, DOCX or TXT.</p><% } %>

            <form action="${pageContext.request.contextPath}/ta/profile" method="post" class="form form--ta ta-profile-form">
                <label>Student ID</label>
                <input type="text" name="studentId" value="<%= escHtml(profile.getStudentId() != null ? profile.getStudentId() : "") %>">
                <label>Phone</label>
                <input type="tel" name="phone" value="<%= escHtml(profile.getPhone() != null ? profile.getPhone() : "") %>">
                <label>Degree</label>
                <input type="text" name="degree" value="<%= escHtml(profile.getDegree() != null ? profile.getDegree() : "") %>" placeholder="e.g. BSc, MSc">
                <label>Programme / major</label>
                <input type="text" name="programme" value="<%= escHtml(profile.getProgramme() != null ? profile.getProgramme() : "") %>" placeholder="e.g. Computer Science">
                <label>TA or teaching experience</label>
                <textarea name="taExperience" placeholder="Prior TA roles, tutoring, labs, etc."><%= escHtml(profile.getTaExperience() != null ? profile.getTaExperience() : "") %></textarea>
                <label>Skills (comma-separated, e.g. Java, Python, Teaching)</label>
                <input type="text" name="skills" value="<%= escHtml((profile.getSkills() != null && !profile.getSkills().isEmpty()) ? String.join(", ", profile.getSkills()) : "") %>">
                <label>Availability</label>
                <input type="text" name="availability" value="<%= escHtml(profile.getAvailability() != null ? profile.getAvailability() : "") %>" placeholder="e.g. Mon/Wed/Fri 9-12">
                <label>Introduction</label>
                <textarea name="introduction"><%= escHtml(profile.getIntroduction() != null ? profile.getIntroduction() : "") %></textarea>
                <button type="submit">Save Profile</button>
            </form>

            <section class="ta-cv-section" aria-labelledby="ta-cv-heading">
            <h2 id="ta-cv-heading">Upload CV</h2>
            <div class="ta-cv-panel">
            <% if (profile.getCvFilePath() != null && !profile.getCvFilePath().isEmpty()) { %>
            <p class="success ta-cv-status">Current CV on file.
                <a href="${pageContext.request.contextPath}/view-cv?userId=<%= profile.getUserId() %>" target="_blank" rel="noopener">View</a>
                <span class="muted-inline"> | </span>
                <a href="${pageContext.request.contextPath}/view-cv?userId=<%= profile.getUserId() %>&amp;download=1">Download</a>
            </p>
            <% } %>
            <form action="${pageContext.request.contextPath}/ta/upload-cv" method="post" enctype="multipart/form-data" class="form form--ta form--ta-cv">
                <label>Select file (PDF, DOC, DOCX, TXT, max 5MB)</label>
                <input type="file" name="cvFile" accept=".pdf,.doc,.docx,.txt">
                <button type="submit">Upload CV</button>
            </form>
            </div>
            </section>
        </main>
        <aside class="right-sidebar">
            <div class="widget-card">
                <div class="widget-title">Profile Health</div>
                <p class="widget-line">Keep phone and availability updated.</p>
                <p class="widget-line">Add detailed skills for better AI ranking.</p>
            </div>
            <div class="widget-card">
                <div class="widget-title">CV Reminder</div>
                <p class="widget-line">Accepted: PDF, DOC, DOCX, TXT</p>
                <p class="widget-line">Max upload size: 5MB</p>
            </div>
        </aside>
    </div>
</div>
</body>
</html>
