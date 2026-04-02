package bupt.ta.servlet;

import bupt.ta.model.TAProfile;
import bupt.ta.storage.DataStorage;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ViewCVServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String role = (String) req.getSession().getAttribute("role");
        String currentUserId = (String) req.getSession().getAttribute("userId");
        String targetUserId = req.getParameter("userId");

        if (targetUserId == null || targetUserId.trim().isEmpty()) {
            targetUserId = currentUserId;
        }

        boolean allowed = "ADMIN".equals(role) || "MO".equals(role) || ("TA".equals(role) && targetUserId.equals(currentUserId));
        if (!allowed) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "No permission to view this CV");
            return;
        }

        DataStorage storage = new DataStorage(getServletContext());
        TAProfile profile = storage.getProfileByUserId(targetUserId);
        if (profile == null || profile.getCvFilePath() == null || profile.getCvFilePath().trim().isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "CV not uploaded");
            return;
        }

        String storedPath = profile.getCvFilePath().replace("\\", "/");
        String relativePath = storedPath.startsWith("data/") ? storedPath : "data/" + storedPath;
        Path file = Path.of(getServletContext().getRealPath("/"), relativePath).normalize();
        if (!Files.exists(file) || !Files.isRegularFile(file)) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "CV file not found");
            return;
        }

        String filename = file.getFileName().toString();
        String contentType = getServletContext().getMimeType(filename);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        resp.setContentType(contentType);
        resp.setHeader("Content-Disposition", "inline; filename=\"" + filename + "\"");
        resp.setContentLengthLong(Files.size(file));
        Files.copy(file, resp.getOutputStream());
    }
}
