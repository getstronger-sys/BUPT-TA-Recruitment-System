<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="bupt.ta.model.Job" %>
<%@ page import="bupt.ta.model.Application" %>
<%@ page import="bupt.ta.ai.AIMatchService" %>
<% List<Object[]> jobsWithApps = (List<Object[]>) request.getAttribute("jobsWithApps"); if (jobsWithApps == null) jobsWithApps = java.util.Collections.emptyList(); %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>My Jobs - MO</title>
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
                <div class="icon-dot active">J</div>
                <div class="icon-dot">P</div>
                <div class="icon-dot">D</div>
            </div>
            <aside class="side-nav">
                <a class="active" href="${pageContext.request.contextPath}/mo/jobs">My Jobs</a>
                <a href="${pageContext.request.contextPath}/mo/post-job">Post Job</a>
            </aside>
        </div>
        <main class="main-panel">
            <h1>My Posted Jobs</h1>
            <div class="context-card">
                <strong>MO Tip</strong>
                <p>Use AI match score as a reference, then check applicant profile and CV before final decision.</p>
            </div>
            <p class="ai-hint"><strong>AI Recommendations</strong>: Applicants sorted by skill match score. Workload balance hint shown for fair distribution.</p>
            <% if ("1".equals(request.getParameter("success"))) { %><p class="success">Job posted successfully!</p><% } %>
            <% if ("1".equals(request.getParameter("updated"))) { %><p class="success">Applicant status updated.</p><% } %>
            <% String err = request.getParameter("error"); if (err != null) { %><p class="error">Error: <%= err %></p><% } %>

            <p><a href="${pageContext.request.contextPath}/mo/post-job" class="btn btn-primary">Post New Job</a></p>

            <% for (Object[] row : jobsWithApps) {
                Job j = (Job) row[0];
                List<AIMatchService.ApplicantRecommendation> recs = (List<AIMatchService.ApplicantRecommendation>) row[1];
                if (recs == null) recs = java.util.Collections.emptyList();
            %>
            <div class="job-card">
                <div class="job-card-head">
                    <div class="job-card-title">
                        <h3><%= j.getTitle() %> - <%= j.getModuleCode() %> <span class="job-status-text">(<%= j.getStatus() %>)</span></h3>
                        <% if (j.getJobType() != null && !j.getJobType().isEmpty()) { %>
                        <p class="skills">Type: <%= "MODULE_TA".equals(j.getJobType()) ? "Module TA" : "INVIGILATION".equals(j.getJobType()) ? "Invigilation" : "Other" %></p>
                        <% } %>
                    </div>
                    <div class="job-card-actions">
                        <% if ("OPEN".equals(j.getStatus())) { %>
                        <form action="${pageContext.request.contextPath}/mo/close-job" method="post">
                            <input type="hidden" name="jobId" value="<%= j.getId() %>">
                            <input type="hidden" name="action" value="close">
                            <button type="submit" class="btn btn-danger">Close Job</button>
                        </form>
                        <% } else { %>
                        <form action="${pageContext.request.contextPath}/mo/close-job" method="post">
                            <input type="hidden" name="jobId" value="<%= j.getId() %>">
                            <input type="hidden" name="action" value="reopen">
                            <button type="submit" class="btn btn-primary">Reopen</button>
                        </form>
                        <% } %>
                    </div>
                </div>

                <p class="job-description"><%= j.getDescription() != null ? j.getDescription() : "" %></p>
                <% if (j.getRequiredSkills() != null && !j.getRequiredSkills().isEmpty()) { %>
                <p class="skills">Required: <%= String.join(", ", j.getRequiredSkills()) %></p>
                <% } %>

                <div class="applicants-panel">
                    <div class="applicants-head">
                        <h4>Applicants (<%= recs.size() %>)</h4>
                        <p>AI sorted by match score first, then workload balance.</p>
                    </div>

                    <% if (recs.isEmpty()) { %>
                    <div class="empty-applicants-card">No applications yet.</div>
                    <% } %>

                    <% for (AIMatchService.ApplicantRecommendation rec : recs) {
                        Application a = rec.application;
                        String statusPillClass = "status-pill status-pill-pending";
                        if ("SELECTED".equals(a.getStatus())) {
                            statusPillClass = "status-pill status-pill-selected";
                        } else if ("REJECTED".equals(a.getStatus()) || "WITHDRAWN".equals(a.getStatus())) {
                            statusPillClass = "status-pill status-pill-rejected";
                        }
                        String applicantName = a.getApplicantName() != null ? a.getApplicantName() : a.getApplicantId();
                        String appliedText = a.getAppliedAt() != null ? a.getAppliedAt().replace("T", " ").replaceFirst("\\..*$", "") : "-";
                        boolean hasProfile = rec.profile != null;
                        String skillsText = hasProfile && rec.profile.getSkills() != null && !rec.profile.getSkills().isEmpty() ? String.join(", ", rec.profile.getSkills()) : "Not provided";
                        String missingText = rec.matchResult.missing != null && !rec.matchResult.missing.isEmpty() ? String.join(", ", rec.matchResult.missing) : "No major gaps";
                        boolean hasCv = hasProfile && rec.profile.getCvFilePath() != null && !rec.profile.getCvFilePath().isEmpty();
                        String noteText = a.getNotes() != null && !a.getNotes().isEmpty() ? a.getNotes() : "No notes saved for this application.";
                        String profileStateText;
                        if (!hasProfile) {
                            profileStateText = "No profile submitted yet.";
                        } else if (hasCv) {
                            profileStateText = "Profile available, CV uploaded.";
                        } else {
                            profileStateText = "Profile available, CV missing.";
                        }
                    %>
                    <article class="applicant-card">
                        <div class="applicant-topline">
                            <div class="applicant-title-group">
                                <div class="applicant-name-row">
                                    <h5><%= applicantName %></h5>
                                    <% if (rec.workloadBalanced && "PENDING".equals(a.getStatus())) { %>
                                    <span class="load-badge" title="Lower workload, suitable for balance">Low load</span>
                                    <% } %>
                                </div>
                                <div class="applicant-links">
                                    <a href="${pageContext.request.contextPath}/mo/applicant-detail?applicantId=<%= a.getApplicantId() %>" class="mini-link">Full profile</a>
                                    <% if (hasCv) { %>
                                    <a href="${pageContext.request.contextPath}/view-cv?userId=<%= a.getApplicantId() %>" target="_blank" class="mini-link">View CV</a>
                                    <% } else { %>
                                    <span class="muted-inline">CV not uploaded</span>
                                    <% } %>
                                </div>
                            </div>
                            <div class="applicant-score-area">
                                <span class="match-badge" title="<%= rec.matchResult.explanation %>"><%= (int)rec.matchResult.score %>% match</span>
                                <span class="<%= statusPillClass %>"><%= a.getStatus() %></span>
                            </div>
                        </div>

                        <div class="applicant-sections applicant-sections-compact">
                            <section class="applicant-section">
                                <div class="section-label">Profile</div>
                                <p class="section-copy"><strong>Skills:</strong> <%= skillsText %></p>
                                <p class="section-copy muted-inline"><%= profileStateText %></p>
                            </section>

                            <section class="applicant-section">
                                <div class="section-label">AI Review</div>
                                <p class="section-copy"><strong>Missing skills:</strong> <%= missingText %></p>
                                <p class="section-copy"><strong>Workload:</strong> <%= rec.currentWorkload %> jobs</p>
                            </section>

                            <section class="applicant-section">
                                <div class="section-label">Application</div>
                                <p class="section-copy"><strong>Applied:</strong> <%= appliedText %></p>
                                <p class="section-copy"><strong>CV status:</strong> <%= hasCv ? "Ready for review" : "Missing" %></p>
                            </section>
                        </div>

                        <div class="applicant-actions">
                            <% if ("PENDING".equals(a.getStatus())) { %>
                            <div class="decision-bar">
                                <div class="decision-bar-copy">
                                    <div class="section-label">Make decision</div>
                                    <p>Add notes only if they help explain the selection decision.</p>
                                </div>
                                <form action="${pageContext.request.contextPath}/mo/select-applicant" method="post" class="decision-form decision-form-inline">
                                    <input type="hidden" name="applicationId" value="<%= a.getId() %>">
                                    <input type="text" name="notes" placeholder="Optional notes" class="note-input">
                                    <div class="decision-buttons decision-buttons-inline">
                                        <button type="submit" name="action" value="select" class="btn btn-success decision-btn">Select</button>
                                        <button type="submit" name="action" value="reject" class="btn btn-danger decision-btn">Reject</button>
                                    </div>
                                </form>
                            </div>
                            <% } else { %>
                            <div class="decision-bar decision-bar-recorded">
                                <div class="decision-bar-copy">
                                    <div class="section-label">Decision recorded</div>
                                    <p>Status saved as <strong><%= a.getStatus() %></strong>.</p>
                                </div>
                                <div class="decision-summary">
                                    <p><strong>Notes:</strong> <%= noteText %></p>
                                </div>
                            </div>
                            <% } %>
                        </div>
                    </article>
                    <% } %>
                </div>
            </div>
            <% }
               if (jobsWithApps.isEmpty()) { %>
            <p>No jobs posted yet. <a href="${pageContext.request.contextPath}/mo/post-job">Post your first job</a>.</p>
            <% } %>
        </main>
        <aside class="right-sidebar">
            <div class="widget-card">
                <div class="widget-title">MO Dashboard</div>
                <p class="widget-line">Track applicant quality and workload balance.</p>
                <p class="widget-line">Use profile + CV before selecting.</p>
            </div>
            <div class="widget-card">
                <div class="widget-title">Reminder</div>
                <p class="widget-line">Close jobs after quota is met.</p>
            </div>
        </aside>
    </div>
</div>
</body>
</html>
