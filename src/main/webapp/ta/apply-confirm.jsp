<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jspf/html-esc.jspf" %>
<%@ page import="bupt.ta.model.Job" %>
<%@ page import="bupt.ta.model.TAProfile" %>
<%
    Job job = (Job) request.getAttribute("job");
    TAProfile profile = (TAProfile) request.getAttribute("profile");
    String profileEditUrl = (String) request.getAttribute("profileEditUrl");
    if (job == null) {
        response.sendRedirect(request.getContextPath() + "/ta/jobs?error=job_not_found");
        return;
    }
    if (profile == null) profile = new TAProfile();
    String uid = (String) session.getAttribute("userId");
    boolean hasCv = profile.getCvFilePath() != null && !profile.getCvFilePath().trim().isEmpty();
    if (profileEditUrl == null) profileEditUrl = request.getContextPath() + "/ta/profile";
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Confirm application - TA</title>
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
                <div class="icon-dot">H</div>
                <div class="icon-dot active">F</div>
                <div class="icon-dot">A</div>
            </div>
            <aside class="side-nav">
                <a href="${pageContext.request.contextPath}/ta/dashboard">Home</a>
                <a class="active" href="${pageContext.request.contextPath}/ta/jobs">Find Jobs</a>
                <a href="${pageContext.request.contextPath}/ta/applications">My Applications</a>
                <a href="${pageContext.request.contextPath}/ta/profile">My Profile</a>
            </aside>
        </div>
        <main class="main-panel">
            <p class="breadcrumb-line"><a href="${pageContext.request.contextPath}/ta/job?jobId=<%= escHtml(job.getId()) %>">&larr; Back to job details</a></p>
            <h1>Confirm your application</h1>
            <div class="context-card">
                <strong>What happens next</strong>
                <p>Your application will be sent to the module organiser. They will review your <strong>current saved profile and CV</strong> (not a frozen copy). You can update them anytime before a decision.</p>
            </div>

            <div class="detail-card">
                <h3>Job</h3>
                <p><strong><%= escHtml(job.getTitle() != null ? job.getTitle() : "") %></strong></p>
                <p>Module: <%= escHtml(job.getModuleCode() != null ? job.getModuleCode() : "-") %>
                    <% if (job.getModuleName() != null && !job.getModuleName().isEmpty()) { %> | <%= escHtml(job.getModuleName()) %><% } %></p>
            </div>

            <div class="detail-card">
                <h3>What will be shared from your profile</h3>
                <p><strong>Student ID:</strong> <% if (profile.getStudentId() != null && !profile.getStudentId().isEmpty()) { %><%= escHtml(profile.getStudentId()) %><% } else { %><span class="muted-inline">Not set</span><% } %></p>
                <p><strong>Degree:</strong> <%= profile.getDegree() != null && !profile.getDegree().isEmpty() ? escHtml(profile.getDegree()) : "-" %></p>
                <p><strong>Programme:</strong> <%= profile.getProgramme() != null && !profile.getProgramme().isEmpty() ? escHtml(profile.getProgramme()) : "-" %></p>
                <p><strong>Skills:</strong> <% if (profile.getSkills() != null && !profile.getSkills().isEmpty()) { %><%= escHtml(String.join(", ", profile.getSkills())) %><% } else { %><span class="muted-inline">Not set</span><% } %></p>
                <p><strong>TA experience:</strong> <%= profile.getTaExperience() != null && !profile.getTaExperience().isEmpty() ? escHtml(profile.getTaExperience()) : "-" %></p>
                <p><strong>Availability:</strong> <%= profile.getAvailability() != null && !profile.getAvailability().isEmpty() ? escHtml(profile.getAvailability()) : "-" %></p>
                <div class="detail-block-text">
                    <strong>Introduction</strong>
                    <p class="pre-wrap"><%= profile.getIntroduction() != null && !profile.getIntroduction().isEmpty() ? escHtml(profile.getIntroduction()) : "Not provided." %></p>
                </div>
                <p><strong>CV:</strong>
                    <% if (hasCv && uid != null) { %>
                    <a href="${pageContext.request.contextPath}/view-cv?userId=<%= escHtml(uid) %>" target="_blank" rel="noopener">Preview</a>
                    <% } else { %>
                    <span class="muted-inline">Not uploaded</span>
                    <% } %>
                </p>
            </div>

            <div class="apply-confirm-actions">
                <a href="<%= escHtml(profileEditUrl) %>" class="btn btn-primary">Edit profile first</a>
                <form action="${pageContext.request.contextPath}/ta/apply" method="post" class="apply-confirm-submit-form">
                    <input type="hidden" name="jobId" value="<%= escHtml(job.getId()) %>">
                    <button type="submit" class="btn btn-success btn-lg">Submit application</button>
                </form>
            </div>
        </main>
        <aside class="right-sidebar">
            <div class="widget-card">
                <div class="widget-title">Checklist</div>
                <p class="widget-line">Update skills and CV before submitting if anything is missing.</p>
            </div>
        </aside>
    </div>
</div>
</body>
</html>
