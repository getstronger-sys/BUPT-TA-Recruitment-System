<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jspf/html-esc.jspf" %>
<%@ page import="bupt.ta.model.TAProfile" %>
<%
    TAProfile profile = (TAProfile) request.getAttribute("profile");
    if (profile == null) profile = new TAProfile();
    int totalApps = request.getAttribute("totalApplications") != null ? (Integer) request.getAttribute("totalApplications") : 0;
    int pending = request.getAttribute("pendingCount") != null ? (Integer) request.getAttribute("pendingCount") : 0;
    int selected = request.getAttribute("selectedCount") != null ? (Integer) request.getAttribute("selectedCount") : 0;
    int other = request.getAttribute("otherApplicationsCount") != null ? (Integer) request.getAttribute("otherApplicationsCount") : 0;
    int openJobs = request.getAttribute("openJobsCount") != null ? (Integer) request.getAttribute("openJobsCount") : 0;
    int savedJobs = request.getAttribute("savedJobsCount") != null ? (Integer) request.getAttribute("savedJobsCount") : 0;
    boolean hasCv = Boolean.TRUE.equals(request.getAttribute("hasCv"));
    boolean hasSkills = Boolean.TRUE.equals(request.getAttribute("hasSkills"));
    boolean hasStudentId = Boolean.TRUE.equals(request.getAttribute("hasStudentId"));
    boolean hasEmail = Boolean.TRUE.equals(request.getAttribute("hasEmail"));
    request.setAttribute("taNavActive", "home");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <%@ include file="/WEB-INF/jspf/viewport.jspf" %>
    <title>Home - TA</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="container">
    <div class="nav top-nav">
        <span class="brand">BUPT Teaching Assistant Recruitment System</span>
        <div class="user user-inline-actions"><span><%= session.getAttribute("realName") %> |</span><form action="${pageContext.request.contextPath}/logout" method="post" class="inline-form logout-form"><%@ include file="/WEB-INF/jspf/csrf-hidden.jspf" %><button type="submit" class="logout-button">Logout</button></form></div>
    </div>
    <div class="page-layout">
        <div class="left-nav-wrap">
            <div class="icon-rail">
                <div class="icon-dot active">H</div>
                <div class="icon-dot">F</div>
                <div class="icon-dot">S</div>
                <div class="icon-dot">A</div>
            </div>
            <%@ include file="/WEB-INF/jspf/ta-side-nav.jspf" %>
        </div>
        <main class="main-panel ta-main ta-home-dashboard">
            <h1>Welcome back, <%= escHtml(session.getAttribute("realName") != null ? session.getAttribute("realName").toString() : "TA") %></h1>
            <p class="ta-page-lead">Use the shortcuts below to track your applications and keep your profile ready for module organisers.</p>
            <div class="context-card ta-dash-email-hint">
                <strong>Contact email for module organisers</strong>
                <p>Enter or confirm your email under <strong>My Profile</strong> (sidebar) — it appears to organisers when you apply. <a href="${pageContext.request.contextPath}/ta/profile#ta-profile-email">Go to email field</a></p>
            </div>

            <div class="stats-row ta-dash-stats">
                <div class="stat-card">
                    <div class="stat-icon">J</div>
                    <div>
                        <div class="stat-title">Open jobs</div>
                        <div class="stat-value"><%= openJobs %></div>
                        <div class="stat-meta"><a class="ta-dash-stat-link" href="${pageContext.request.contextPath}/ta/jobs">Browse positions</a></div>
                    </div>
                </div>
                <div class="stat-card">
                    <div class="stat-icon">A</div>
                    <div>
                        <div class="stat-title">My applications</div>
                        <div class="stat-value"><%= totalApps %></div>
                        <div class="stat-meta">Pending <%= pending %> | Selected <%= selected %><% if (other > 0) { %> | Other <%= other %><% } %></div>
                        <div class="stat-meta"><a class="ta-dash-stat-link" href="${pageContext.request.contextPath}/ta/applications">View all</a></div>
                    </div>
                </div>
                <div class="stat-card">
                    <div class="stat-icon">S</div>
                    <div>
                        <div class="stat-title">Saved jobs</div>
                        <div class="stat-value"><%= savedJobs %></div>
                        <div class="stat-meta"><a class="ta-dash-stat-link" href="${pageContext.request.contextPath}/ta/saved-jobs">Open saved list</a></div>
                    </div>
                </div>
            </div>

            <div class="detail-card ta-dash-checklist">
                <h3>Profile checklist</h3>
                <p>MOs review your <strong>saved profile and CV</strong> when you apply. Items marked missing may weaken your application.</p>
                <ul class="dash-checklist">
                    <li class="<%= hasStudentId ? "dash-ok" : "dash-miss" %>"><%= hasStudentId ? "Student ID on file" : "Add student ID" %></li>
                    <li class="<%= hasEmail ? "dash-ok" : "dash-miss" %>"><%= hasEmail ? "Contact email on file" : "Add contact email (My Profile)" %></li>
                    <li class="<%= hasSkills ? "dash-ok" : "dash-miss" %>"><%= hasSkills ? "Skills listed" : "Add skills for better AI matching" %></li>
                    <li class="<%= hasCv ? "dash-ok" : "dash-miss" %>"><%= hasCv ? "CV uploaded" : "Upload a CV (PDF/DOC/DOCX/TXT)" %></li>
                </ul>
                <p><a href="${pageContext.request.contextPath}/ta/profile" class="btn btn-primary">Open My Profile</a></p>
            </div>

            <div class="context-card ta-dash-quick-card">
                <strong>Quick links</strong>
                <div class="ta-dash-quick-row">
                    <a class="ta-dash-quick-btn" href="${pageContext.request.contextPath}/ta/jobs"><span class="ta-dash-quick-ico">J</span>Find Jobs</a>
                    <a class="ta-dash-quick-btn" href="${pageContext.request.contextPath}/ta/saved-jobs"><span class="ta-dash-quick-ico">S</span>Saved Jobs</a>
                    <a class="ta-dash-quick-btn" href="${pageContext.request.contextPath}/ta/applications"><span class="ta-dash-quick-ico">A</span>My Applications</a>
                    <a class="ta-dash-quick-btn" href="${pageContext.request.contextPath}/ta/profile"><span class="ta-dash-quick-ico">P</span>My Profile</a>
                </div>
            </div>
        </main>
        <aside class="right-sidebar">
            <div class="widget-card">
                <div class="widget-title">Tip</div>
                <p class="widget-line">Before you apply, open the job and use <strong>Review and apply</strong> to confirm what will be shared.</p>
            </div>
        </aside>
    </div>
</div>
<%@ include file="/WEB-INF/jspf/login-celebration.jspf" %>
</body>
</html>
