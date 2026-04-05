<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jspf/html-esc.jspf" %>
<%@ page import="bupt.ta.model.TAProfile" %>
<%@ page import="java.util.List" %>
<% TAProfile profile = (TAProfile) request.getAttribute("profile"); if (profile == null) profile = new TAProfile();
   String returnUrlAttr = (String) request.getAttribute("returnUrl");
   request.setAttribute("taNavActive", "profile");
%>
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
                <div class="icon-dot">S</div>
                <div class="icon-dot">A</div>
                <div class="icon-dot active">P</div>
            </div>
            <%@ include file="/WEB-INF/jspf/ta-side-nav.jspf" %>
        </div>
        <main class="main-panel ta-main">
            <h1>My Profile</h1>
            <p class="ta-page-lead">Keep your skills and CV up to date so module organisers and the matching system can rank you fairly.</p>
            <div class="context-card">
                <strong>Profile Tip</strong>
                <p>Profiles with complete academic details, experience, skills, availability and CV are easier for MO to review and shortlist.</p>
            </div>
            <% if ("1".equals(request.getParameter("success"))) { %><p class="success">Profile saved.</p><% } %>
            <% if ("cv_success".equals(request.getParameter("cv_success")) || "1".equals(request.getParameter("cv_success"))) { %><p class="success">CV uploaded successfully.</p><% } %>
            <% if ("no_file".equals(request.getParameter("error"))) { %><p class="error">Please select a file to upload.</p><% } %>
            <% if ("invalid_type".equals(request.getParameter("error"))) { %><p class="error">Invalid file type. Use PDF, DOC, DOCX or TXT.</p><% } %>
            <% if (request.getAttribute("errorMessage") != null) { %><p class="error"><%= escHtml((String) request.getAttribute("errorMessage")) %></p><% } %>

            <form action="${pageContext.request.contextPath}/ta/profile" method="post" class="form form--ta ta-profile-form">
                <% if (returnUrlAttr != null && !returnUrlAttr.isEmpty()) { %>
                <input type="hidden" name="returnUrl" value="<%= escHtml(returnUrlAttr) %>">
                <% } %>
                <label>Student ID</label>
                <input type="text" name="studentId" required value="<%= escHtml(profile.getStudentId() != null ? profile.getStudentId() : "") %>">
                <label>Phone</label>
                <input type="tel" name="phone" required value="<%= escHtml(profile.getPhone() != null ? profile.getPhone() : "") %>">
                <label>Degree</label>
                <input type="text" name="degree" required value="<%= escHtml(profile.getDegree() != null ? profile.getDegree() : "") %>" placeholder="e.g. BSc, MSc">
                <label>Programme / major</label>
                <input type="text" name="programme" required value="<%= escHtml(profile.getProgramme() != null ? profile.getProgramme() : "") %>" placeholder="e.g. Computer Science">
                <label>Year of study</label>
                <input type="text" name="yearOfStudy" required value="<%= escHtml(profile.getYearOfStudy() != null ? profile.getYearOfStudy() : "") %>" placeholder="e.g. Year 2">
                <label>Previous TA or teaching experience</label>
                <textarea name="taExperience" required placeholder="Prior TA roles, tutoring, labs, etc. If none, write 'None'."><%= escHtml(profile.getTaExperience() != null ? profile.getTaExperience() : "") %></textarea>
                <label>Skills (comma-separated, e.g. Java, Python, Teaching)</label>
                <input type="text" name="skills" required value="<%= escHtml((profile.getSkills() != null && !profile.getSkills().isEmpty()) ? String.join(", ", profile.getSkills()) : "") %>">
                <label>Availability</label>
                <input type="text" name="availability" required value="<%= escHtml(profile.getAvailability() != null ? profile.getAvailability() : "") %>" placeholder="e.g. Mon/Wed/Fri 9-12">
                <label>Introduction</label>
                <textarea name="introduction" required><%= escHtml(profile.getIntroduction() != null ? profile.getIntroduction() : "") %></textarea>
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
                <p class="widget-line">Complete every required field before saving.</p>
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
