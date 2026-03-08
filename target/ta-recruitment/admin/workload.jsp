<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<% List<Object[]> workloadRows = (List<Object[]>) request.getAttribute("workloadRows"); if (workloadRows == null) workloadRows = java.util.Collections.emptyList();
   Double avgWorkload = (Double) request.getAttribute("avgWorkload"); if (avgWorkload == null) avgWorkload = 0.0; %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>TA Workload - Admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="container">
    <div class="nav">
        <a href="${pageContext.request.contextPath}/admin/workload">TA Workload</a>
        <span class="user"><%= session.getAttribute("realName") %> | <a href="${pageContext.request.contextPath}/logout">Logout</a></span>
    </div>
    <h1>TA Overall Workload</h1>
    <p class="ai-hint"><strong>AI Workload Balancing</strong>: Average jobs per TA: <%= String.format("%.1f", avgWorkload) %>. TAs above average are flagged for consideration when assigning new jobs.</p>
    <p><a href="${pageContext.request.contextPath}/admin/export-workload" class="btn btn-primary">Export to CSV</a></p>
    <table>
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
            <td><% if (overloaded) { %><span class="balance-warn" title="Above average - consider workload when assigning">⚠ High</span><% } else { %><span class="balance-ok">✓ OK</span><% } %></td>
            <td><%= jobTitles != null ? String.join(", ", jobTitles) : "" %></td>
        </tr>
        <% }
           if (workloadRows.isEmpty()) { %>
        <tr><td colspan="5">No TA has been selected for any job yet.</td></tr>
        <% } %>
    </table>
</div>
</body>
</html>
