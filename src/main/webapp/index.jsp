<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <%@ include file="/WEB-INF/jspf/viewport.jspf" %>
    <title>BUPT TA Recruitment - Login</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body class="login-cover-body">
<div class="container container--auth container--login">
    <header class="auth-topbar auth-topbar--cover">
        <div class="auth-brand-lockup">
            <div class="auth-logo-group">
                <a class="auth-logo-mat auth-logo-mat--bupt" href="https://www.bupt.edu.cn/" target="_blank" rel="noopener noreferrer" title="北京邮电大学官网">
                    <img class="auth-logo auth-logo--bupt" src="${pageContext.request.contextPath}/images/logo-bupt.png" width="200" height="56" alt="北京邮电大学">
                </a>
                <a class="auth-logo-mat auth-logo-mat--qm" href="https://www.qmul.ac.uk/" target="_blank" rel="noopener noreferrer" title="Queen Mary University of London — official site">
                    <img class="auth-logo auth-logo--qm" src="${pageContext.request.contextPath}/images/logo-qm.png" width="180" height="48" alt="Queen Mary University of London">
                </a>
            </div>
            <div class="auth-brand-text">
                <span class="auth-brand-line1">BUPT International School</span>
                <span class="auth-brand-line2">Teaching Assistant Recruitment System</span>
            </div>
        </div>
    </header>
    <div class="auth-cover-panel">
    <div class="auth-shell auth-shell--split auth-shell--glass">
        <section class="auth-hero" aria-label="Platform introduction">
            <p class="auth-hero-copy auth-hero-lead">Sign in to continue your recruitment workflow. This portal supports Teaching Assistants, Module Organisers, and Admin staff with role-based access.</p>
            <div class="auth-role-list">
                <div class="auth-role-row">
                    <div class="auth-role-icon-wrap auth-role-icon-wrap--ta" aria-hidden="true">
                        <svg class="auth-role-svg" viewBox="0 0 40 40" width="36" height="36" xmlns="http://www.w3.org/2000/svg" focusable="false">
                            <rect x="11" y="7" width="18" height="24" rx="2.5" fill="#4a5568"/>
                            <rect x="13.5" y="10" width="13" height="2.2" rx="1" fill="#e2e8f0"/>
                            <rect x="13.5" y="14.5" width="11" height="2" rx="1" fill="#cbd5e0"/>
                            <rect x="13.5" y="18.5" width="12" height="2" rx="1" fill="#cbd5e0"/>
                            <path d="M14 23l1.8 1.8 3.5-4.2" stroke="#48bb78" stroke-width="1.6" fill="none" stroke-linecap="round" stroke-linejoin="round"/>
                            <path d="M19 23l1.6 1.6 3.2-3.8" stroke="#48bb78" stroke-width="1.6" fill="none" stroke-linecap="round" stroke-linejoin="round"/>
                            <circle cx="28" cy="27" r="6.5" fill="#38a169"/>
                            <path d="M25.2 27l1.6 1.6 3.4-4.2" stroke="#fff" stroke-width="1.8" fill="none" stroke-linecap="round" stroke-linejoin="round"/>
                        </svg>
                    </div>
                    <div class="auth-role-body">
                        <strong class="auth-role-name">Teaching Assistants:</strong>
                        <p class="auth-role-desc">Browse jobs, submit applications, and track status updates.</p>
                    </div>
                </div>
                <div class="auth-role-row">
                    <div class="auth-role-icon-wrap auth-role-icon-wrap--mo" aria-hidden="true">
                        <svg class="auth-role-svg" viewBox="0 0 40 40" width="36" height="36" xmlns="http://www.w3.org/2000/svg" focusable="false">
                            <rect x="7" y="11" width="26" height="17" rx="2" fill="#4a5568"/>
                            <rect x="9" y="13" width="22" height="12" rx="1" fill="#ebf8ff"/>
                            <rect x="11" y="15" width="11" height="2.5" rx="1" fill="#c9a227"/>
                            <rect x="11" y="19" width="18" height="1.8" rx="0.9" fill="#90cdf4"/>
                            <rect x="11" y="22.5" width="14" height="1.8" rx="0.9" fill="#90cdf4"/>
                            <rect x="5" y="30" width="30" height="2.2" rx="1" fill="#5a6a7a"/>
                            <rect x="16" y="31" width="8" height="1.5" fill="#4a5568"/>
                        </svg>
                    </div>
                    <div class="auth-role-body">
                        <strong class="auth-role-name">Module Organisers:</strong>
                        <p class="auth-role-desc">Publish postings, manage candidates, and record decisions.</p>
                    </div>
                </div>
                <div class="auth-role-row">
                    <div class="auth-role-icon-wrap auth-role-icon-wrap--admin" aria-hidden="true">
                        <svg class="auth-role-svg auth-role-svg--admin" viewBox="0 0 40 40" width="36" height="36" xmlns="http://www.w3.org/2000/svg" focusable="false">
                            <rect x="9" y="9" width="22" height="16" rx="2" fill="#4a5568"/>
                            <rect x="11" y="11" width="18" height="11" rx="1" fill="#edf2f7"/>
                            <rect x="14" y="17" width="3.5" height="5" rx="0.5" fill="#ecc94b"/>
                            <rect x="18.5" y="14" width="3.5" height="8" rx="0.5" fill="#ed8936"/>
                            <rect x="23" y="15.5" width="3.5" height="6.5" rx="0.5" fill="#68d391"/>
                        </svg>
                        <span class="auth-role-icon-overlay auth-role-icon-overlay--gear" aria-hidden="true">
                            <svg viewBox="0 0 24 24" width="18" height="18" xmlns="http://www.w3.org/2000/svg" focusable="false">
                                <path fill="#2b6cb0" d="M19.14 12.94c.04-.31.06-.63.06-.94 0-.31-.02-.63-.06-.94l2.03-1.58c.18-.14.23-.41.12-.61l-1.92-3.32c-.12-.22-.37-.29-.59-.22l-2.39.96c-.5-.38-1.03-.7-1.62-.94l-.36-2.54c-.04-.24-.24-.41-.48-.41h-3.84c-.24 0-.43.17-.47.41l-.36 2.54c-.59.24-1.13.57-1.62.94l-2.39-.96c-.22-.08-.47 0-.59.22L2.74 8.87c-.12.21-.08.47.12.61l2.03 1.58c-.04.31-.06.63-.06.94s.02.63.06.94l-2.03 1.58c-.18.14-.23.41-.12.61l1.92 3.32c.12.22.37.29.59.22l2.39-.96c.5.38 1.03.7 1.62.94l.36 2.54c.05.24.24.41.48.41h3.84c.24 0 .44-.17.47-.41l.36-2.54c.59-.24 1.13-.56 1.62-.94l2.39.96c.22.08.47 0 .59-.22l1.92-3.32c.12-.22.07-.47-.12-.61l-2.01-1.58zM12 15.6c-1.98 0-3.6-1.62-3.6-3.6s1.62-3.6 3.6-3.6 3.6 1.62 3.6 3.6-1.62 3.6-3.6 3.6z"/>
                            </svg>
                        </span>
                    </div>
                    <div class="auth-role-body">
                        <strong class="auth-role-name">Admin:</strong>
                        <p class="auth-role-desc">Monitor recruitment progress and workload metrics.</p>
                    </div>
                </div>
            </div>
            <p class="auth-hero-copy auth-hero-footnote">Use your assigned account credentials. New users can register for a TA or MO account below.</p>
        </section>

        <section class="auth-card auth-card--login">
            <div class="auth-card-head auth-card-head--left">
                <h2 class="auth-title">Login to Your Account</h2>
                <p class="auth-subtitle">Enter your username and password to continue</p>
            </div>

            <% if (request.getParameter("registered") != null) { %>
            <p class="success">Registration successful. Please sign in.</p>
            <% } %>
            <% if ("1".equals(request.getParameter("reset"))) { %>
            <p class="success">Password updated. Please sign in.</p>
            <% } %>
            <% if (request.getParameter("error") != null) { %>
            <p class="error">Please sign in to continue.</p>
            <% } %>
            <% String err = (String) request.getAttribute("error"); if (err != null) { %>
            <p class="error"><%= err %></p>
            <% } %>

            <form action="${pageContext.request.contextPath}/login" method="post" class="form form--auth form--login">
                <label for="login-username">Username</label>
                <input id="login-username" type="text" name="username" required autocomplete="username" placeholder="Enter your username">
                <label for="login-password">Password</label>
                <input id="login-password" type="password" name="password" required autocomplete="current-password" placeholder="Enter your password">
                <div class="auth-form-options">
                    <label class="auth-remember"><input type="checkbox" name="remember" value="1"> Remember me</label>
                    <a class="auth-forgot" href="${pageContext.request.contextPath}/forgot-password">Forgot password?</a>
                </div>
                <button type="submit">Sign In</button>
            </form>

            <div class="auth-card-footer auth-card-footer--stacked">
                <p class="auth-helper">New to the system?</p>
                <a href="${pageContext.request.contextPath}/register">Create an Account</a>
            </div>
        </section>
    </div>
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
