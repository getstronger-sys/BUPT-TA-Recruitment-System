package bupt.ta.servlet;

import bupt.ta.model.TAProfile;
import bupt.ta.storage.DataStorage;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TAProfileServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String userId = (String) req.getSession().getAttribute("userId");
        DataStorage storage = new DataStorage(getServletContext());
        TAProfile profile = storage.getProfileByUserId(userId);
        if (profile == null) {
            profile = new TAProfile(userId);
        }
        req.setAttribute("profile", profile);
        req.getRequestDispatcher("/ta/profile.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String userId = (String) req.getSession().getAttribute("userId");
        String studentId = req.getParameter("studentId");
        String phone = req.getParameter("phone");
        String availability = req.getParameter("availability");
        String introduction = req.getParameter("introduction");
        String skillsStr = req.getParameter("skills");

        List<String> skills = skillsStr != null && !skillsStr.trim().isEmpty()
                ? Arrays.stream(skillsStr.split(",")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList())
                : Arrays.asList();

        DataStorage storage = new DataStorage(getServletContext());
        TAProfile profile = storage.getProfileByUserId(userId);
        if (profile == null) {
            profile = new TAProfile(userId);
        }
        profile.setStudentId(studentId != null ? studentId.trim() : "");
        profile.setPhone(phone != null ? phone.trim() : "");
        profile.setAvailability(availability != null ? availability.trim() : "");
        profile.setIntroduction(introduction != null ? introduction.trim() : "");
        profile.setSkills(skills);

        storage.saveProfile(profile);
        resp.sendRedirect(req.getContextPath() + "/ta/profile?success=1");
    }
}
