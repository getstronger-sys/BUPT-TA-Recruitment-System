<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jspf/html-esc.jspf" %>
<%@ page import="java.util.List" %>
<%@ page import="bupt.ta.model.JobTemplate" %>
<%!
    static String fv(javax.servlet.http.HttpServletRequest r, String key, String def) {
        Object o = r.getAttribute(key);
        return o != null ? (String) o : (def != null ? def : "");
    }
    static String fva(javax.servlet.http.HttpServletRequest r, String key) {
        return escHtml(fv(r, key, ""));
    }
%>
<% List<JobTemplate> jobTemplates = (List<JobTemplate>) request.getAttribute("jobTemplates");
   if (jobTemplates == null) jobTemplates = java.util.Collections.emptyList();
   String selectedTemplateId = request.getAttribute("selectedTemplateId") != null ? request.getAttribute("selectedTemplateId").toString() : "";
   boolean fvAutoFill = Boolean.TRUE.equals(request.getAttribute("fvAutoFillFromWaitlist"));
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <%@ include file="/WEB-INF/jspf/viewport.jspf" %>
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
                <a href="${pageContext.request.contextPath}/mo/past-jobs">Past postings</a>
            </aside>
        </div>
        <main class="main-panel mo-main">
    <h1>Post a New Job</h1>
    <p class="mo-page-lead">Create a clear listing so TAs can judge fit before applying. Required fields are marked below.</p>
    <div class="context-card">
        <strong>Posting checklist</strong>
        <p>Fields marked * are required. Deadline must be YYYY-MM-DD and not in the past. Responsibilities at least 20 characters. At least one skill.</p>
    </div>
    <div class="context-card">
        <strong>Reusable templates</strong>
        <p>Load a saved template to prefill course code, skills, workload and description, then adjust only the fields that changed for this term.</p>
        <form action="${pageContext.request.contextPath}/mo/post-job" method="get" class="search-form">
            <select name="templateId">
                <option value="">Choose a saved template</option>
                <% for (JobTemplate t : jobTemplates) { %>
                <option value="<%= escHtml(t.getId()) %>" <%= t.getId().equals(selectedTemplateId) ? "selected" : "" %>><%= escHtml(t.getTemplateName()) %> - <%= escHtml(t.getModuleCode()) %></option>
                <% } %>
            </select>
            <button type="submit" class="btn btn-primary">Load template</button>
        </form>
    </div>
    <% String err = (String) request.getAttribute("error"); if (err != null) { %>
    <p class="error"><%= escHtml(err) %></p>
    <% } %>
    <form action="${pageContext.request.contextPath}/mo/post-job" method="post" class="form form--mo-post">
        <label>Job Title *</label>
        <input type="text" name="title" required placeholder="e.g. TA for Software Engineering" value="<%= fva(request, "fvTitle") %>">
        <label>Module Code *</label>
        <input type="text" name="moduleCode" required placeholder="e.g. EBU6304" value="<%= fva(request, "fvModuleCode") %>">
        <label>Module Name *</label>
        <input type="text" name="moduleName" required placeholder="e.g. Software Engineering" value="<%= fva(request, "fvModuleName") %>">
        <label>Job Type</label>
        <select name="jobType">
            <% String jt = fv(request, "fvJobType", "MODULE_TA");
               if (jt.isEmpty()) jt = "MODULE_TA"; %>
            <option value="MODULE_TA" <%= "MODULE_TA".equals(jt) ? "selected" : "" %>>Module TA</option>
            <option value="INVIGILATION" <%= "INVIGILATION".equals(jt) ? "selected" : "" %>>Invigilation</option>
            <option value="OTHER" <%= "OTHER".equals(jt) ? "selected" : "" %>>Other</option>
        </select>
        <label>Description (overview)</label>
        <textarea name="description" placeholder="Short overview for the listing..."><%= fva(request, "fvDescription") %></textarea>
        <label>Responsibilities * <span class="muted-inline">(min 20 characters)</span></label>
        <textarea name="responsibilities" required minlength="20" rows="5" placeholder="What the TA will do: labs, marking, office hours..."><%= fva(request, "fvResponsibilities") %></textarea>
        <label>Working hours / schedule *</label>
        <input type="text" name="workingHours" required placeholder="e.g. Wed 14:00–16:00; or 6 hrs/week flexible" value="<%= fva(request, "fvWorkingHours") %>">
        <label>Workload *</label>
        <input type="text" name="workload" required placeholder="e.g. ~8 hours/week; 2 lab sessions" value="<%= fva(request, "fvWorkload") %>">
        <label>Payment / compensation *</label>
        <input type="text" name="payment" required placeholder="e.g. £15/hour; stipend amount" value="<%= fva(request, "fvPayment") %>">
        <label>Application deadline * <span class="muted-inline">(YYYY-MM-DD)</span></label>
        <input type="date" name="deadline" required value="<%= fva(request, "fvDeadline") %>">
        <label>Required Skills * <span class="muted-inline">(comma-separated)</span></label>
        <input type="text" name="skills" required placeholder="e.g. Java, Python, Teaching" value="<%= fva(request, "fvSkills") %>">
        <label>Max Applicants (0 = unlimited)</label>
        <input type="number" name="maxApplicants" min="0" value="<%= fva(request, "fvMaxApplicants").isEmpty() ? "0" : fva(request, "fvMaxApplicants") %>">
        <label class="checkbox-line"><input type="checkbox" name="autoFillFromWaitlist" <%= fvAutoFill ? "checked" : "" %>> Auto-fill from waitlist when a selected TA withdraws</label>
        <label class="checkbox-line"><input type="checkbox" name="saveAsTemplate"> Save this posting setup as a reusable template</label>
        <label>Template name <span class="muted-inline">(optional when saving)</span></label>
        <input type="text" name="templateName" placeholder="e.g. EBU6304 standard TA template" value="<%= fva(request, "fvTemplateName") %>">
        <button type="submit" class="btn btn-primary">Post Job</button>
    </form>
        </main>
        <aside class="right-sidebar">
            <div class="widget-card">
                <div class="widget-title">Why these fields</div>
                <p class="widget-line">TAs see full detail before applying.</p>
                <p class="widget-line">Payment and hours reduce mismatched expectations.</p>
            </div>
        </aside>
    </div>
</div>
</body>
</html>
