<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<% List<Object[]> workloadRows = (List<Object[]>) request.getAttribute("workloadRows"); if (workloadRows == null) workloadRows = java.util.Collections.emptyList();
   Double avgWorkload = (Double) request.getAttribute("avgWorkload"); if (avgWorkload == null) avgWorkload = 0.0; %>
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
                <div class="icon-dot active">W</div>
            </div>
            <aside class="side-nav">
                <a class="active" href="${pageContext.request.contextPath}/admin/workload">TA Workload</a>
            </aside>
        </div>
        <main class="main-panel admin-main">
    <h1>TA Overall Workload</h1>
    <p class="ta-page-lead">Review selected-job distribution to keep workload fair across module assignments.</p>
    <div class="context-card">
        <strong>Admin tip</strong>
        <p>Average selected jobs per TA: <strong><%= String.format("%.1f", avgWorkload) %></strong>. Prioritize lower-load TAs where possible.</p>
    </div>
    <p><a href="${pageContext.request.contextPath}/admin/export-workload" class="btn btn-primary">Export to CSV</a></p>
    <p class="table-scroll-wrap-hint">Tip: swipe horizontally on narrow screens to view all columns.</p>
    <div class="table-scroll-wrap">
    <table class="admin-table">
        <tr>
            <th>TA Name</th>
            <th>User ID</th>
            <th># Selected Jobs</th>
            <th>Balance</th>
            <th>Jobs</th>
        </tr>
        <% for (Object[] row : workloadRows) {
            String name = (String) row[0];
            String userId = (String) row[1];
            int count = (Integer) row[2];
            List<String> jobTitles = (List<String>) row[3];
            boolean overloaded = row.length > 4 && Boolean.TRUE.equals(row[4]);
        %>
        <tr class="<%= overloaded ? "workload-high" : "" %>">
            <td><%= name %></td>
            <td><%= userId %></td>
            <td><strong><%= count %></strong></td>
            <td><% if (overloaded) { %><span class="balance-warn" title="Above average - consider workload when assigning">High</span><% } else { %><span class="balance-ok">Balanced</span><% } %></td>
            <td><%= jobTitles != null ? String.join(", ", jobTitles) : "" %></td>
        </tr>
        <% }
           if (workloadRows.isEmpty()) { %>
        <tr><td colspan="5">No TA has been selected for any job yet.</td></tr>
        <% } %>
    </table>
    </div>
        </main>
        <aside class="right-sidebar">
            <div class="widget-card">
                <div class="widget-title">Balance Summary</div>
                <p class="widget-line">Average selected jobs: <%= String.format("%.1f", avgWorkload) %></p>
                <p class="widget-line">Use this panel for fairness checks.</p>
            </div>
        </aside>
    </div>
</div>
</body>
</html>
