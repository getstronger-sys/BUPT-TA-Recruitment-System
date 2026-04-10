<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jspf/html-esc.jspf" %>
<%@ page import="java.util.List" %>
<%@ page import="bupt.ta.model.AdminSettings" %>
<%@ page import="bupt.ta.service.AdminService" %>
<%
    List<AdminService.WorkloadRow> workloadRows = (List<AdminService.WorkloadRow>) request.getAttribute("workloadRows");
    if (workloadRows == null) workloadRows = java.util.Collections.emptyList();
    Double avgWorkload = (Double) request.getAttribute("avgWorkload");
    if (avgWorkload == null) avgWorkload = 0.0;
    AdminSettings settings = (AdminSettings) request.getAttribute("adminSettings");
    if (settings == null) settings = new AdminSettings();
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <%@ include file="/WEB-INF/jspf/viewport.jspf" %>
    <title>TA Workload - Admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="container">
    <div class="nav top-nav">
        <span class="brand">BUPT Teaching Assistant Recruitment System</span>
        <span class="user"><%= session.getAttribute("realName") %> | <a href="${pageContext.request.contextPath}/logout">Logout</a></span>
    </div>
    <div class="page-layout">
        <div class="left-nav-wrap">
            <div class="icon-rail">
                <div class="icon-dot">D</div>
                <div class="icon-dot active">W</div>
                <div class="icon-dot">M</div>
                <div class="icon-dot">U</div>
            </div>
            <aside class="side-nav">
                <a href="${pageContext.request.contextPath}/admin/dashboard">Summary</a>
                <a class="active" href="${pageContext.request.contextPath}/admin/workload">Workload</a>
                <a href="${pageContext.request.contextPath}/admin/monitoring">Monitoring</a>
                <a href="${pageContext.request.contextPath}/admin/users">Users</a>
            </aside>
        </div>
        <main class="main-panel admin-main">
            <h1>TA Overall Workload</h1>
            <p class="ta-page-lead">Review selected and pending load distribution across TAs to keep recruitment decisions balanced and policy-compliant.</p>
            <div class="context-card">
                <strong>Workload rule</strong>
                <p>Average selected jobs per TA: <strong><%= String.format("%.1f", avgWorkload) %></strong>. Hard limit: <strong><%= settings.hasWorkloadLimit() ? settings.getMaxSelectedJobsPerTa() : 0 %></strong>. Auto-close pending: <strong><%= settings.isAutoClosePendingWhenLimitReached() ? "ON" : "OFF" %></strong>.</p>
            </div>
            <p><a href="${pageContext.request.contextPath}/admin/export-workload" class="btn btn-primary">Export to CSV</a></p>

            <p class="table-scroll-wrap-hint">Tip: swipe horizontally on narrow screens to view all columns.</p>
            <div class="table-scroll-wrap">
                <table class="admin-table">
                    <thead>
                    <tr>
                        <th>TA Name</th>
                        <th>User ID</th>
                        <th>Selected</th>
                        <th>Pending</th>
                        <th>Average flag</th>
                        <th>Limit flag</th>
                        <th>Selected jobs</th>
                    </tr>
                    </thead>
                    <tbody>
                    <% for (AdminService.WorkloadRow row : workloadRows) { %>
                    <tr class="<%= row.isAboveLimit() ? "workload-high" : row.isAtOrOverLimit() ? "workload-warning" : "" %>">
                        <td><a href="${pageContext.request.contextPath}/admin/ta-detail?userId=<%= row.getApplicantId() %>" class="admin-inline-link"><%= escHtml(row.getApplicantName()) %></a></td>
                        <td><%= escHtml(row.getApplicantId()) %></td>
                        <td><strong><%= row.getSelectedCount() %></strong></td>
                        <td><%= row.getPendingCount() %></td>
                        <td>
                            <% if (row.isAboveAverage()) { %>
                            <span class="balance-warn">Above average</span>
                            <% } else { %>
                            <span class="balance-ok">Balanced</span>
                            <% } %>
                        </td>
                        <td>
                            <% if (!settings.hasWorkloadLimit()) { %>
                            <span class="muted-inline">No limit</span>
                            <% } else if (row.isAboveLimit()) { %>
                            <span class="balance-warn">Over limit</span>
                            <% } else if (row.isAtOrOverLimit()) { %>
                            <span class="balance-warn">At limit</span>
                            <% } else { %>
                            <span class="balance-ok">Within limit</span>
                            <% } %>
                        </td>
                        <td><%= row.getSelectedJobTitles().isEmpty() ? "-" : escHtml(String.join(", ", row.getSelectedJobTitles())) %></td>
                    </tr>
                    <% } %>
                    <% if (workloadRows.isEmpty()) { %>
                    <tr>
                        <td colspan="7">No TA has been selected for any job yet.</td>
                    </tr>
                    <% } %>
                    </tbody>
                </table>
            </div>
        </main>
        <aside class="right-sidebar">
            <div class="widget-card">
                <div class="widget-title">Interpretation</div>
                <p class="widget-line">"At limit" means the TA reached the configured cap exactly.</p>
                <p class="widget-line">"Over limit" means selected jobs already exceed the configured cap.</p>
                <p class="widget-line"><a href="${pageContext.request.contextPath}/admin/users">Open user directory</a></p>
            </div>
        </aside>
    </div>
</div>
</body>
</html>
