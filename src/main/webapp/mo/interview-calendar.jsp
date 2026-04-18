<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jspf/html-esc.jspf" %>
<%@ page import="java.time.LocalDate" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.TreeMap" %>
<%@ page import="bupt.ta.servlet.MOInterviewCalendarServlet.CalendarRow" %>
<%
    @SuppressWarnings("unchecked")
    TreeMap<LocalDate, List<CalendarRow>> calendarByDay =
            (TreeMap<LocalDate, List<CalendarRow>>) request.getAttribute("calendarByDay");
    if (calendarByDay == null) {
        calendarByDay = new TreeMap<>();
    }
    @SuppressWarnings("unchecked")
    List<CalendarRow> unscheduled = (List<CalendarRow>) request.getAttribute("calendarUnscheduled");
    if (unscheduled == null) {
        unscheduled = java.util.Collections.emptyList();
    }
    Integer nSched = (Integer) request.getAttribute("calendarTotalScheduled");
    Integer nUnsched = (Integer) request.getAttribute("calendarTotalUnscheduled");
    int totalSched = nSched != null ? nSched : 0;
    int totalUnsched = nUnsched != null ? nUnsched : 0;
    String ctx = request.getContextPath();
    DateTimeFormatter dayFmt = DateTimeFormatter.ofPattern("EEEE, yyyy-MM-dd", java.util.Locale.ENGLISH);
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <%@ include file="/WEB-INF/jspf/viewport.jspf" %>
    <title>Interview calendar - MO</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="container">
    <div class="nav top-nav">
        <span class="brand">BUPT Teaching Assistant Recruitment System</span>
        <span class="user"><%= session.getAttribute("realName") %> | <a href="<%= ctx %>/logout">Logout</a></span>
    </div>
    <div class="page-layout">
        <div class="left-nav-wrap">
            <div class="icon-rail">
                <div class="icon-dot active">J</div>
                <div class="icon-dot">P</div>
                <div class="icon-dot">D</div>
            </div>
            <%@ include file="/WEB-INF/jspf/mo-side-nav.jspf" %>
        </div>
        <main class="main-panel mo-main">
            <h1>Interview calendar</h1>
            <p class="mo-page-lead">All <strong>Interview</strong> and <strong>Waitlist</strong> applications across your postings, grouped by booked slot time when available, or by the legacy per-applicant interview notice field otherwise.</p>

            <div class="stats-row mo-cal-stats">
                <div class="stat-card">
                    <div class="stat-icon">D</div>
                    <div>
                        <div class="stat-title">Scheduled slots</div>
                        <div class="stat-value"><%= totalSched %></div>
                        <div class="stat-meta">Rows with a parseable date</div>
                    </div>
                </div>
                <div class="stat-card">
                    <div class="stat-icon">N</div>
                    <div>
                        <div class="stat-title">Needs date / text</div>
                        <div class="stat-value"><%= totalUnsched %></div>
                        <div class="stat-meta">Interview or waitlist without a recognised date</div>
                    </div>
                </div>
            </div>

            <% if (calendarByDay.isEmpty() && unscheduled.isEmpty()) { %>
            <p class="section-empty section-empty--card">No interview or waitlist applicants yet. Use <strong>My Jobs</strong> &rarr; Interview tab to move candidates and send notices.</p>
            <% } else { %>
            <div class="mo-cal-day-list">
                <% for (Map.Entry<LocalDate, List<CalendarRow>> e : calendarByDay.entrySet()) {
                    LocalDate d = e.getKey();
                    List<CalendarRow> rows = e.getValue();
                %>
                <section class="detail-card mo-cal-day-card">
                    <h2 class="mo-cal-day-title"><%= escHtml(d.format(dayFmt)) %></h2>
                    <div class="table-scroll-wrap">
                        <table class="admin-table mo-cal-table">
                            <thead>
                            <tr>
                                <th>Time</th>
                                <th>Applicant</th>
                                <th>Job</th>
                                <th>Module</th>
                                <th>Location</th>
                                <th>Status</th>
                                <th></th>
                            </tr>
                            </thead>
                            <tbody>
                            <% for (CalendarRow r : rows) { %>
                            <tr>
                                <td><%= escHtml(r.getTimeDisplay()) %></td>
                                <td><%= escHtml(r.getApplicantName()) %></td>
                                <td><%= escHtml(r.getJobTitle()) %></td>
                                <td><%= escHtml(r.getModuleCode()) %></td>
                                <td class="pre-wrap"><%= escHtml(r.getLocation().isEmpty() ? "—" : r.getLocation()) %></td>
                                <td><%= escHtml(r.getStatus()) %></td>
                                <td><a class="btn btn-secondary btn-sm" href="<%= r.manageHref(ctx) %>">Manage posting</a></td>
                            </tr>
                            <% } %>
                            </tbody>
                        </table>
                    </div>
                </section>
                <% } %>
            </div>

            <% if (!unscheduled.isEmpty()) { %>
            <section class="detail-card mo-cal-unscheduled">
                <h2 class="mo-cal-day-title">Awaiting a parseable date</h2>
                <p class="muted-inline">These applicants are in Interview or Waitlist but the interview time field is empty or not in a recognised format. You can still open the posting and edit notices.</p>
                <div class="table-scroll-wrap">
                    <table class="admin-table mo-cal-table">
                        <thead>
                        <tr>
                            <th>Applicant</th>
                            <th>Job</th>
                            <th>Module</th>
                            <th>Time (raw)</th>
                            <th>Location</th>
                            <th>Status</th>
                            <th></th>
                        </tr>
                        </thead>
                        <tbody>
                        <% for (CalendarRow r : unscheduled) { %>
                        <tr>
                            <td><%= escHtml(r.getApplicantName()) %></td>
                            <td><%= escHtml(r.getJobTitle()) %></td>
                            <td><%= escHtml(r.getModuleCode()) %></td>
                            <td class="pre-wrap"><%= escHtml(r.getInterviewTimeRaw() == null || r.getInterviewTimeRaw().isEmpty() ? "—" : r.getInterviewTimeRaw()) %></td>
                            <td class="pre-wrap"><%= escHtml(r.getLocation().isEmpty() ? "—" : r.getLocation()) %></td>
                            <td><%= escHtml(r.getStatus()) %></td>
                            <td><a class="btn btn-secondary btn-sm" href="<%= r.manageHref(ctx) %>">Manage posting</a></td>
                        </tr>
                        <% } %>
                        </tbody>
                    </table>
                </div>
            </section>
            <% } %>
            <% } %>
        </main>
        <aside class="right-sidebar">
            <div class="widget-card">
                <div class="widget-title">Tip</div>
                <p class="widget-line">Use consistent date formats in interview notices so rows appear on the correct day.</p>
            </div>
        </aside>
    </div>
</div>
</body>
</html>
